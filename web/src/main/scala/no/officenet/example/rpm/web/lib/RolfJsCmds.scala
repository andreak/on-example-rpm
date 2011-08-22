package no.officenet.example.rpm.web.lib

import net.liftweb._
import common.Box
import http._
import util.Helpers._
import js._
import js.jquery.JqJE
import js.JsCmds._
import xml.NodeSeq

object RolfJqJE {

	case class JqInsertAtCaret(text: String) extends JsExp with JsMember {
		override val toJsCmd = "insertAtCaret(" + text.encJs + ")"
	}
	case class JqReplaceWith(content: NodeSeq) extends JsExp with JsMember {
		override val toJsCmd = "replaceWith(" + fixHtmlFunc ("inline", content)(str => str) + ")"
	}
}

object RolfJsCmds{
	object JqInsertAtCaret {
		def apply(uid: String, text: String): JsCmd =
			(JqJE.JqId(JE.Str(uid)) ~> RolfJqJE.JqInsertAtCaret(text))
	}

	object JqReplaceWith {
		def apply(uid: String, content: NodeSeq): JsCmd =
			(JqJE.JqId(JE.Str(uid)) ~> RolfJqJE.JqReplaceWith(content))
	}
}