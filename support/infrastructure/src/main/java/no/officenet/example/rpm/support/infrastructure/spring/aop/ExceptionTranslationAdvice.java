package no.officenet.example.rpm.support.infrastructure.spring.aop;

import no.officenet.example.rpm.support.infrastructure.errorhandling.AccessDeniedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionTranslationAdvice implements Ordered {

	@Around("no.officenet.example.rpm.support.infrastructure.spring.aop.SystemArchitectureAspect.allServices()")
	public Object doTranslation(ProceedingJoinPoint pjp) throws Throwable {
		try {
			return pjp.proceed();
		} catch (Throwable t) {
			Throwable exceptionToThrow = t;
			if (t instanceof org.springframework.security.access.AccessDeniedException) {
				exceptionToThrow = new AccessDeniedException(t);
			}
			throw exceptionToThrow;
		}
	}

	@Override
	public int getOrder() {
		return 4; // Lower than tx-aspect means "before" tx-aspect executes.
	}
}
