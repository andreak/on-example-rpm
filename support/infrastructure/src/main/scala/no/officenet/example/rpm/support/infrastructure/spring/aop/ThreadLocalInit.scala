package no.officenet.example.rpm.support.infrastructure.spring.aop

class ThreadLocalInit[T](initValue: T) extends ThreadLocal[T] {
	protected override def initialValue: T = initValue
}
