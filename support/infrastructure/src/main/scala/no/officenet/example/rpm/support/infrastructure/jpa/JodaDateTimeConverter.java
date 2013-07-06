package no.officenet.example.rpm.support.infrastructure.jpa;

import org.joda.time.DateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;

@Converter(autoApply = true)
public class JodaDateTimeConverter implements AttributeConverter<DateTime, Timestamp> {

	@Override
	public Timestamp convertToDatabaseColumn(DateTime attribute) {
		if (attribute == null) {
			return null;
		} else {
			return new Timestamp(attribute.getMillis());
		}
	}

	@Override
	public DateTime convertToEntityAttribute(Timestamp dbData) {
		if (dbData == null) {
			return null;
		} else {
			return new DateTime(dbData.getTime());
		}
	}
}
