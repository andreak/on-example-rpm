package no.officenet.example.rpm.support.infrastructure.validation;

import net.sf.oval.context.ClassContext;
import net.sf.oval.context.ConstructorParameterContext;
import net.sf.oval.context.FieldContext;
import net.sf.oval.context.MethodEntryContext;
import net.sf.oval.context.MethodExitContext;
import net.sf.oval.context.MethodParameterContext;
import net.sf.oval.context.MethodReturnValueContext;
import net.sf.oval.context.OValContext;
import net.sf.oval.internal.Log;
import net.sf.oval.localization.context.OValContextRenderer;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Use custom locale
 */
public class RpmResourceBundleValidationContextRenderer implements OValContextRenderer
{
	private static final Log  LOG = Log.getLog(RpmResourceBundleValidationContextRenderer.class);

	public static final RpmResourceBundleValidationContextRenderer INSTANCE = new RpmResourceBundleValidationContextRenderer();

	private boolean containsKey(final ResourceBundle bundle, final String key)
	{
		for (final Enumeration<String> en = bundle.getKeys(); en.hasMoreElements();)
		{
			if (en.nextElement().equals(key)) return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String render(final OValContext ovalContext)
	{
		final String baseName;
		final String key;
		if (ovalContext instanceof ClassContext)
		{
			final ClassContext ctx = (ClassContext) ovalContext;
			baseName = ctx.getClazz().getName();
			key = "label.class";
		}
		else if (ovalContext instanceof FieldContext)
		{
			final FieldContext ctx = (FieldContext) ovalContext;
			baseName = ctx.getField().getDeclaringClass().getName();
			final String fieldName = ctx.getField().getName();
			key = "label.field." + fieldName;
		}
		else if (ovalContext instanceof ConstructorParameterContext)
		{
			final ConstructorParameterContext ctx = (ConstructorParameterContext) ovalContext;
			baseName = ctx.getConstructor().getDeclaringClass().getName();
			key = "label.parameter." + ctx.getParameterName();
		}
		else if (ovalContext instanceof MethodParameterContext)
		{
			final MethodParameterContext ctx = (MethodParameterContext) ovalContext;
			baseName = ctx.getMethod().getDeclaringClass().getName();
			key = "label.parameter." + ctx.getParameterName();
		}
		else if (ovalContext instanceof MethodEntryContext)
		{
			final MethodEntryContext ctx = (MethodEntryContext) ovalContext;
			baseName = ctx.getMethod().getDeclaringClass().getName();
			key = "label.method." + ctx.getMethod().getName();
		}
		else if (ovalContext instanceof MethodExitContext)
		{
			final MethodExitContext ctx = (MethodExitContext) ovalContext;
			baseName = ctx.getMethod().getDeclaringClass().getName();
			key = "label.method." + ctx.getMethod().getName();
		}
		else if (ovalContext instanceof MethodReturnValueContext)
		{
			final MethodReturnValueContext ctx = (MethodReturnValueContext) ovalContext;
			baseName = ctx.getMethod().getDeclaringClass().getName();
			key = "label.method." + ctx.getMethod().getName();
		}
		else
			return ovalContext.toString();

		try
		{
			final ResourceBundle bundle = ResourceBundle.getBundle(baseName, getLocale());
			if (containsKey(bundle, key)) return bundle.getString(key);
			LOG.debug("Key {1} not found in bundle {2}", key, baseName);
		}
		catch (final MissingResourceException ex)
		{
			LOG.debug("Bundle {1} not found", baseName, ex);
		}
		return ovalContext.toString();
	}

	protected Locale getLocale() {
		Locale locale = LocaleContextHolder.getLocale();
		return locale;
	}
}
