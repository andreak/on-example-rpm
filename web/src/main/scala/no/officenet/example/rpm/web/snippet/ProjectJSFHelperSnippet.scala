package no.officenet.example.rpm.web.snippet

import org.springframework.beans.factory.annotation.Configurable
import xml.NodeSeq
import net.liftweb.http.S


@Configurable
class ProjectJSFHelperSnippet {

	def render(in: NodeSeq) = {
		// The locale too has to be a part of the comet's name. Else switching locale doesn't affect the actor
		val cometName = List(S.request.get.param("locale").get, S.request.get.param("id").get)
		<div class={"lift:comet?type=ProjectJsfActor;name="+cometName.mkString(":")}
			 style="display: inline;">
			{in}
		</div>

	}


}