package no.officenet.example.rpm.support.infrastructure.spring.aop

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.FieldSignature
import org.aspectj.lang.reflect.MethodSignature
import org.hibernate.Hibernate
import org.hibernate.HibernateException
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.collection.AbstractPersistentCollection
import org.hibernate.engine.SessionImplementor
import org.hibernate.proxy.HibernateProxy
import org.springframework.beans.factory.InitializingBean
import org.springframework.orm.hibernate3.SessionFactoryUtils
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import java.lang.reflect.Method

@Aspect
class LazyInitAspect extends InitializingBean {
	private val log: Log = LogFactory.getLog(getClass)

	@PersistenceContext
	private val entityManager: EntityManager = null
	private var sessionFactory: SessionFactory = null
	protected var locked = new ThreadLocalInit[Boolean](false)

	def afterPropertiesSet() {
		sessionFactory = (entityManager.getDelegate.asInstanceOf[Session]).getSessionFactory
	}

	@Pointcut("(@within(javax.persistence.Entity) || @within(javax.persistence.MappedSuperclass))" +
		" && (" +
		"@annotation(javax.persistence.ManyToOne)" +
		" || @annotation(javax.persistence.ManyToMany)" +
		" || @annotation(javax.persistence.OneToMany)" +
		" || @annotation(javax.persistence.OneToOne)" +
		")")
	def lazyLoadableJpaProperties() {
	}

	private def getPropertyName(method: Method): String = {
		val methodName = method.getName
		var propertyName: String = null
		if (methodName.startsWith("get")) {
			propertyName = methodName.substring("get".length)
		}
		else if (methodName.startsWith("is")) {
            // Note the getter will probably never be a "is" because this aspect only applies to
            // JPA-associations, which are not primitives, thus never boolean-types
			propertyName = methodName.substring("is".length)
		}
		else {
			throw new IllegalArgumentException("Unknow read-method: " + methodName)
		}
		propertyName = propertyName.substring(0, 1).toLowerCase + propertyName.substring(1)
		propertyName
	}

	private def getImplementation(obj: AnyRef): AnyRef = {
		Hibernate.initialize(obj)
		obj match {
			case hp: HibernateProxy => hp.getHibernateLazyInitializer.getImplementation
			case _ => obj
		}
	}

	private def getSetter(entityType: Class[_], propertyName: String, returnType: Class[_]): Method = {
		val setterName: String = "set" + propertyName.substring(0, 1).toUpperCase + propertyName.substring(1)
		entityType.getDeclaredMethod(setterName, returnType)
	}

	@Around("no.officenet.example.rpm.support.infrastructure.spring.aop.LazyInitAspect.lazyLoadableJpaProperties()")
	def reattachSession(pjp: ProceedingJoinPoint): AnyRef = {
		var obj: AnyRef = pjp.proceed
		if (obj == null || !LazyInitState.lazyInit.get) return obj
		val traceEnabled: Boolean = log.isTraceEnabled
		// Don't try to re-init if already re-initing some stuff on the same thread.
		// This happens when the persistence-provider calls the getters when it's initing)
		if (!locked.get) {
			val implementor: SessionImplementor = getSessionImplementor(obj)
			val hasOpenSession = implementor != null && implementor.isOpen
			val reAttachNeeded = !hasOpenSession && !Hibernate.isInitialized(obj)
			if (reAttachNeeded) {
				try {
					locked.set(true)
					obj = loadAndReAttach(pjp, traceEnabled)
				}
				finally {
					locked.set(false)
				}
			}
		}
		obj
	}

	private def getSessionImplementor(obj: AnyRef): SessionImplementor = {
		var implementor: SessionImplementor = null
		if (obj.isInstanceOf[AbstractPersistentCollection]) {
			val ps = obj.asInstanceOf[AbstractPersistentCollection]
			implementor = ps.getSession
		}
		else if (obj.isInstanceOf[HibernateProxy]) {
			val li = (obj.asInstanceOf[HibernateProxy]).getHibernateLazyInitializer
			implementor = li.getSession
		}
		implementor
	}

	private def loadAndReAttach(pjp: ProceedingJoinPoint, traceEnabled: Boolean): AnyRef = {
		var session: Session = null
		var obj: AnyRef = null
		var newSession = false
		try {
			try {
				session = sessionFactory.getCurrentSession
			}
			catch {
				case e: HibernateException => {
					session = SessionFactoryUtils.getNewSession(sessionFactory)
					newSession = true
				}
			}
			if (traceEnabled) {
				log.trace("Re-attaching Hibernate session to " + pjp.toString)
			}
			val implementor: SessionImplementor = session.asInstanceOf[SessionImplementor]
			val pjpTarget = pjp.getTarget
			val entityType = pjpTarget.getClass
			val identifier = sessionFactory.getClassMetadata(entityType).getIdentifier(pjpTarget, implementor)
			val newInstance = getImplementation(session.load(entityType, identifier))
			obj = initializeAndReAttach(pjp, newInstance)
		}
		finally {
			if (session != null && newSession) {
				try {
					SessionFactoryUtils.closeSession(session)
				}
				catch {
					case t: Throwable => {
					}
				}
			}
		}
		if (traceEnabled) {
			log.trace("obj: " + obj.getClass.getSimpleName + "@" + System.identityHashCode(obj) + " isInitialized: " + Hibernate.isInitialized(obj))
		}
		obj
	}

	private def initializeAndReAttach(pjp: ProceedingJoinPoint, newInstance: AnyRef): AnyRef = {
		var obj: AnyRef = null
		val pjpTarget = pjp.getTarget
		val entityType = pjpTarget.getClass
		pjp.getSignature match {
			case methodSignature: MethodSignature =>
				val getter = methodSignature.getMethod
				var wasAccessible = getter.isAccessible
				if (!wasAccessible) {
					getter.setAccessible(true)
				}
				obj = getter.invoke(newInstance)
				Hibernate.initialize(obj)
				if (!wasAccessible) {
					getter.setAccessible(false)
				}
				val propertyName: String = getPropertyName(getter)
				val setter: Method = getSetter(entityType, propertyName, getter.getReturnType)
				wasAccessible = setter.isAccessible
				if (!wasAccessible) {
					setter.setAccessible(true)
				}
				setter.invoke(pjpTarget, obj)
				if (!wasAccessible) {
					setter.setAccessible(false)
				}
			case fieldSignature: FieldSignature =>
				val field = fieldSignature.getField
				val wasAccessible = field.isAccessible
				if (!wasAccessible) {
					field.setAccessible(true)
				}
				obj = field.get(newInstance)
				Hibernate.initialize(obj)
				field.set(pjpTarget, obj)
				if (!wasAccessible) {
					field.setAccessible(false)
				}
				case signature =>
					throw new IllegalStateException("Unsupported signature-type: " + signature)
		}
		obj
	}

}