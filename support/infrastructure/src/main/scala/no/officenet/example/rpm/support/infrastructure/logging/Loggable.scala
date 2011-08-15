package no.officenet.example.rpm.support.infrastructure.logging

import org.apache.commons.logging.{Log, LogFactory}

trait Loggable {
	def log: Log = LogFactory.getLog(getClass)

	def trace(msg: => Any) {
		try {
			if (log.isTraceEnabled) log.trace(msg)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

	def trace(msg: => Any, throwable: Throwable) {
		try {
			if (log.isTraceEnabled) log.trace(msg, throwable)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

	def debug(msg: => Any) {
		try {
			if (log.isDebugEnabled) log.debug(msg)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

	def debug(msg: => Any, throwable: Throwable) {
		try {
			if (log.isDebugEnabled) log.debug(msg, throwable)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

	def info(msg: => Any) {
		try {
			if (log.isInfoEnabled) log.info(msg)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

	def info(msg: => Any, throwable: Throwable) {
		try {
			if (log.isInfoEnabled) log.info(msg, throwable)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

	def warn(msg: => Any) {
		try {
			if (log.isWarnEnabled) log.warn(msg)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

	def warn(msg: => Any, throwable: Throwable) {
		try {
			if (log.isWarnEnabled) log.warn(msg, throwable)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

	def error(msg: => Any) {
		try {
			if (log.isErrorEnabled) log.error(msg)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

	def error(msg: => Any, throwable: Throwable) {
		try {
			if (log.isErrorEnabled) log.error(msg, throwable)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

	def fatal(msg: => Any) {
		try {
			if (log.isFatalEnabled) log.fatal(msg)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

	def fatal(msg: => Any, throwable: Throwable) {
		try {
			if (log.isFatalEnabled) log.fatal(msg, throwable)
		}
		catch {
			case t => log.error("Exception while logging: " + t.getMessage, t)
		}
	}

}
