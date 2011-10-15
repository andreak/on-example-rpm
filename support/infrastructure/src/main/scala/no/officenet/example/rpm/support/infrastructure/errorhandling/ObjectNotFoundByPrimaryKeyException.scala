package no.officenet.example.rpm.support.infrastructure.errorhandling

class ObjectNotFoundByPrimaryKeyException(entityName: String, primaryKeyValue: String)
	extends AbstractInfrastructureApplicationException("object_not_found_by_pk", entityName, primaryKeyValue) {

	

}