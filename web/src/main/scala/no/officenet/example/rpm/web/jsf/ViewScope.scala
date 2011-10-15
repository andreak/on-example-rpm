package no.officenet.example.rpm.web.jsf

import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import javax.faces.context.FacesContext

class ViewScope extends Scope {

	def get(name: String, objectFactory: ObjectFactory[_]): AnyRef = {
		val viewMap = FacesContext.getCurrentInstance.getViewRoot.getViewMap
		if (viewMap.containsKey(name)) {
			return viewMap.get(name)
		}
		else {
			val o = objectFactory.getObject
			viewMap.put(name, o.asInstanceOf[AnyRef])
			return o.asInstanceOf[AnyRef]
		}
	}

	def remove(name: String): AnyRef = FacesContext.getCurrentInstance.getViewRoot.getViewMap.remove(name)

	def getConversationId: String = null

	def registerDestructionCallback(name: String, callback: Runnable) {
	}

	def resolveContextualObject(key: String): AnyRef = null
}