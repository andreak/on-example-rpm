package no.officenet.example.rpm.web.lib

import net.liftweb._
import http.js.JE.Call
import http.js.JsCmds._

trait InputMask {
	def getJsFunction: String

	def applyTo(field: ValidatableScreen#TextFormField[_, _]) {
		field.script = field.script ++ Script(Call(getJsFunction, field.inputId))
	}

}

object NaturalNumberMask {
	val JsFunction = "Rolf.InputMask.naturalNumber"

	def apply() = {
		new NaturalNumberMask
	}
}

class NaturalNumberMask extends InputMask {
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