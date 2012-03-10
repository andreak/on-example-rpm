package no.officenet.example.rpm.projectmgmt.domain.service.repository

import no.officenet.example.rpm.projectmgmt.domain.model.entities.Activity
import no.officenet.example.rpm.support.infrastructure.jpa.{PersistenceUnits, GenericRepository}
import org.springframework.stereotype.Repository

@Repository
class ActivityRepositoryJpa extends ActivityRepository with PersistenceUnits.PersistenceUnitRPM

trait ActivityRepository extends GenericRepository[Activity, java.lang.Long] {
	
}