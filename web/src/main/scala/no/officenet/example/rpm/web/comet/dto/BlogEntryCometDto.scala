package no.officenet.example.rpm.web.comet.dto

import org.joda.time.DateTime
import collection.mutable.Buffer
import no.officenet.example.rpm.blog.domain.model.entities.BlogEntry
import scala.collection.JavaConversions._

case class BlogEntryCometDto(blogId: Long,
							 entityId: Long,
							 created: DateTime,
							 createdBy: UserCometDto,
							 modified: Option[DateTime],
							 modifiedBy: Option[UserCometDto],
							 title: String,
							 summary: String,
							 content: String,
							 comments: List[CommentCometDto]
								) {

	def addComment(comment: CommentCometDto): BlogEntryCometDto = {
		if (comment.parentId.isEmpty) {
			copy(comments = comments :+ comment)
		} else {
			copy(comments = appendCommentToParent(comment, comment.parentId.get))
		}
	}

	def replaceComment(comment: CommentCometDto): BlogEntryCometDto = {
		copy(comments = buildCommentList(comment))
	}

	private def findComment(comment: CommentCometDto, id: Long): Option[CommentCometDto] = {
		if (comment.id == id) {
			return Some(comment)
		}
		for (child <- comment.children) {
			findComment(child, id) match {
				case m @ Some(v) => return m
				case _ =>
			}
		}
		None
	}

	def findComment(id: Long): Option[CommentCometDto] = {
		for (comment <- comments) {
			findComment(comment, id) match {
				case m @ Some(parent) =>
					return m
				case _ =>
			}
		}
		None
	}

	private def populateRoot(comment: CommentCometDto): Option[CommentCometDto] = {
		comment.parentId.flatMap(pid => findComment(pid)).map{parent =>
			parent.copy(children = parent.children.map(child => {
				if (comment.id == child.id) {
					comment
				} else {
					child
				}
			}))
		}.map(populateRoot).getOrElse(Some(comment))
	}

	private def buildCommentList(newParent: CommentCometDto): List[CommentCometDto] = {
		// Recurse up to root and re-build parent's list of children on the way up
		// Then iterate over all root-nodes and inject the new root-node replacing the old
		val buffer = Buffer[CommentCometDto]()
		val root = populateRoot(newParent)
		for (rootParent <- root;
			 existingComment <- comments) {
			if (existingComment.id == rootParent.id) {
				buffer += rootParent
			} else {
				buffer += existingComment
			}
		}
		buffer.toList
	}

	private def appendCommentToParent(comment: CommentCometDto, parentId: Long): List[CommentCometDto] = {
		val parent = findComment(parentId)
		if (parent.isDefined) {
			val newParent = parent.map(p => p.copy(children = p.children :+ comment)).get
			// Build up new list, from root, with replaced parent-node with new comment added
			buildCommentList(newParent)
		} else {
			comments
		}
	}

	private[this] def printComments(comments: List[CommentCometDto]) {
		comments.foreach(comment => printComment(comment, 0))
		println("----------------")
	}

	private[this] def getIdent(level: Int): String = {
		val sb = new StringBuilder
		(1 to (level * 4)).foreach(v => sb.append(" "))
		sb.toString()
	}

	private[this] def printComment(comment: CommentCometDto, level: Int) {
		println(getIdent(level) + comment)
		comment.children.foreach(child => printComment(child, level + 1))
	}

}

object BlogEntryCometDto {
	def apply(blogEntry: BlogEntry): BlogEntryCometDto = {
		BlogEntryCometDto(
			blogId = blogEntry.blog.id,
			entityId = blogEntry.id,
			created = blogEntry.created,
			createdBy = UserCometDto(blogEntry.createdBy),
			modified = blogEntry.modified,
			modifiedBy = blogEntry.modifiedByOpt.map(UserCometDto(_)),
			title = blogEntry.title,
			summary = blogEntry.summary,
			content = blogEntry.content,
			comments = blogEntry.comments.map(CommentCometDto(_)).toList
		)
	}

}