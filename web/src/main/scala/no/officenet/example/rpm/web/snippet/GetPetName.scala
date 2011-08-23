package no.officenet.example.rpm.web.snippet

import xml.Text
import javax.annotation.Resource
import no.officenet.example.rpm.pets.domain.service.repository.PetRepository
import net.liftweb.http.S
import org.springframework.beans.factory.annotation.Configurable

@Configurable
object GetPetName {

	@Resource
	val petRepository: PetRepository = null

	def render = Text(S.attr("petId").map(id => petRepository.retrieve(id.toLong).petName).openOr(""))

}