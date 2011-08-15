package no.officenet.example.rpm.support.infrastructure.jpa

import org.apache.commons.logging.LogFactory
import org.springframework.orm.jpa.persistenceunit.{MutablePersistenceUnitInfo, DefaultPersistenceUnitManager}
import collection.JavaConversions._
import javax.persistence.spi.PersistenceUnitInfo
import javax.sql.DataSource
import collection.mutable.{HashMap}
import java.util.HashSet

/**
 * Custom implementation of the {@link PersistenceUnitManager} interface which uses persistence units with the same name.
 */
class MergePersistenceUnitManager extends DefaultPersistenceUnitManager {
	private val logger = LogFactory.getLog(this.getClass.getName)
	private val persistenceUnits = new HashMap[String, PersistenceUnitInfo]
	private var includedPersistenceUnits: java.util.Set[String] = new HashSet[String]()
	private var jtdDataSource: DataSource = null

	override def postProcessPersistenceUnitInfo(pui: MutablePersistenceUnitInfo) {
		if (jtdDataSource != null) {
			pui.setJtaDataSource(jtdDataSource)
		}

		if (includedPersistenceUnits.isEmpty || includedPersistenceUnits.contains(pui.getPersistenceUnitName)) {
			super.postProcessPersistenceUnitInfo(pui)
			pui.addJarFileUrl(pui.getPersistenceUnitRootUrl)

			if (logger.isInfoEnabled) {
				logger.info("PersistenceUnitName: " + pui.getPersistenceUnitName + " at "+pui.getPersistenceUnitRootUrl)
			}

			val oldPui = getPersistenceUnitInfo(pui.getPersistenceUnitName)
			if (oldPui != null) {
				oldPui.getJarFileUrls.foreach {url => {
					if (logger.isInfoEnabled) {
						logger.info("Merging " + url)
					}
					pui.addJarFileUrl(url)
				}}
			}
			persistenceUnits.put(pui.getPersistenceUnitName, pui)
		} else {
			logger.debug("Disgarding persistent-unit: " + pui.getPersistenceUnitRootUrl)
		}
	}


	override def preparePersistenceUnitInfos() {
		super.preparePersistenceUnitInfos()
		persistenceUnits.values.foreach(pui => {
			// Remove rootUrl from list to prevent "cannot scan same url twice"-error thrown by Hibernate
			val removedRootUrlFromJars = pui.getJarFileUrls.remove(pui.getPersistenceUnitRootUrl)
			if (removedRootUrlFromJars) {
				if (logger.isInfoEnabled) {
					logger.info("persistence-unit '"+pui.getPersistenceUnitName+"': Removed PersistenceUnitRootUrl=" + pui.getPersistenceUnitRootUrl + " from jar-files")
				}
			}
		})
		persistenceUnits.clear() // No use to keep this
	}

	def setIncludedPersistenceUnits(includedPersistenceUnits: java.util.Set[String]) {
		this.includedPersistenceUnits = includedPersistenceUnits
	}

	def setJtdDataSource(jtdDataSource: DataSource) {
		this.jtdDataSource = jtdDataSource
	}

}