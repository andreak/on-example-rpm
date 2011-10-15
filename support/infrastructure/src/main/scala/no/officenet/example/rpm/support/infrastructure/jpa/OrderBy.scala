package no.officenet.example.rpm.support.infrastructure.jpa


case class OrderBy(field: javax.persistence.metamodel.Attribute[_, _], order: Order.OrderType) {
	def fieldName = field.getName
}