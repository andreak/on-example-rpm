package no.officenet.example.rpm.web.snippet

import net.liftweb._
import http._
import js.JE.JsRaw
import js.JsCmds._
import util.Helpers._
import collection.JavaConversions._
import no.officenet.example.rpm.support.infrastructure.i18n.{Bundle, ResourceBundleHelper}

class ConfigSnippet {

	def setBaseURLInJavaScript = {
		"*" #> Script(JsRaw("Rolf.setBaseUrl(\""+S.contextPath+"/" + S.locale + "\")"))
	}

	def setGlobalJavaScript = {
		val globalMessages = ResourceBundleHelper.getMessages(Bundle.GLOBAL, S.locale)
		val sb = new StringBuilder
		sb.append("Rolf.appendProperties($H({\n")
		var index = 0
		for (key <- globalMessages.keys) {
			if (index > 0) sb.append(",\n")
			sb.append(key.encJs).append(": ").append(globalMessages.get(key).toPattern.encJs)
			index += 1
		}
		sb.append("}))")
		"*" #> Script(JsRaw(sb.toString()))
	}

}