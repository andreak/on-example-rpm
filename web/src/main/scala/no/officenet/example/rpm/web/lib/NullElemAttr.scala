package no.officenet.example.rpm.web.lib

import net.liftweb.http.SHtml
import xml.{Null, Elem}

case object NullElemAttr extends SHtml.ElemAttr {
	def apply(in: Elem): Elem = in % Null
}
