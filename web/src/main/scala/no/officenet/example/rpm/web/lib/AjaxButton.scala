package no.officenet.example.rpm.web.lib

import net.liftweb._
import util.Helpers._
import http._
import js._
import xml.NodeSeq

case class AjaxButton(text: String, onClickFunc: () => JsCmd, buttonId: String,
					  isEnabled: Boolean, attrs: SHtml.ElemAttr*) {

	def toNodeSeq: NodeSeq = {
		SHtml.ajaxButton(text, onClickFunc, attrs:_*)
	}

}

object AjaxButton {
	implicit def ajaxButton2NodeSeq(button: AjaxButton) = button.toNodeSeq

	def apply(text: String, onClickFunc: () => JsCmd, attrs: SHtml.ElemAttr*) = {
		new AjaxButton(text, onClickFunc, nextFuncName, true, attrs: _*)
	}

}