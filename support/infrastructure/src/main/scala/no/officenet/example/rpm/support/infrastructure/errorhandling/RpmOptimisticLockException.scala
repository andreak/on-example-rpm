package no.officenet.example.rpm.support.infrastructure.errorhandling

import javax.persistence.OptimisticLockException
import org.springframework.dao.DataAccessException

class RpmOptimisticLockException(cause: OptimisticLockException) extends DataAccessException(cause.getMessage, cause) with ApplicationException with Localizable {
	def getArguments: Seq[AnyRef] = Seq(cause.getEntity)

	def getResourceBundleName: String = "infrastructureErrorResources"

	def getResourceKey: String = "optimistic_lock_exception"

}