package no.officenet.example.rpm.support.infrastructure.spring.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class SystemArchitectureAspect {

	@Pointcut("(@within(javax.persistence.Entity) || @within(javax.persistence.MappedSuperclass))" +
			  " && (" +
			  "@annotation(javax.persistence.ManyToOne)" +
			  " || @annotation(javax.persistence.ManyToMany)" +
			  " || @annotation(javax.persistence.OneToMany)" +
			  " || @annotation(javax.persistence.OneToOne)" +
			  ")")
	public void lazyLoadableJpaProperties() {
	}

	@Pointcut("execution(* no.officenet.example.rpm..*(..))")
	public void allMethods() {
	}

	@Pointcut("applicationServices() || domainServices() || repositories()")
	public void allServices() {
	}

	@Pointcut("@within(no.officenet.example.rpm.support.infrastructure.spring.AppService)")
	public void applicationServices() {
	}

	@Pointcut("@within(org.springframework.stereotype.Service)")
	public void domainServices() {
	}

	@Pointcut("@within(org.springframework.stereotype.Repository)")
	public void repositories() {
	}

}
