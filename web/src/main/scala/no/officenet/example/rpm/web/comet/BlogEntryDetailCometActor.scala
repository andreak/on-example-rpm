package no.officenet.example.rpm.web.comet

import dto.{CommentCometDto, BlogEntryCometDto}
import net.liftweb._
import http._
import server.{AddMinusVoteMessage, AddPlusVoteMessage, RemoveVoteMessage, VoteRemovedMessage, VotesUpdatedMessage, CommentUpdatedMessage, CommentCreatedMessage, BlogEntryUpdatedMessage, BlogEntryServerCreatedMessage, BlogEntryMasterServer}
import util._
import util.Helpers._
import common.{Empty, Box, Full}
import http.js._
import http.js.JE._
import http.js.JsCmds._
import http.js.jquery.JqJsCmds._
import http.js.jquery.JqJE
import xml.{Text, NodeSeq}
import javax.annotation.Resource
import org.springframework.beans.factory.annotation.Configurable
import no.officenet.example.rpm.blog.domain.service.{CommentService, BlogEntryService}
import no.officenet.example.rpm.blog.domain.model.entities.Comment
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer._
import no.officenet.example.rpm.support.infrastructure.i18n.GlobalTexts
import no.officenet.example.rpm.web.lib.RolfJsCmds._
import no.officenet.example.rpm.support.domain.model.entities.User
import no.officenet.example.rpm.web.lib.ContextVars._
import no.officenet.example.rpm.web.snippet.blog.{afterBlogEntrySaveVar, afterCommentSaveVar}
import no.officenet.example.rpm.web.lib.{BlogHelper, CloseDialog, LiftUtils, JQueryDialog, AjaxButton}

@Configurable
class BlogEntryDetailCometActor extends RpmCometActor {

	lazy val blogEntryIdent = nameParts(1).toLong

	val newBlogPostTemplate = "lift/blog/_newBlogPostDialog"

	@Resource
	private val blogEntryService: BlogEntryService = null
	@Resource
	private val commentService: CommentService = null

	val replyTemplate = "lift/blog/_newCommentDialog"
	var cachedBlogEntry: Box[BlogEntryCometDto] = Empty
	private var voteMap = Map[Long, Int]()

	lazy val blogEntryServer = BlogEntryMasterServer.registerWithServer(blogEntryIdent)

	protected def registerWith = blogEntryServer

	var blogEntryTemplate: NodeSeq = NodeSeq.Empty
	var commentTemplate: NodeSeq = NodeSeq.Empty
	var voteTemplate: NodeSeq = NodeSeq.Empty
	var myVotedComments: Map[Long, Boolean] = Map.empty // Holds the commentId -> voteValue which I've voted for

