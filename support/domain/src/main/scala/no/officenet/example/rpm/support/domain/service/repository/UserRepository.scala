package no.officenet.example.rpm.support.domain.service.repository

import org.springframework.stereotype.Repository
import no.officenet.example.rpm.support.domain.model.entities.User
import no.officenet.example.rpm.support.infrastructure.jpa.GenericRepository

@Repository
class UserRepositoryJpa extends UserRepository with PersistenceUnits.PersistenceUnitRPM

trait UserRepository extends GenericRepository[User, java.lang.Long] {

	def findByUserName(userName: String) = {
		entityManager.createQuery[User]("SELECT u FROM User u WHERE u.userName = :userName")
			.setParams("userName" -> userName)
			.findOne
	}

	def search(userSearchQuery: String) = {
		entityManager.createQuery[User]("""SELECT u FROM User u WHERE lower(u.userName) LIKE :query
										 ORDER BY u.firstName ASC, u.lastName ASC, u.userName ASC""")
			.setParams("query" -> ("%" + userSearchQuery.toLowerCase + "%"))
			.getResultList()
	}

	override def findAll(implicit m: Manifest[User]) = {
		entityManager.createQuery[User]("SELECT u FROM User u ORDER BY u.firstName ASC, u.lastName ASC, u.userName ASC")
			.getResultList()
	}
}