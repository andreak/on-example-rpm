package no.officenet.example.rpm.web.snippet

import net.liftweb._
import http._
import common._
import xml.{Text, Elem, NodeSeq}
import no.officenet.example.rpm.support.infrastructure.i18n.Bundle
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.L

object I18n extends DispatchSnippet {

	def dispatch : DispatchIt = {
		case "i" => ns => i(ns)
		case s => ns => render(ns)
	}

	def i(ns: NodeSeq): NodeSeq = {
		ns match {
			case Elem(prefix, label, attribs, scope, child @ _*) =>
				val bundle = S.attr("bundle").map(b => Bundle.valueOf(b).get) openOr Bundle.GLOBAL
				Elem(prefix, label, attribs, scope, Text(L(bundle, ns.text.trim())): _*)
			case _ => render(ns)
		}
	}

	def render(kids: NodeSeq) : NodeSeq = {
		val bundle = S.attr("bundle").map(b => Bundle.valueOf(b).get) openOr Bundle.GLOBAL
		S.attr("key") match {
			case Full(id) => Text(L(bundle, id))
			case _ => Text(L(bundle, kids.text.trim()))
		}
	}

}
