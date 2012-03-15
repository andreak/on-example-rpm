package no.officenet.example.rpm.web.comet

import dto.BlogEntrySummaryCometDto
import net.liftweb._
import http.js.JE.Call
import http.js.jquery.JqJsCmds._
import http.js.JsCmds.{Replace, Script}
import http.js.JsRules
import http.{SHtml, S}
import net.liftweb.util.Helpers._

import org.springframework.beans.factory.annotation.Configurable
import server.{BlogEntryCommentAddedMessage, BlogEntryUpdatedMessage, BlogEntryCreatedMessage, BlogCometListenerAddedMessage, BlogMasterServer}
import xml.{Text, NodeSeq}
import no.officenet.example.rpm.web.lib.RolfJsCmds.SlideDown
import no.officenet.example.rpm.web.menu.{BlogEntryViewLoc, BlogEntryViewParam}
import no.officenet.example.rpm.support.infrastructure.i18n.GlobalTexts
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer._
import no.officenet.example.rpm.web.lib.{BlogHelper, LiftUtils}

@Configurable
class BlogCometActor extends RpmCometActor {

	lazy val blogIdent = nameParts(1).toLong

	lazy val blogServer = BlogMasterServer.registerWithServer(blogIdent)

	override protected def registerWith = blogServer

	private var cachedBlogEntryList: List[BlogEntrySummaryCometDto] = Nil

	private var blogEntrySummaryTemplate: NodeSeq = NodeSeq.Empty

	override def lowPriority = {
		case BlogCometListenerAddedMessage(blogEntrySummaryList) =>
			cachedBlogEntryList = blogEntrySummaryList
			reRender()
		case (blogEntrySummaryList: List[BlogEntrySummaryCometDto], BlogEntryCreatedMessage(blogEntry)) =>
			cachedBlogEntryList = blogEntrySummaryList
			val blogEntryDomID = ("blogEntry_" + blogEntry.entityId)
			partialUpdate(
				PrependHtml("futureBlogPostContainer", renderBlogEntry(BlogEntrySummaryCometDto(blogEntry), blogEntrySummaryTemplate)) &
					Call("SyntaxHighlighter.highlight") &
					Hide(blogEntryDomID) & SlideDown(blogEntryDomID, "slow")

			)
		case (blogEntrySummaryList: List[BlogEntrySummaryCometDto], BlogEntryUpdatedMessage(blogEntry)) =>
			cachedBlogEntryList = blogEntrySummaryList
			val blogEntryDomID = ("blogEntry_" + blogEntry.entityId)
			partialUpdate(
				Replace(blogEntryDomID, renderBlogEntry(BlogEntrySummaryCometDto(blogEntry), blogEntrySummaryTemplate)) &
					Call("SyntaxHighlighter.highlight") &
					Hide(blogEntryDomID) & FadeIn(blogEntryDomID, 0 seconds, JsRules.fadeTime)
			)
		case (blogEntrySummaryList: List[BlogEntrySummaryCometDto], BlogEntryCommentAddedMessage(blogEntry)) =>
			cachedBlogEntryList = blogEntrySummaryList
			val blogEntryDomID = ("blogEntry_" + blogEntry.entityId)
			partialUpdate(
				Replace(blogEntryDomID, renderBlogEntry(BlogEntrySummaryCometDto(blogEntry), blogEntrySummaryTemplate)) &
					Call("SyntaxHighlighter.highlight")
			)
	}

	override def render = {
		"*" #> ((ns: NodeSeq) =>
			renderBlogEntrySummary.apply(ns) ++ Script(Call("SyntaxHighlighter.highlight"))
			)
	}

	def renderBlogEntry(entry: BlogEntrySummaryCometDto, ns: NodeSeq): NodeSeq = {
		(".blogEntry [id]" #> ("blogEntry_" + entry.entityId) &
			".imageIconPath [src]" #> entry.createdBy.imageIconPath.getOrElse(BlogHelper.anonymousImage) &
			".imageIconPath [alt]" #> entry.createdBy.userName &
			".imageIconPath [title]" #> entry.createdBy.displayName &
			".createdBy *" #> entry.createdBy.displayName &
			".created *" #> formatDateTime(L(GlobalTexts.dateformat_fullDateTimeSeconds), Some(entry.created), S.locale) &
			".title *" #> entry.title &
			".summary *" #> LiftUtils.renderWiki(entry.summary) &
			".numComments" #> entry.numComments &
			".readMoreLink" #> SHtml.link(BlogEntryViewLoc.createLink(BlogEntryViewParam(entry.createdBy.userName, entry.entityId)).
				get.text, () => (), Text("Read more"))
			).apply(ns)
	}

	private def renderBlogEntrySummary = {
		".blogEntry" #> ((ns: NodeSeq) => {
			blogEntrySummaryTemplate = ns
			("*" #> cachedBlogEntryList.map {
				blogEntryId =>
					renderBlogEntry(blogEntryId, ns)
			}).apply(ns)
		})
	}

}