	override def lowPriority = {
		case BlogEntryServerCreatedMessage(entry, vm) =>
			cachedBlogEntry = entry
			voteMap = vm
			loggedOnUser.foreach(initMyVotedComments(_))
			reRender()
		case BlogEntryUpdatedMessage(blogEntry) =>
			cachedBlogEntry = Full(blogEntry)
			val domId = ("blogEntry_" + blogEntry.entityId)
			partialUpdate(
				Replace(domId, blogEntryTransform(blogEntry).apply(blogEntryTemplate)) &
					Call("SyntaxHighlighter.highlight").cmd &
					Hide(domId) & FadeIn(domId, 0 seconds, JsRules.fadeTime)
			)
		case (entry: Box[BlogEntryCometDto], CommentCreatedMessage(comment, vm)) =>
			vm.foreach(this.voteMap = _)
			cachedBlogEntry = entry
			def showComment: JsCmd = {
				val domId = "commentContainer_" + comment.id
				Hide(domId) & SlideDown(domId, "slow")
			}
			if (comment.parentId.isEmpty) {
				partialUpdate(
					RolfAppendHtml("blogEntryComments", printComment(comment).apply(commentTemplate)) &
						Call("SyntaxHighlighter.highlight").cmd &
						showComment
				)
			} else {
				val parentId = "commentContainer_" + comment.parentId.get
				partialUpdate(
					RolfAppendHtml(parentId, printComment(comment).apply(commentTemplate)) &
						Call("SyntaxHighlighter.highlight").cmd &
						showComment
				)
			}
		case (entry: Box[BlogEntryCometDto], CommentUpdatedMessage(comment)) =>
			cachedBlogEntry = entry
			val commentID = "comment_" + comment.id
			partialUpdate(
				Replace(commentID, printCommentContent(comment).apply((".comment ^^" #> "ignore").apply(commentTemplate))) &
					Call("SyntaxHighlighter.highlight").cmd &
					Hide(commentID) & FadeIn(commentID, 0 seconds, JsRules.fadeTime)
			)
		case VotesUpdatedMessage(commentId, vm, userId, voteValue) =>
			this.voteMap = vm
			val isMyVote = loggedOnUser.map(_.id.longValue() == userId).openOr(false)
			if (isMyVote) {
				myVotedComments += commentId -> voteValue
			}
			for (votesForComment <- voteMap.get(commentId)) {
				val voteId = "comment_votes_" + commentId
				val updateVoteJS = if (isMyVote) {
					val commentID = "comment_" + commentId
					(JqJE.Jq(JE.Str("#" + commentID + " .voteContainer .voteOperationContainer")) ~> JqJE.JqReplace(Helpers.stripHead(processVote(commentId).apply(voteTemplate)))).cmd
				} else Noop
				partialUpdate(
					SetHtml(voteId, Text(votesForComment.toString)) & updateVoteJS
				)
			}
		case VoteRemovedMessage(commentId, userId, voteValue, vm) =>
			vm.foreach(this.voteMap = _)
			val isMyVote = loggedOnUser.map(_.id.longValue() == userId).openOr(false)
			if (isMyVote) {
				myVotedComments -= commentId
			}
			for (votesForComment <- voteMap.get(commentId)) {
				val voteId = "comment_votes_" + commentId
				val updateVoteJS = if (isMyVote) {
					val commentID = "comment_" + commentId
					(JqJE.Jq(JE.Str("#" + commentID + " .voteContainer .voteOperationContainer")) ~> JqJE.JqReplace(Helpers.stripHead(processVote(commentId).apply(voteTemplate)))).cmd
				} else Noop
				partialUpdate(
					SetHtml(voteId, Text(votesForComment.toString)) & updateVoteJS
				)
			}
	}

	private[this] def initMyVotedComments(user: User) {
		myVotedComments = commentService.findVotesForPerson(blogEntryIdent, user.id)
	}

	def render = {
		val retval = cachedBlogEntry.map {
			entry =>
				".blogEntry" #> ((ns: NodeSeq) => {
					blogEntryTemplate = ns
					blogEntryTransform(entry)
				}.apply(ns)) &
					".comments" #> (
						".commentContainer" #> ((ns: NodeSeq) => {
							commentTemplate = ns
							"*" #> entry.comments.collect {
								case comment if comment.parentId.isEmpty => printComment(comment)
							}
						}.apply(ns) ++ Script(Call("SyntaxHighlighter.highlight").cmd))
						)
		}.openOr(ClearNodes)
		retval
	}

	private def blogEntryTransform(entry: BlogEntryCometDto) = {
		".blogEntry [id]" #> ("blogEntry_" + entry.entityId) &
		".imageIconPath [src]" #> entry.createdBy.imageIconPath.getOrElse(BlogHelper.anonymousImage) &
		".imageIconPath [alt]" #> entry.createdBy.userName &
		".imageIconPath [title]" #> entry.createdBy.displayName &
		".title *" #> entry.title &
		".summary *" #> LiftUtils.renderWiki(entry.summary) &
		".blogContent *" #> LiftUtils.renderWiki(entry.content) &
		".replyToBlogEntry" #> loggedOnUser.map(user => AjaxButton("Reply", () => replyToBlogEntry(entry, user)).toNodeSeq) &
		".editBlogEntry" #> loggedOnUser.filter(user => entry.createdBy.userName == user.userName).
			map(v => AjaxButton("Edit blog-post", () => editBlogEntry).toNodeSeq)
	}

	private def getChildComments(comment: CommentCometDto, printComment: (CommentCometDto) => CssSel, ns: NodeSeq): NodeSeq = {
		comment.children.toList.map(printComment(_).apply(ns)).foldLeft(NodeSeq.Empty)(_ ++ _)
	}

	private def replyToComment(comment: CommentCometDto, user: User): JsCmd = {
		CommentedEntityVar.set(Full(commentService.retrieve(comment.id)))
		CommentVar.set(new Comment(user))
		val dialogId = nextFuncName
		afterCommentSaveVar.set(() => CloseDialog(dialogId))
		JQueryDialog(replyTemplate, "Reply to comment", dialogId).open
	}

	private def replyToBlogEntry(entry: BlogEntryCometDto, user: User): JsCmd = {
		CommentedEntityVar.set(Full(blogEntryService.retrieve(entry.entityId)))
		CommentVar.set(new Comment(user))
		val dialogId = nextFuncName
		afterCommentSaveVar.set(() => CloseDialog(dialogId))
		JQueryDialog(replyTemplate, "replyToBlogEntry", dialogId).open
	}

	def editBlogEntry: JsCmd = {
		cachedBlogEntry.foreach(entry => BlogEntryVar.set(blogEntryService.retrieve(entry.entityId)))
		val dialogId = nextFuncName
		afterBlogEntrySaveVar.set(() => CloseDialog(dialogId))
		JQueryDialog(newBlogPostTemplate, "Edit blog-entry", dialogId).open
	}

	private def editComment(isCommentToBlogPost: Boolean, comment: CommentCometDto): JsCmd = {
		for (entry <- cachedBlogEntry) {
			if (isCommentToBlogPost) {
				CommentedEntityVar.set(Full(blogEntryService.retrieve(entry.entityId)))
			} else {
				for (parentId <- comment.parentId) {
					CommentedEntityVar.set(Full(commentService.retrieve(parentId)))
				}
			}
		}
		CommentVar.set(commentService.retrieve(comment.id))
		val dialogId = nextFuncName
		afterCommentSaveVar.set(() => CloseDialog(dialogId))
		JQueryDialog(replyTemplate, "Edit comment", dialogId).open
	}

	private def attatchVoteJS(commentId: Long): JsCmd = {
		val voteValue = myVotedComments.get(commentId).getOrElse(false)
		Call("Rolf.Blog.prepareVoteBox", commentId, myVotedComments.contains(commentId), voteValue).cmd
	}

	private def attatchVoteJSSeq(commentId: Long): NodeSeq = {
		Script(attatchVoteJS(commentId))
	}

	private def processVote(commentId: Long) = {
		".voteOperationContainer" #> ((ns: NodeSeq) => (
			"*" #> loggedOnUser.map(user=>
			".removeVote [class+]" #> Full("disabled").filterNot(_ => myVotedComments.contains(commentId)) &
				".removeVote *" #> ((ns: NodeSeq) =>
					if (myVotedComments.contains(commentId)) {
						SHtml.a(() => removeVote(commentId, user), ns)
					} else {
						ns
					}) &
				".addPlusVote [class+]" #> Full("disabled").filter(_ => myVotedComments.contains(commentId)) &
				".addPlusVote *" #> ((ns: NodeSeq) =>
					if (!myVotedComments.contains(commentId)) {
						SHtml.a(() => addPlusVote(commentId, user), ns)
					} else {
						ns
					}) &
				".addMinusVote [class+]" #> Full("disabled").filter(_ => myVotedComments.contains(commentId)) &
				".addMinusVote *" #> ((ns: NodeSeq) =>
					if (!myVotedComments.contains(commentId)) {
						SHtml.a(() => addMinusVote(commentId, user), ns)
					} else {
						ns
					})
			)).apply(ns) ++ attatchVoteJSSeq(commentId))
	}

	private def printCommentContent(comment: CommentCometDto): CssSel = {
		".comment [id]" #> ("comment_" + comment.id) &
			".voteContainer" #> (
				".numberOfVotes [id]" #> ("comment_votes_" + comment.id) &
					".numberOfVotes *" #> voteMap.get(comment.id) &
					".voteOperationContainer" #> ((ns: NodeSeq) => {
						voteTemplate = ns
						processVote(comment.id).apply(voteTemplate)
					})
				) &
			".imageIconPath [src]" #> comment.createdBy.imageIconPath.getOrElse(BlogHelper.anonymousImage) &
			".imageIconPath [alt]" #> comment.createdBy.userName &
			".imageIconPath [title]" #> comment.createdBy.displayName &
			".created *" #> formatDateTime(L(GlobalTexts.dateformat_fullDateTimeSeconds), Some(comment.created), cometActorLocale) &
			".createdBy *" #> comment.createdBy.displayName &
			".commentText *" #> LiftUtils.renderWiki(comment.commentText) &
			".replyContainer" #> (
				".replyLink" #> loggedOnUser.map(user => AjaxButton("Reply", () => replyToComment(comment, user)).toNodeSeq).
					openOr(SHtml.button("Reply", () => (), "disabled" -> "disabled", "title" -> "You must log in to comment"))&
				".editLink" #> loggedOnUser.filter(user => comment.createdBy.userName == user.userName).
					map(v => AjaxButton("Edit comment", () => editComment(comment.parentId.isEmpty, comment)).toNodeSeq)
				)
	}

	private def removeVote(commentId: Long, loggedOnUser: User): JsCmd = {
		blogEntryServer ! RemoveVoteMessage(commentId, loggedOnUser.id)
		Noop
	}

	private def addPlusVote(commentId: Long, loggedOnUser: User): JsCmd = {
		blogEntryServer ! AddPlusVoteMessage(commentId, loggedOnUser.id)
		Noop
	}

	private def addMinusVote(commentId: Long, loggedOnUser: User): JsCmd = {
		blogEntryServer ! AddMinusVoteMessage(commentId, loggedOnUser.id)
		Noop
	}

	private def printComment(comment: CommentCometDto): CssSel = {
		".commentContainer" #> ((ns: NodeSeq) => {
			".commentContainer [id]" #> ("commentContainer_" + comment.id) &
				".comment" #> ((seq: NodeSeq) => (printCommentContent(comment)).apply(seq) ++
					getChildComments(comment, printComment _, ns))
		}.apply(ns))
	}

}