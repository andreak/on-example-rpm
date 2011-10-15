package no.officenet.example.rpm.web.lib

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JE._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import xml.NodeSeq

object ErrorDialog {

	def apply(title: NodeSeq, messageLine1: NodeSeq, messageLine2: Option[NodeSeq]): JQueryDialog = {
		val html: NodeSeq = messageLine1 ++ messageLine2.getOrElse(NodeSeq.Empty)
		val isModal: Boolean = true
		new JQueryDialog(html, title) {
			override def elementId = nextFuncName
			override def options = JsObj("modal" -> isModal) +*
								   super.options
		}
	}

	def apply(title: String, messageLine1: NodeSeq, messageLine2: Option[NodeSeq]): JQueryDialog = {
		val html: NodeSeq = messageLine1 ++ messageLine2.getOrElse(NodeSeq.Empty)
		val isModal: Boolean = true
		new JQueryDialog(html, title) {
			override def elementId = nextFuncName
			override def options = JsObj("modal" -> isModal) +*
								   super.options
		}
	}

}
