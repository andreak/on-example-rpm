package no.officenet.example.rpm.web.lib

import net.liftweb._
import http.js.JE.{Str, Num, Call}
import http.js.{JE, JsExp}
import net.liftweb.common.Box._
import http.js.JsCmds._

trait InputMask {
	def getJsFunction: String

	def applyTo(field: ValidatableScreen#FormField[_, _]) {
		field.script = field.script ++ Script(Call(getJsFunction, (Str(field.inputId) +: getExtraArgs(field)):_*))
	}

	def getExtraArgs(field: ValidatableScreen#FormField[_, _]): Seq[JsExp] = Seq.empty

}

object NaturalNumberMask {
	val JsFunction = "Rolf.InputMask.naturalNumber"

	def apply() = {
		new NaturalNumberMask
	}
}

class NaturalNumberMask extends InputMask {
	override def getExtraArgs(field: ValidatableScreen#FormField[_, _]): Seq[JsExp] = {
		(field.maxLength or field.maxLengthOfFieldInChars).map(l => Seq(Num(l))).getOrElse(Seq.empty)
	}
	val getJsFunction = NaturalNumberMask.JsFunction

}

object PercentMask {
	val JsFunction = "Rolf.InputMask.percent"

	def apply() = {
		new PercentMask
	}
}

class PercentMask extends InputMask {
	val getJsFunction = PercentMask.JsFunction
}