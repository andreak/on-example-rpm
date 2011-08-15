package no.officenet.example.rpm.support.infrastructure.errorhandling

class ObjectNotFoundByPrimaryKeyException(entityName: String, primaryKeyValue: String)
	extends RuntimeException("Object of type: " + entityName + " with PK: " + primaryKeyValue + " not found") {

	

}