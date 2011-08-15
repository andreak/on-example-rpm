package no.officenet.example.rpm.pets.domain.service.repository

import org.springframework.stereotype.Repository
import no.officenet.example.rpm.support.infrastructure.jpa.GenericRepository
import no.officenet.example.rpm.support.domain.service.repository.PersistenceUnits
import no.officenet.example.rpm.pets.domain.model.entities.Pet

@Repository
class PetRepositoryJpa extends PetRepository with PersistenceUnits.PersistenceUnitPETS

trait PetRepository extends GenericRepository[Pet, java.lang.Long]