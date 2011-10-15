package no.officenet.example.rpm.support.infrastructure.spring

/*
 * Copyright (C) 2010 Matthias Weßendorf.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.AnnotationScopeMetadataResolver
import org.springframework.context.annotation.ScopeMetadata
import javax.enterprise.context.{ApplicationScoped, SessionScoped, RequestScoped}

/**
 * A <code>ScopeMetadataResolver</code> implementation which maps the CDI provided
 * scopes to their Spring counterparts.
 *
 * <p>The Spring Framework supports the JSR-330 to declare components, however it does not
 * support the API, provided by the JSR-299
 *
 * <p>This class allows Application developers to use the JSR-299 for scoping annotations. The
 * benefit is that a Spring bean can be completely defined by JSR-299 and JSR-330, like:
 *
 * <p><pre>
 * &#064;javax.inject.Named("mySpringBean")
 * &#064;javax.enterprise.context.RequestScoped
 * public class MySpringBeanController{...}
 * </pre>
 *
 * <p>To use this class you need to configure it within the Spring Context, like
 * <p><pre>
 * &lt;context:component-scan
 *    scope-resolver="no.officenet.jsflift.util.CdiScopeMetadataResolver"
 *    base-package="my.packages..." /&gt;
 * </pre>
 *
 * @author Matthias Weßendorf
 */
class CdiScopeMetadataResolver extends AnnotationScopeMetadataResolver {
	/**
	 * Checks if one of the following CDI scope annoations are used and maps
	 * them to their matching Spring scopes:
	 *
	 * <ul>
	 * <li><code>&#064;javax.enterprise.context.RequestScoped</code></li>
	 * <li><code>&#064;javax.enterprise.context.SessionScoped</code></li>
	 * <li><code>&#064;javax.enterprise.context.ApplicationScoped</code></li>
	 * </ul>
	 *
	 * If none of them are found it delegates back to the original Spring
	 * <code>AnnotationScopeMetadataResolver</code> class.
	 */
	override def resolveScopeMetadata(definition: BeanDefinition): ScopeMetadata = {
		val metadata: ScopeMetadata = new ScopeMetadata
		if (definition.isInstanceOf[AnnotatedBeanDefinition]) {
			val annDef = definition.asInstanceOf[AnnotatedBeanDefinition]
			val annotationTypes = annDef.getMetadata.getAnnotationTypes
			if (annotationTypes.contains(classOf[RequestScoped].getName)) {
				metadata.setScopeName("request")
			}
			else if (annotationTypes.contains(classOf[SessionScoped].getName)) {
				metadata.setScopeName("session")
			}
			else if (annotationTypes.contains(classOf[ApplicationScoped].getName)) {
				metadata.setScopeName("application")
			}
			else {
				return super.resolveScopeMetadata(definition)
			}
		}
		metadata
	}
}