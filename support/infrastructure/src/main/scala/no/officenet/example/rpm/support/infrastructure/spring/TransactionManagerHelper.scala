package no.officenet.example.rpm.support.infrastructure.spring

import org.springframework.transaction.jta.JtaTransactionManager
import javax.annotation.Resource

object TransactionManagerHelper {

	@Resource
	private val jtaTransactionManager: JtaTransactionManager = null

	def getJtaTransactionManager = jtaTransactionManager

	def getInstance = this
}