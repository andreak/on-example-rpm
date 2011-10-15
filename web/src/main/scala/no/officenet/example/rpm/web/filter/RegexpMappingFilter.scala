package no.officenet.example.rpm.web.filter

/**
 * Copyright © OfficeNet AS
 */

import javax.servlet._
import http.{HttpServletRequest, HttpServletResponse}
import java.util.regex.Pattern
import org.apache.commons.lang.StringUtils.isBlank
import org.apache.commons.logging.LogFactory
import java.lang.IllegalArgumentException

class RegexpMappingFilter extends Filter {
	val log = LogFactory.getLog(getClass)

	private  var filterConfig: FilterConfig = _

	private var delegate: Filter = _

	var matchPattern: Pattern = _
	var matchURIList: List[Pattern] = Nil


	def destroy() {
		delegate.destroy()
	}

	def init(filterConfig: FilterConfig) {
		this.filterConfig = filterConfig;
		val filterClass = filterConfig.getInitParameter("filterClass")
		val matchPatternString = filterConfig.getInitParameter("matchPattern")
		val matchURIString = filterConfig.getInitParameter("matchURI")
		delegate = Class.forName(filterClass).newInstance().asInstanceOf[Filter]
		delegate.init(filterConfig);
		matchPattern = Pattern.compile(matchPatternString)
		if (matchURIString != null) {
			matchURIList = matchURIString.split("\\r|(\\r?\\n)").map(uriPattern =>
																		 Pattern.compile(uriPattern.trim())
			).toList
		}
	}

	def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
		doFilterHttp(request.asInstanceOf[HttpServletRequest], response.asInstanceOf[HttpServletResponse], chain)
	}

	def doFilterHttp(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
		val uri = request.getRequestURI
		val matcher = matchPattern.matcher(uri)
		if (matcher.find()) {
			val groupCount = matcher.groupCount()
			if (!(groupCount == 0 || groupCount == 2)) {
				throw new IllegalArgumentException("Number of regexp-groups in " + getClass.getName + " must be 0 or 2")
			}
			val baseGroup = if (groupCount == 2) matcher.group(1) else null
			val restGroup = if (groupCount == 2) matcher.group(2) else null
			log.trace("groupCount: " + groupCount + ", baseGroup: '" + baseGroup + "', restGroup: '" + restGroup + "'")
			if (matcher.matches() && (groupCount == 0 ||
									  (!isBlank(baseGroup) && isBlank(restGroup) || isBlank(baseGroup) && uriListMatches(restGroup)))) { // only start searching sub-filter if we have groups
				log.trace("delegating uri '" + uri + "' to filter: " + delegate.getClass.getName)
				delegate.doFilter(request, response, chain)
			} else {
				log.trace("passing thru uri '" + uri + "'")
				chain.doFilter(request, response)
			}
		} else {
			log.trace("passing thru uri '" + uri + "'")
			chain.doFilter(request, response)
		}
	}

	private def uriListMatches(uri: String): Boolean = {
		if (matchURIList.isEmpty) return true

		for (uriPattern <- matchURIList) {
			if (uriPattern.matcher(uri).matches()) return true
		}
		false
	}
}