package no.officenet.example.rpm.support.infrastructure.jpa

import org.springframework.orm.jpa.persistenceunit.{MutablePersistenceUnitInfo, PersistenceUnitPostProcessor}
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.`type`.filter.AnnotationTypeFilter
import javax.persistence.Converter

class ClasspathScanningPersistenceUnitPostProcessor(basePackage: String) extends PersistenceUnitPostProcessor with Loggable {
	def postProcessPersistenceUnitInfo(pui: MutablePersistenceUnitInfo) {
		val provider = new ClassPathScanningCandidateComponentProvider(false)
		provider.addIncludeFilter(new AnnotationTypeFilter(classOf[Converter]))
		import scala.collection.JavaConversions._
		for (definition <- provider.findCandidateComponents(basePackage)) {
			debug(s"Registering classpath-scanned entity ${definition.getBeanClassName} in persistence unit info!")
			pui.addManagedClassName(definition.getBeanClassName)
		}
	}
}
