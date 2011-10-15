package no.officenet.example.rpm.support.infrastructure.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Aspect
public class LazyInitAspect implements InitializingBean {
	private static transient Log log = LogFactory.getLog(LazyInitAspect.class);

	private class ThreadLocaleInit<T> extends ThreadLocal<T> {
		protected T initVal;

		public ThreadLocaleInit(T initValue) {
			initVal = initValue;
		}

		@Override
		protected T initialValue() {
			return initVal;
		}
	}

	@PersistenceContext(unitName = "RPM")
	private EntityManager entityManagerRPM;
	@PersistenceContext(unitName = "PETS")
	private EntityManager entityManagerPETS;

	private SessionFactory sessionFactoryRPM;
	private SessionFactory sessionFactoryPETS;

	@Override
	public void afterPropertiesSet() throws Exception {
		sessionFactoryRPM = ((Session) entityManagerRPM.getDelegate()).getSessionFactory();
		sessionFactoryPETS = ((Session) entityManagerPETS.getDelegate()).getSessionFactory();
	}

	protected ThreadLocal<Boolean> locked = new ThreadLocaleInit<Boolean>(false);

	@Around("execution(* no.officenet.example.rpm.pets..*(..)) && no.officenet.example.rpm.support.infrastructure.spring.aop.SystemArchitectureAspect.lazyLoadableJpaProperties()")
	public Object reattachSessionPETS(ProceedingJoinPoint pjp) throws Throwable {
		return reattachSession(pjp, sessionFactoryPETS);
	}

	@Around("!execution(* no.officenet.example.rpm.pets..*(..)) && no.officenet.example.rpm.support.infrastructure.spring.aop.SystemArchitectureAspect.lazyLoadableJpaProperties()")
	public Object reattachSessionRPM(ProceedingJoinPoint pjp) throws Throwable {
		return reattachSession(pjp, sessionFactoryRPM);
	}

	public Object reattachSession(ProceedingJoinPoint pjp, SessionFactory sessionFactory) throws Throwable {
		Object obj = pjp.proceed();
		// Don't try to re-init if already re-initing some stuff on the same thread.
		// This happens when the persistence-provider calls the getters when it's initing)
		if (!locked.get()) {
			boolean reAttachNeeded = false;
			if (obj instanceof AbstractPersistentCollection) {
				AbstractPersistentCollection ps = (AbstractPersistentCollection) obj;
				reAttachNeeded = !ps.wasInitialized() && ps.getSession() == null;
			}

			if (obj instanceof HibernateProxy) {
				LazyInitializer li = ((HibernateProxy) obj).getHibernateLazyInitializer();
				reAttachNeeded = li.isUninitialized() && li.getSession() == null;
			}

			if (reAttachNeeded) {
				if (log.isTraceEnabled()) {
					log.trace("Re-attaching Hibernate session to " + pjp.toString());
				}
				Session session = null;
				locked.set(true);

				boolean newSession = false;
				try {
					try {
						session = sessionFactory.getCurrentSession();
					} catch (HibernateException e) {
						session = SessionFactoryUtils.getNewSession(sessionFactory);
						newSession = true;
					}

					SessionImplementor implementor = (SessionImplementor) session;

					Object pjpTarget = pjp.getTarget();
					Class entityType = pjpTarget.getClass();
					Serializable identifier = sessionFactory.getClassMetadata(entityType).getIdentifier(pjpTarget, implementor);

					Object newInstance = session.load(entityType, identifier);

					Signature signature = pjp.getSignature();
					if (signature instanceof MethodSignature) {
						MethodSignature methodSignature = (MethodSignature) signature;
						Method m = methodSignature.getMethod();
						boolean wasAccessible = m.isAccessible();
						if (!wasAccessible) {
							m.setAccessible(true);
						}
						obj = m.invoke(newInstance);
						if (!wasAccessible) {
							m.setAccessible(false);
						}
					} else if (signature instanceof FieldSignature) {
						// The instance will never be AbstractPersistentCollection because we've loaded an entity
						if (newInstance instanceof HibernateProxy) {
							newInstance = ((HibernateProxy)newInstance).getHibernateLazyInitializer().getImplementation();
						}
						FieldSignature fieldSignature = (FieldSignature) signature;
						Field field = fieldSignature.getField();
						boolean wasAccessible = field.isAccessible();
						if (!wasAccessible) {
							field.setAccessible(true);
						}
						obj = field.get(newInstance);
						// Stuff the updated field in the original object
						field.set(pjpTarget, obj);
						if (!wasAccessible) {
							field.setAccessible(false);
						}
					} else {
						throw new IllegalStateException("Unsupported signature-type: " + signature);
					}

					// Initialize the hole entity, else getters on primitives might throw exception after session-close
					Hibernate.initialize(obj);
				} finally {
					locked.set(false);
					if (session != null && newSession) {
						try {
							SessionFactoryUtils.closeSession(session);
						} catch (Throwable t) {
							// Fall thru
						}
					}
				}
			}
		}
		return obj;
	}
}
