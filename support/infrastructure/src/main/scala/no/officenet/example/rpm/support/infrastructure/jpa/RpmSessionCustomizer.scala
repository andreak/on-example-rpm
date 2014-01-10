package no.officenet.example.rpm.support.infrastructure.jpa

import org.eclipse.persistence.config.SessionCustomizer
import org.eclipse.persistence.sessions.Session

class RpmSessionCustomizer extends SessionCustomizer{
	def customize(session: Session) {
		session.getPlatform.setShouldBindLiterals(false)
	}
}
