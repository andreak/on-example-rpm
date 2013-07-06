package no.officenet.example.rpm.projectmgmt.domain.model.customizers

import org.eclipse.persistence.config.DescriptorCustomizer
import org.eclipse.persistence.descriptors.ClassDescriptor
import org.eclipse.persistence.mappings.foundation.AbstractColumnMapping
import org.eclipse.persistence.internal.jpa.metadata.converters.ConverterClass
import no.officenet.example.rpm.support.infrastructure.jpa.OptionDateTimeConverter
import java.sql.Timestamp

class ProjectDescriptorCustomizer extends DescriptorCustomizer {
	def customize(descriptor: ClassDescriptor) {
		descriptor.getMappingForAttributeName("estimatedStartDate") match {
			case mapping: AbstractColumnMapping =>
				mapping.setConverter(new ConverterClass(classOf[OptionDateTimeConverter].getName, false, classOf[Timestamp].getName, false))
			case _ =>

		}
		descriptor.getMappingForAttributeName("modified") match {
			case mapping: AbstractColumnMapping =>
				mapping.setConverter(new ConverterClass(classOf[OptionDateTimeConverter].getName, false, classOf[Timestamp].getName, false))
			case _ =>

		}
	}
}
