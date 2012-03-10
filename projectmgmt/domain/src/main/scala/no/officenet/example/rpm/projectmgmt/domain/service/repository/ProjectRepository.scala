package no.officenet.example.rpm.projectmgmt.domain.service.repository

import no.officenet.example.rpm.support.infrastructure.jpa.{PersistenceUnits, GenericRepository}
import org.springframework.stereotype.Repository
import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project

@Repository
class ProjectRepositoryJpa extends ProjectRepository with PersistenceUnits.PersistenceUnitRPM

trait ProjectRepository extends GenericRepository[Project, java.lang.Long] {
	
}