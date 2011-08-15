package no.officenet.example.rpm.support.infrastructure.scala.lang

import collection.mutable.HashMap

class IdentityHashMap[K,V] extends HashMap[K,V] {

	override protected def elemEquals(key1: K, key2: K) =  {
		if (key1.isInstanceOf[AnyRef] && key2.isInstanceOf[AnyRef]) {
			val object1 = key1.asInstanceOf[AnyRef]
			val object2 = key2.asInstanceOf[AnyRef]
			object1 eq object2
		} else {
			key1 == key2
		}
	}

	override protected def elemHashCode(key: K) = System.identityHashCode(key.asInstanceOf[AnyRef])
}