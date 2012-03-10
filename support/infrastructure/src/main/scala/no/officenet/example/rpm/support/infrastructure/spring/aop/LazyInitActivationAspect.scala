package no.officenet.example.rpm.support.infrastructure.spring.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Aspect, Around}

/**
 * Aspect which controls activation of the LazyInitAspect.
 * The LazyInitAspect tests the value in {@link LazyInitState#lazyInit} and tuns of LazyInit'ing if the value is
 * <code>false</code>, typically in the repository-layer (@Repository).
 * <p/>
 * This aspect applies to {@link org.springframework.stereotype.Repository} annotated classes.
 */
@Aspect
class LazyInitActivationAspect {
	private final val threadState = new ThreadLocal[State]

	@Around("@within(org.springframework.stereotype.Repository)")
	def wrap(pjp: ProceedingJoinPoint): AnyRef = {
		var state = threadState.get
		if (state == null) {
			state = new State
			threadState.set(state)
		}
		try {
			if (state.depth == 0) {
				LazyInitState.lazyInit.set(false)
			}
			state.increment()
			pjp.proceed
		}
		finally {
			state.decrement()
			if (state.depth == 0) {
				LazyInitState.lazyInit.set(true)
			}
		}
	}

	private class State {
		private[aop] def decrement() {
			depth -= 1
		}

		private[aop] def increment() {
			depth += 1
		}

		private[aop] var depth: Int = 0
	}

}
