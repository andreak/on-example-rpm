package no.officenet.example.rpm.web.lib

import net.liftweb._
import common.Box
import http._
import js.JE._
import util.Helpers._
import js._
import js.jquery.JqJE
import js.JsCmds._
import js.jquery.JqJE._
import xml.NodeSeq

object RolfJsCmds {

	case class RemoveFieldError(containerId: String, inputId: String) extends JsCmd {
		override def toJsCmd = Call("Rolf.removeFieldError", containerId, inputId).cmd.toJsCmd
	}

	case class AttachFieldError(containerId: String, inputId: String, errorSeq: NodeSeq) extends JsCmd {
		override def toJsCmd = Call("Rolf.attachFieldError", containerId, inputId, errorSeq.toString()).cmd.toJsCmd
	}

	case class JqSlideDown(option: String) extends JsExp with JsMember {
		override val toJsCmd =
			"slideDown(" + option.encJs + ")"
	}

	case class JqSlideUp(option: String) extends JsExp with JsMember {
		override val toJsCmd =
			"slideUp(" + option.encJs + ")"
	}

	case class SlideDown(id: String, option: String) extends JsCmd {
		override def toJsCmd: String = {
			(JqId(Str(id)) ~> JqSlideDown(option)).cmd.toJsCmd
		}
	}

	case class SlideUp(id: String, option: String) extends JsCmd {
		override def toJsCmd: String = {
			(JqId(Str(id)) ~> JqSlideUp(option)).cmd.toJsCmd
		}
	}

	private case class Append(content: NodeSeq) extends JsExp with JsMember {
		override val toJsCmd = fixHtmlCmdFunc("inline", content){"append(" + _ + ")"}
	}

	object RolfAppendHtml {
		def apply(uid: String, content: NodeSeq): JsCmd =
			JqJE.JqId(JE.Str(uid)) ~> Append(content)
	}

}