package no.officenet.example.rpm.support.infrastructure.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourceBundleHelper {
	private static final Log log = LogFactory.getLog(ResourceBundleHelper.class);

	// Cache over all translated messages, for all bundles, before place-holders like {0} are expanded.
	protected static final Map<String, Map<String, MessageFormat>> formats = new HashMap<String, Map<String, MessageFormat>>();
	protected static final Map<Locale, Map<String, Map<String, MessageFormat>>> formatsForLocale =
		new HashMap<Locale, Map<String, Map<String, MessageFormat>>>();

	public static void resetCachedFormats() {
		synchronized (formats) {
			formats.clear();
			formatsForLocale.clear();
		}
	}

	public static String getMessage(String resourceBundleName, String resourceKey, Object... arguments) {
		return getMessage(getLocale(), resourceBundleName, resourceKey, arguments);
	}

	public static String getMessage(Locale locale, String resourceBundleName, String resourceKey, Object... arguments) {
		if (resourceBundleName == null) {
			throw new IllegalArgumentException("resourceBundleName cannot be null");
		}
		if (resourceKey == null) {
			throw new IllegalArgumentException("resourceKey cannot be null");
		}
		String formatKey = messageKey(locale, resourceKey);
		Map<String, MessageFormat> formatsForBundle = formats.get(resourceBundleName);
		if (formatsForBundle == null) {
			synchronized (formats) {
				formatsForBundle = formats.get(resourceBundleName);
				if (formatsForBundle == null) {
					formatsForBundle = new HashMap<String, MessageFormat>();
					formats.put(resourceBundleName, formatsForBundle);
				}
			}
		}
		MessageFormat format = formatsForBundle.get(formatKey);
		if (format == null) {
			synchronized (formats) {
				formatsForBundle = formats.get(resourceBundleName);
				format = formatsForBundle.get(formatKey);
				if (format == null) {
					String formatString = getFormatMessage(resourceBundleName, locale, resourceKey);
					format = new MessageFormat(formatString);
					formatsForBundle.put(formatKey, format);
					formats.put(resourceBundleName, formatsForBundle);
				}
			}
		}
		return format.format(arguments);
	}

	public static Map<String, MessageFormat> getMessages(ResourceBundleNameProvider bundle, Locale locale) {
		String bundleName = bundle.path();
		Map<String, Map<String, MessageFormat>> mapForLocale = formatsForLocale.get(locale);
		if (mapForLocale == null) {
			synchronized (formatsForLocale) {
				mapForLocale = formatsForLocale.get(locale); // Someone might have slipped in so check again in sync-context
				if (mapForLocale == null) {
					mapForLocale = new HashMap<String, Map<String, MessageFormat>>();
					formatsForLocale.put(locale, mapForLocale);
				}
			}
		}
		Map<String, MessageFormat> mapForBundle = mapForLocale.get(bundleName);
		if (mapForBundle == null) {
			synchronized (formatsForLocale) {
				mapForLocale = formatsForLocale.get(locale);
				mapForBundle = mapForLocale.get(bundleName); // Someone might have slipped in formatsForLocale in another thread so check again in sync-context
				if (mapForBundle == null) {
					mapForBundle = new HashMap<String, MessageFormat>();
					mapForLocale.put(bundleName, mapForBundle);
					ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, locale);
					for (String key : resourceBundle.keySet()) {
						String formatString = resourceBundle.getString(key);
						MessageFormat format = new MessageFormat(formatString);
						mapForBundle.put(key, format);
					}
				}
			}
		}
		return mapForBundle;
	}

	private static Locale getLocale() {
		Locale locale = LocaleContextHolder.getLocale();
		return locale;
	}

	private static String getFormatMessage(String resourceBundleName, Locale locale, String resourceKey) {
		ResourceBundle bundle = ResourceBundle.getBundle(resourceBundleName, locale);
		if (bundle == null) {
			throw new IllegalArgumentException("Resource-bundle not found: "+ resourceBundleName);
		}
		try {
			return bundle.getString(resourceKey);
		} catch (MissingResourceException e) {
			throw new MissingResourceException("Missing resource for locale '" + locale + "'.", resourceBundleName, resourceKey);
		}
	}

	private static String messageKey(Locale locale, String key) {
		return (localeKey(locale) + "." + key);
	}

	private static String localeKey(Locale locale) {

		if (locale == null) {
			return ("");
		} else {
			return locale.toString();
		}

	}

}
