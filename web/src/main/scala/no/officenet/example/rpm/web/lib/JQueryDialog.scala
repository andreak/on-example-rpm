package no.officenet.example.rpm.web.lib

import xml.NodeSeq
import net.liftweb.http.js.jquery.JqJE._
import net.liftweb.http.js.JE._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import java.util.UUID

object JQueryDialog {
	def apply(html: NodeSeq) = new JQueryDialog(html, "")
	def apply(html: NodeSeq, title: String) = new JQueryDialog(html, title)
	def apply(html: NodeSeq, title: String, isModal: Boolean) = {
		new JQueryDialog(html, title) {
			override def options = JsObj("modal" -> isModal) +*
								   super.options
		}
	}
	def apply(html: NodeSeq, title: String, isModal: Boolean, id: String) = {
		new JQueryDialog(html, title) {
			override def elementId = id
			override def options = JsObj("modal" -> isModal) +*
								   super.options
		}
	}
}

class JQueryDialog(in: NodeSeq, title: String) {
  protected lazy val uuid: String = "dialog_box_%s".format(UUID.randomUUID)
  protected def elementId = uuid

  protected val cssClass: String = "dialog_box"

  protected def options = JsObj(
    "title" -> title,
    "width" -> 600,
    "height" -> 650,
    "resizable" -> true,
    "show" -> "drop", // choose effect here
    "hide" -> "drop", // choose effect here
    "close" -> AnonFunc("ev, ui", Jq(JsVar("this")) ~> JsFunc("remove")))

  def open: JsCmd =
    Jq("<div id='%s' class='%s'></div>".format(elementId, cssClass)) ~>
      JsFunc("appendTo", "body") ~>
      JsFunc("html", in.toString()) ~>
      JsFunc("dialog", options) ~>
      JsFunc("dialog", "open")

  def close: JsCmd = JqId(Str(elementId)) ~> JsFunc("dialog", "close")
}
