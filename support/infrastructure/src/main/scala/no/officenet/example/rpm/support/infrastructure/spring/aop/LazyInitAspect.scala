package no.officenet.example.rpm.support.infrastructure.spring.aop

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
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

	private class ThreadLocaleInit[T](initValue: T) extends ThreadLocal[T] {
		protected override def initialValue: T = initValue
	}

	@PersistenceContext(unitName = "RPM")
	private val entityManagerRPM: EntityManager = null
	@PersistenceContext(unitName = "PETS")
	private val entityManagerPETS: EntityManager = null

	private var sessionFactoryRPM: SessionFactory = null
	private var sessionFactoryPETS: SessionFactory = null

	protected var locked: ThreadLocal[Boolean] = new ThreadLocaleInit[Boolean](false)

	def afterPropertiesSet() {
		sessionFactoryRPM = (entityManagerRPM.getDelegate.asInstanceOf[Session]).getSessionFactory
		sessionFactoryPETS = (entityManagerPETS.getDelegate.asInstanceOf[Session]).getSessionFactory
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
		val initialized = obj match {
			case hp :HibernateProxy => hp.getHibernateLazyInitializer.getImplementation
			case _ => obj
		}
		initialized
	}

	private def getSetter(entityType: Class[_], propertyName: String, returnType: Class[_]): Method = {
		val setterName: String = "set" + propertyName.substring(0, 1).toUpperCase + propertyName.substring(1)
		entityType.getDeclaredMethod(setterName, returnType)
	}

	@Around("execution(* no.officenet.example.rpm.pets..*(..)) && no.officenet.example.rpm.support.infrastructure.spring.aop.SystemArchitectureAspect.lazyLoadableJpaProperties()")
	def reattachSessionPETS(pjp: ProceedingJoinPoint): AnyRef = reattachSession(pjp, sessionFactoryPETS)

	@Around("!execution(* no.officenet.example.rpm.pets..*(..)) && no.officenet.example.rpm.support.infrastructure.spring.aop.SystemArchitectureAspect.lazyLoadableJpaProperties()")
	def reattachSessionRPM(pjp: ProceedingJoinPoint): AnyRef = reattachSession(pjp, sessionFactoryRPM)

	@Around("no.officenet.example.rpm.support.infrastructure.spring.aop.LazyInitAspect.lazyLoadableJpaProperties()")
	def reattachSession(pjp: ProceedingJoinPoint, sessionFactory: SessionFactory): AnyRef = {
		var obj = pjp.proceed
		// Don't try to re-init if already re-initing some stuff on the same thread.
		// This happens when the persistence-provider calls the getters when it's initing)
		if (!locked.get) {
			var reAttachNeeded: Boolean = false
			if (obj.isInstanceOf[AbstractPersistentCollection]) {
				val ps = obj.asInstanceOf[AbstractPersistentCollection]
				reAttachNeeded = !ps.wasInitialized && ps.getSession == null
			}
			if (obj.isInstanceOf[HibernateProxy]) {
				val li = (obj.asInstanceOf[HibernateProxy]).getHibernateLazyInitializer
				reAttachNeeded = li.isUninitialized && li.getSession == null
			}
			if (reAttachNeeded) {
				if (log.isTraceEnabled) {
					log.trace("Re-attaching Hibernate session to " + pjp.toString)
				}
				var session: Session = null
				locked.set(true)
				var newSession: Boolean = false
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

					val implementor = session.asInstanceOf[SessionImplementor]
					val pjpTarget = pjp.getTarget
					val entityType = pjpTarget.getClass
					val identifier = sessionFactory.getClassMetadata(entityType).getIdentifier(pjpTarget, implementor)
					val newInstance = getImplementation(session.load(entityType, identifier));

					pjp.getSignature match {
						case methodSignature: MethodSignature =>
							val getter = methodSignature.getMethod
							var wasAccessible = getter.isAccessible
							if (!wasAccessible) {
								getter.setAccessible(true)
							}
							obj = getImplementation(getter.invoke(newInstance));
							if (!wasAccessible) {
								getter.setAccessible(false)
							}
							val propertyName = getPropertyName(getter);
							val setter = getSetter(entityType, propertyName, getter.getReturnType);
							wasAccessible = setter.isAccessible
							if (!wasAccessible) {
								setter.setAccessible(true)
							}
							// Stuff the updated field in the original object
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
							obj = getImplementation(field.get(newInstance));
							// Stuff the updated field in the original object
							field.set(pjpTarget, obj)
							if (!wasAccessible) {
								field.setAccessible(false)
							}
						case signature =>
							throw new IllegalStateException("Unsupported signature-type: " + signature)
					}

				} finally {
					locked.set(false)
					if (session != null && newSession) {
						try {
							SessionFactoryUtils.closeSession(session)
						}
						catch {
							case t: Throwable => // Fall thru
						}
					}
				}
			}
		}
		obj
	}

}