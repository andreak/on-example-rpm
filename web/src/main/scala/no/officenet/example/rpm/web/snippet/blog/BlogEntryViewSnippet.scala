package no.officenet.example.rpm.web.snippet.blog

import net.liftweb.http._
import net.liftweb.util.Helpers._
import org.springframework.beans.factory.annotation.Configurable
import javax.annotation.Resource
import xml.NodeSeq
import no.officenet.example.rpm.web.menu.BlogEntryViewParam
import no.officenet.example.rpm.support.domain.service.UserService
import no.officenet.example.rpm.blog.domain.service.BlogEntryService
import no.officenet.example.rpm.web.lib.LiftUtils

@Configurable
class BlogEntryViewSnippet(blogEntryViewParam: BlogEntryViewParam) {

	@Resource
	private val blogEntryService: BlogEntryService = null
	@Resource
	private val personService: UserService = null

	lazy val blogUserOpt = personService.findByUserName(blogEntryViewParam.userName)

	private def entryBox = tryo {
		blogEntryService.retrieve(blogEntryViewParam.blogEntryId)
	}

	def render = {
		".blogEntryHeaderContainer" #> (
			".userName" #> LiftUtils.getLoggedInUser.map(_.userName)
			) &
			".blogEntryCometContainer" #> ((ns: NodeSeq) => {
				blogUserOpt.map {
					blogUser =>
						entryBox.collect {
							case entry if entry.createdBy.userName == blogUser.userName =>
								val cometName = List(S.locale, blogEntryViewParam.blogEntryId)
								<div class={"lift:comet?type=BlogEntryDetailCometActor;name=" + cometName.mkString(":")}
									 style="display: inline;">
									{ns}
								</div>
						} openOr {
							<div>Blog-entry does not exist</div>
						}
				}.getOrElse(<div>User
					{blogEntryViewParam.userName}
					does not exist</div>)
			})
	}
}