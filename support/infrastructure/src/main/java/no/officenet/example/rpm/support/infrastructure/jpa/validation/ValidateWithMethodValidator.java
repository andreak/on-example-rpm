package no.officenet.example.rpm.support.infrastructure.jpa.validation;

import no.officenet.example.rpm.support.infrastructure.jpa.util.ReflectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ValidateWithMethodValidator implements ConstraintValidator<ValidateWithMethod, Object> {
	Log log = LogFactory.getLog(getClass());

	private boolean ignoreIfNull;
	private String methodName;
	private String fieldName;

	@Override
	public void initialize(ValidateWithMethod constraintAnnotation) {
		this.ignoreIfNull = constraintAnnotation.ignoreIfNull();
		this.fieldName = constraintAnnotation.fieldName();
		this.methodName = constraintAnnotation.methodName();
	}

	@Override
	public boolean isValid(Object validatedObject, ConstraintValidatorContext context) {
		final Method method = ReflectionUtils.getMethodRecursive(validatedObject.getClass(), methodName);
		if (method == null)
			throw new IllegalStateException("Method " + validatedObject.getClass().getName() + "." + methodName
					+ " not found.");
		if (!(method.getReturnType().equals(Boolean.TYPE) || method.getReturnType().equals(Boolean.class))) {
			throw new IllegalStateException("Method " + validatedObject.getClass().getName() + "." + methodName
											+ " does not return boolean, but " + method.getReturnType().getName() + ".");
		}
		// Get the value;
		Field field = ReflectionUtils.getFieldRecursive(validatedObject.getClass(), fieldName);

		Object fieldValue = ReflectionUtils.getFieldValue(field, validatedObject);
		if (fieldValue == null && ignoreIfNull) return true;

		boolean valid = (Boolean) ReflectionUtils.invokeMethod(method, validatedObject);
		if (!valid) {
			String resolvedMessage = "{method_validation." + validatedObject.getClass().getName() + "." + fieldName+"}";
			context.disableDefaultConstraintViolation();
			ConstraintViolationBuilder builder = context.buildConstraintViolationWithTemplate(resolvedMessage);
			builder.addNode(fieldName)
				.addConstraintViolation();
		}
		return valid;
	}
}
