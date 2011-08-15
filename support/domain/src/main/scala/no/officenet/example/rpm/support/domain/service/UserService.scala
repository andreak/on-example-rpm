package no.officenet.example.rpm.support.domain.service

import repository.UserRepository
import javax.annotation.Resource
import no.officenet.example.rpm.support.domain.model.entities.User
import org.apache.commons.lang.StringUtils
import org.jasypt.util.password.StrongPasswordEncryptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserServiceImpl @Autowired() (val userRepository: UserRepository) extends UserService

trait UserService extends GenericDomainService[User] {

	val userRepository: UserRepository

	repository = userRepository

	@Resource
	val passwordEncryptor: StrongPasswordEncryptor = null

	override def create(user: User) = {
		val plainTextPassword = user.plainTextPassword
		if (!StringUtils.isBlank(plainTextPassword)) {
			val encryptedPassword = passwordEncryptor.encryptPassword(plainTextPassword)
			user.password = encryptedPassword
		}
		val savedUser = super.create(user)
		savedUser
	}

	def findByUserName(userName: String) = userRepository.findByUserName(userName)
}