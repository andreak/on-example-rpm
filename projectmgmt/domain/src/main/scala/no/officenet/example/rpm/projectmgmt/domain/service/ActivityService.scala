package no.officenet.example.rpm.projectmgmt.domain.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import no.officenet.example.rpm.support.domain.service.GenericDomainService
import no.officenet.example.rpm.projectmgmt.domain.model.entities.Activity
import repository.ActivityRepository
import org.springframework.beans.factory.annotation.Autowired

@Service
@Transactional
class ActivityServiceImpl @Autowired() (val activityRepository: ActivityRepository) extends ActivityService

trait ActivityService extends GenericDomainService[Activity] {

	val activityRepository: ActivityRepository

	repository = activityRepository // Note: *MUST* use constructor-injection for this to be set

	

}