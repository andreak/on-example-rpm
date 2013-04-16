package no.officenet.example.rpm.web.lib

import net.liftweb._
import common._
import http.js.JsCmds._
import http.js.JE._
import http.js.{JsExp, JsCmd}
import net.liftweb.http.{S, SHtml}
import util.Helpers._
import util.{PassThru, CssSel}
import scala.xml._
import no.officenet.example.rpm.support.domain.model.entities.User
import net.liftweb.common.Full
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.Run

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

	final case class StrFuncElemAttr[T](name: String, value: (T) => String) extends SHtml.ElemAttr {
		/**
		 * Apply the attribute to the element
		 */
		def apply(in: Elem): Elem = {
			val snipp = (s: T) => value(s)
			in % (name -> snipp)
		}

		def apply(in: T): SHtml.ElemAttr = (name -> value(in))
	}

	def ritchRadioElem[T](opts: Seq[T], deflt: Box[T], attrs: SHtml.ElemAttr*)
						 (onSubmit: Box[T] => Any): SHtml.ChoiceHolder[T] = {

		def checked(in: Boolean) = if (in) new UnprefixedAttribute("checked", "checked", Null) else Null

		val possible = opts.map(v => nextFuncName -> v).toList

		val hiddenId = nextFuncName

		S.fmapFunc(S.LFuncHolder(lst => lst.filter(_ != hiddenId) match {
			case Nil => onSubmit(Empty)
			case x :: _ => onSubmit(possible.filter(_._1 == x).
				headOption.map(_._2))
		})) {
			name => {
				val items = possible.zipWithIndex.map {
					case ((id, value), idx) => {
						val radio =
							attrs.foldLeft(<input type="radio"
												  name={name} value={id}/>) {
								(b, a) =>
									b % (if (a.isInstanceOf[StrFuncElemAttr[T]]) {
										a.asInstanceOf[StrFuncElemAttr[T]].apply(value)
									} else {
										a
									})
							} %
								checked(deflt.filter(_ == value).isDefined)

						val elem = if (idx == 0) {
							radio ++ <input type="hidden" value={hiddenId} name={name}/>
						} else {
							radio
						}

						SHtml.ChoiceItem(value, elem)
					}
				}

				SHtml.ChoiceHolder(items)
			}
		}
	}

}

object WikiRenderer {
	def render(rawString: String): NodeSeq = {
		Text(rawString) // We don't have any proper wiki-parsers...
	}
}