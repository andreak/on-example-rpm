package no.officenet.example.rpm.support.infrastructure.spring

import java.util.Properties
import javax.transaction.{TransactionManager, Transaction}

class TransactionManagerLookup extends org.hibernate.transaction.TransactionManagerLookup {

	private var transactionManager: TransactionManager = _

	def getTransactionManager(props: Properties): TransactionManager = {
		if (transactionManager == null) {
			transactionManager = TransactionManagerHelper.getJtaTransactionManager.getTransactionManager
		}
		transactionManager
	}

	def getUserTransactionName: String = null

	def getTransactionIdentifier(transaction: Transaction) = transaction
}