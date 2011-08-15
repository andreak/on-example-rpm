package no.officenet.example.rpm.projectmgmt.application.service

import no.officenet.example.rpm.support.infrastructure.spring.AppService
import javax.annotation.Resource
import no.officenet.example.rpm.projectmgmt.domain.service.ActivityService
import no.officenet.example.rpm.projectmgmt.application.dto.ActivityDto

@AppService
class ActivityAppServiceImpl extends ActivityAppService

trait ActivityAppService {

	@Resource
	val activityService: ActivityService = null

	def retrieve(id: java.lang.Long): ActivityDto = {
		val activityDto = new ActivityDto
		activityDto
	}

	def create(activityDto: ActivityDto): ActivityDto = {
		activityDto.activity = activityService.create(activityDto.activity)
		activityDto
	}
}