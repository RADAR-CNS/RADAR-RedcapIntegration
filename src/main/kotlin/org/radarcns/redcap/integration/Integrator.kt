package org.radarcns.redcap.integration

import org.radarcns.redcap.config.RedCapInfo
import org.radarcns.redcap.config.RedCapManager
import org.radarcns.redcap.managementportal.MpClient
import org.radarcns.redcap.util.RedCapClient
import org.radarcns.redcap.util.RedCapTrigger

/*
 * Copyright 2017 King's College London
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
/** Handler for updating Integrator Redcap form parameters. The input parameters are
 * described by [IntegrationData].
 */
class Integrator(
    private val trigger: RedCapTrigger, private val mpClient: MpClient,
    private val redCapInfo: RedCapInfo = RedCapManager.getInfo(trigger),
    private val mpIntegrator: MpIntegrator = MpIntegrator(mpClient),
    private val redCapIntegrator: RedCapIntegator = RedCapIntegator(RedCapClient(redCapInfo))
) {

    fun handleDataEntryTrigger(): Boolean {
        val recordId = trigger.record
        val enrolmentEvent = redCapInfo.enrolmentEvent
        val integrationFrom = redCapInfo.integrationForm
        val attributeKeys = redCapInfo.attributes?.map { a -> a.fieldName }

        checkNotNull(recordId) { "Record ID cannot be null." }
        checkNotNull(enrolmentEvent) { "Enrolment event cannot be null" }
        checkNotNull(integrationFrom) { "Integration Form cannot be null" }

        val attributes = if (attributeKeys == null) {
            mutableMapOf()
        } else {
            redCapIntegrator.pullRecordAttributes(attributeKeys, recordId)
        }
        val subject =
            mpIntegrator.performSubjectUpdateOnMp(
                redCapInfo.url,
                redCapInfo.projectId,
                recordId,
                attributes
            )
        return redCapIntegrator.updateRedCapIntegrationForm(
            subject,
            recordId,
            enrolmentEvent,
            integrationFrom
        )
    }
}