package no.officenet.example.rpm.support.infrastructure.jpa.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateWithMethodValidator.class)
public @interface ValidateWithMethod {
	String message() default ""; // Not used
	boolean ignoreIfNull() default true;
	String fieldName();
	String methodName();
	Class<? extends Payload>[] payload() default {};
	Class<?>[] groups() default {};

	/**
	 * Defines several <code>@ValidateWithMethod</code> annotations on the same element
	 *
	 * @see ValidateWithMethod
	 */
	@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List
	{
		ValidateWithMethod[] value();
	}

}
