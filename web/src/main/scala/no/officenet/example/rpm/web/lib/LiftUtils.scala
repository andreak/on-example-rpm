package no.officenet.example.rpm.web.lib

import net.liftweb._
import common._
import http.js.JsCmds._
import http.js.JE._
import http.js.{JsExp, JsCmd}
import http.SHtml
import util.Helpers._
import util.{PassThru, CssSel}
import xml.{Text, NodeSeq}
import no.officenet.example.rpm.support.domain.model.entities.User

object LiftUtils {

	/**
	 * Hide the matched CssSel (LHS) if the 'isEmptyCond' evaluates to true, else show the bloc 'f'
	 */
	def collapseIf[A](isEmptyCond: Boolean)(f: => A): Box[A] = {
		if (isEmptyCond) {
			Empty
		} else {
			Full(f)
		}
	}

	def enableIf[T](enable: Boolean)(enabled: T, disabled: T): T = {
		if (enable) enabled
		else disabled
	}

	def nullSafeString(string: String) = if (string == null) "" else string

	def mandatoryIcon(isMandatory: Boolean) = {
		if (isMandatory)
			<div class="mandatory">*</div>
		else {
			NodeSeq.Empty
		}
	}

	def prependWith(nodeSeqToPrepend: NodeSeq): (NodeSeq) => NodeSeq = {
		(ns: NodeSeq) => nodeSeqToPrepend ++ ns
	}

	def hideParent(sel: CssSel): CssSel = {
		"*" #> ((ns: NodeSeq) => PassThru(sel(ns.head.child)))
	}

	def addAttributeIf(cond: Boolean, attribute: SHtml.ElemAttr): SHtml.ElemAttr = {
		if (cond) attribute else NullElemAttr
	}

	def getNodesWithClass(seq: NodeSeq, className: String): Box[NodeSeq] = {
		var selected: Box[NodeSeq] = Empty
		(("." + className) #> ((ns: NodeSeq) => {
			selected = Full(ns)
			ns
		})
			).apply(seq)
		selected
	}

	def getAsAnonFunc(jsExp: String): String = {
		"(" + AnonFunc(Run(jsExp)).toJsCmd + ").bind(this)()"
	}

	def getAsAnonFunc(jsCmd: JsCmd): JsExp = {
		JsRaw("(" + AnonFunc(jsCmd).toJsCmd + ").bind(this)")
	}

	def getLoggedInUser: Box[User] = ContextVars.loggedInUserVar.get

	def renderWiki(rawString: String): NodeSeq = {
		WikiRenderer.render(rawString)
	}
}

object WikiRenderer {
	def render(rawString: String): NodeSeq = {
		Text(rawString) // We don't have any proper wiki-parsers...
	}
}