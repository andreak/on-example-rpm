package no.officenet.example.rpm.web.security

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.security.web.savedrequest.HttpSessionRequestCache

class FilteredRequestCache extends HttpSessionRequestCache {

	override def saveRequest(request: HttpServletRequest ,response:  HttpServletResponse) {
		if (matches(request)) {
			super.saveRequest(request, response)
		}
	}

	protected def matches(request: HttpServletRequest): Boolean = {
		val servletPath = request.getServletPath
		if (servletPath.startsWith("/ajax_request") ||
			servletPath.startsWith("/comet_request") ||
			servletPath.startsWith("/classpath") ||
			servletPath.startsWith("/resources")
			) {
			if (logger.isTraceEnabled) {
				logger.trace("servletPath '" + servletPath + "' not qualified for redirect.")
			}
			false
		} else {
			if (logger.isTraceEnabled) {
				logger.trace("servletPath '" + servletPath + "' qualified for redirect, saving.")
			}
			true
		}
	}
}
