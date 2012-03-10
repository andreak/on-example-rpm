package no.officenet.example.rpm.support.infrastructure.spring.aop

object LazyInitState {
	final val lazyInit = new ThreadLocalInit[Boolean](true);
}
