package no.officenet.example.rpm.support.infrastructure.jpa.util;

import javax.validation.ConstraintViolationException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class ReflectionUtils {
	/**
	 * @return the method or null if the method does not exist
	 */
	public static Method getMethodRecursive(final Class< ? > clazz, final String methodName,
			final Class< ? >... parameterTypes)
	{
		final Method m = getMethod(clazz, methodName, parameterTypes);
		if (m != null) return m;

		final Class< ? > superclazz = clazz.getSuperclass();
		if (superclazz == null) return null;

		return getMethodRecursive(superclazz, methodName, parameterTypes);
	}

	/**
	 * @return the method or null if the method does not exist
	 */
	public static Method getMethod(final Class< ? > clazz, final String methodName, final Class< ? >... parameterTypes)
	{
		try
		{
			return clazz.getDeclaredMethod(methodName, parameterTypes);
		}
		catch (final NoSuchMethodException e)
		{
			return null;
		}
	}

	/**
	 * @return the field or null if the field does not exist
	 */
	public static Field getField(final Class< ? > clazz, final String fieldName)
	{
		try
		{
			return clazz.getDeclaredField(fieldName);
		}
		catch (final NoSuchFieldException e)
		{
			return null;
		}
	}

	public static Field getFieldRecursive(final Class< ? > clazz, final String fieldName)
	{
		final Field f = getField(clazz, fieldName);
		if (f != null) return f;

		final Class< ? > superclazz = clazz.getSuperclass();
		if (superclazz == null) return null;

		return getFieldRecursive(superclazz, fieldName);
	}

	public static Object getFieldValue(final Field field, final Object obj) throws IllegalStateException
	{
		try
		{
			if (!field.isAccessible()) AccessController.doPrivileged(new SetAccessibleAction(field));
			return field.get(obj);
		}
		catch (final Exception ex)
		{
			throw new IllegalStateException(ex.getMessage());
		}
	}

	public static Object getFieldValue(final String fieldName, final Object obj) throws IllegalStateException
	{
		try
		{
			final Field field = ReflectionUtils.getFieldRecursive(obj.getClass(), fieldName);
			if (field == null) return null;
			if (!field.isAccessible()) AccessController.doPrivileged(new SetAccessibleAction(field));
			return field.get(obj);
		}
		catch (final Exception ex)
		{
			throw new IllegalStateException(ex.getMessage());
		}
	}

	public static Object invokeMethod(final Method method, final Object obj, final Object... args)
			throws ConstraintViolationException
	{
		try
		{
			if (!method.isAccessible()) AccessController.doPrivileged(new SetAccessibleAction(method));
			return method.invoke(obj, args);
		}
		catch (final Exception ex)
		{
			if (ex.getCause() instanceof ConstraintViolationException)
				throw (ConstraintViolationException) ex.getCause();
			throw new IllegalStateException("Executing method " + method.getName() + " failed.", ex);
		}
	}


}

final class SetAccessibleAction implements PrivilegedAction<Object>
{
	private final AccessibleObject ao;

	public SetAccessibleAction(final AccessibleObject ao)
	{
		this.ao = ao;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object run()
	{
		ao.setAccessible(true);
		return null;
	}
}