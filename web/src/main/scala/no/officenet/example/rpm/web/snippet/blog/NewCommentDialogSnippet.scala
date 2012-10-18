package no.officenet.example.rpm.web.snippet.blog

import net.liftweb._
import http.js.{JsCmds, JsCmd}
import http.{RequestVar, TransientSnippet, IdMemoizeTransform, SHtml}
import util._
import util.Helpers._
import org.springframework.beans.factory.annotation.Configurable
import javax.annotation.Resource
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.blog.domain.service.CommentService
import no.officenet.example.rpm.blog.domain.model.entities.Comment
import no.officenet.example.rpm.web.lib.ContextVars.{CommentVar, CommentedEntityVar}
import no.officenet.example.rpm.blog.domain.model.entities.CommentJPAFields._
import no.officenet.example.rpm.web.lib.{LiftUtils, JpaFormFields, ValidatableScreen}
import no.officenet.example.rpm.support.infrastructure.scala.lang.ControlHelpers.?

object afterCommentSaveVar extends RequestVar[() => JsCmd](() => JsCmds.Noop)

@Configurable
class NewCommentDialogSnippet extends ValidatableScreen with JpaFormFields with TransientSnippet with Loggable {

	@Resource
	val commentService: CommentService = null

	val comment = CommentVar.get

	val afterSave = afterCommentSaveVar.get

	CommentedEntityVar.get.foreach(abstractEntity =>
		abstractEntity match {
			case cmnt: Comment =>
				comment.commentedId = cmnt.commentedId
				comment.parentOpt = Some(cmnt)
			case _ =>
				comment.commentedId = abstractEntity.id
		}
	)

	override protected def renderScreen() = {
		".inputContainer" #> SHtml.idMemoize {
			idMemoize =>
				".commentText" #> JpaTextAreaField(comment, commentText, ?(comment.commentText), (v: String) => comment.commentText = v) &
				":submit" #> SHtml.ajaxSubmit("Save", () => saveComment(idMemoize))
		}
	}

	private def saveComment(idMemoize: IdMemoizeTransform): JsCmd = {
		if (!hasErrors) {
			if (comment.id == null) {
				commentService.createComment(comment)
			} else {
				LiftUtils.getLoggedInUser.foreach(user => comment.modifiedByOpt = Some(user))
				commentService.updateComment(comment)
			}
			afterSave()
		} else {
			idMemoize.setHtml()
		}
	}

}