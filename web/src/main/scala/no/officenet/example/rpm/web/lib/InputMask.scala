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

case object NaturalNumberMask extends InputMask {
	override def getExtraArgs(field: ValidatableScreen#FormField[_, _]): Seq[JsExp] = {
		(field.maxLength or field.maxLengthOfFieldInChars).map(l => Seq(Num(l))).getOrElse(Seq.empty)
	}
	val getJsFunction = "Rolf.InputMask.naturalNumber"

}

case object PercentMask extends InputMask {
	def getJsFunction = "Rolf.InputMask.percent"
}

case object DecimalNumberMask extends InputMask {
	def getJsFunction = "Rolf.InputMask.decimalNumber"
}