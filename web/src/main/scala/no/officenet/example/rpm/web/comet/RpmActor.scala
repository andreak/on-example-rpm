package no.officenet.example.rpm.web.comet

import net.liftweb.actor.LiftActor
import net.liftweb.common.CommonLoanWrapper
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.web.errorhandling.ExceptionHandlerDelegate

trait RpmActor extends LiftActor with Loggable {

	override protected def aroundLoans: List[CommonLoanWrapper] = {
		val lw = LoanWrapperHelper.getLoanWrapper(() => None)
		lw :: Nil
	}


	override protected def exceptionHandler = {
		case ex => ExceptionHandlerDelegate.handleException(log, ex)
	}

}