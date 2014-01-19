package no.officenet.example.rpm.support.infrastructure.i18n

import org.springframework.context.i18n.LocaleContextHolder
import java.text.MessageFormat
import java.util.HashMap
import java.util.Locale
import java.util.Map
import java.util.MissingResourceException
import java.util.ResourceBundle

object ResourceBundleHelper {

	private val formats: Map[String, Map[String, MessageFormat]] = new HashMap[String, Map[String, MessageFormat]]
	private val formatsForLocale: Map[Locale, Map[String, Map[String, MessageFormat]]] = new HashMap[Locale, Map[String, Map[String, MessageFormat]]]

	def resetCachedFormats() {
		formats synchronized {
			formats.clear()
			formatsForLocale.clear()
		}
		ResourceBundle.clearCache()
	}

	def getMessage(resourceBundleName: String, resourceKey: String, arguments: Any*): String = {
		getMessage(getLocale, resourceBundleName, resourceKey, arguments: _*)
	}

	def getMessage(locale: Locale, resourceBundleName: String, resourceKey: String, arguments: Any*): String = {
		getMessage(locale, resourceBundleName, resourceKey, true, arguments: _*)
	}

	def getMessage(locale: Locale, resourceBundleName: String, resourceKey: String, useFormat: Boolean,  arguments: Any*): String = {
		if (resourceBundleName == null) {
			throw new IllegalArgumentException("resourceBundleName cannot be null")
		}
		if (resourceKey == null) {
			throw new IllegalArgumentException("resourceKey cannot be null")
		}
		val formatKey = messageKey(locale, resourceKey)
		var formatsForBundle = formats.get(resourceBundleName)
		if (formatsForBundle == null) {
			formats synchronized {
				formatsForBundle = getFormatsForBundle(resourceBundleName)
			}
		}
		if (useFormat) {
			var format = formatsForBundle.get(formatKey)
			if (format == null) {
				formats synchronized {
					formatsForBundle = formats.get(resourceBundleName)
					if (formatsForBundle == null) {
						formatsForBundle = getFormatsForBundle(resourceBundleName)
					}
					format = formatsForBundle.get(formatKey)
					if (format == null) {
						val formatString = getFormatMessage(resourceBundleName, locale, resourceKey)
						format = new MessageFormat(formatString, locale)
						formatsForBundle.put(formatKey, format)
						formats.put(resourceBundleName, formatsForBundle)
					}
				}
			}
			format.format(arguments.toArray)
		}
		else {
			getFormatMessage(resourceBundleName, locale, resourceKey);
		}

}

	private def getFormatsForBundle(resourceBundleName: String): Map[String, MessageFormat] = {
		var formatsForBundle = formats.get(resourceBundleName)
		if (formatsForBundle == null) {
			formatsForBundle = new HashMap[String, MessageFormat]
			formats.put(resourceBundleName, formatsForBundle)
		}
		formatsForBundle
	}

	def getMessages(bundle: ResourceBundleNameProvider, locale: Locale): Map[String, MessageFormat] = {
		val bundleName = bundle.path
		var mapForLocale = formatsForLocale.get(locale)
		if (mapForLocale == null) {
			formatsForLocale synchronized {
				mapForLocale = getMapForLocale(locale)
			}
		}
		var mapForBundle = mapForLocale.get(bundleName)
		if (mapForBundle == null) {
			formatsForLocale synchronized {
				mapForLocale = formatsForLocale.get(locale)
				if (mapForLocale == null) {
					mapForLocale = getMapForLocale(locale)
				}
				mapForBundle = mapForLocale.get(bundleName)
				if (mapForBundle == null) {
					mapForBundle = new HashMap[String, MessageFormat]
					mapForLocale.put(bundleName, mapForBundle)
					val resourceBundle = ResourceBundle.getBundle(bundleName, locale)
					import scala.collection.JavaConversions._
					for (key <- resourceBundle.keySet) {
						val formatString = resourceBundle.getString(key)
						val format = new MessageFormat(formatString)
						mapForBundle.put(key, format)
					}
				}
			}
		}
		mapForBundle
	}

	private def getLocale: Locale = LocaleContextHolder.getLocale

	private def getMapForLocale(locale: Locale): Map[String, Map[String, MessageFormat]] = {
		var mapForLocale = formatsForLocale.get(locale)
		if (mapForLocale == null) {
			mapForLocale = new HashMap[String, Map[String, MessageFormat]]
			formatsForLocale.put(locale, mapForLocale)
		}
		mapForLocale
	}

	private def getFormatMessage(resourceBundleName: String, locale: Locale, resourceKey: String): String = {
		val bundle = ResourceBundle.getBundle(resourceBundleName, locale)
		if (bundle == null) {
			throw new IllegalArgumentException("Resource-bundle not found: " + resourceBundleName)
		}
		try {
			bundle.getString(resourceKey)
		}
		catch {
			case e: MissingResourceException => {
				throw new MissingResourceException(
					String.format("Missing resource for locale='%s', bundle='%s', key='%s'.", locale, resourceBundleName,
						resourceKey), resourceBundleName, resourceKey)
			}
		}
	}

	private def messageKey(locale: Locale, key: String): String = (localeKey(locale) + "." + key)

	private def localeKey(locale: Locale): String = {
		if (locale == null) {
			""
		} else {
			locale.toString
		}
	}

}