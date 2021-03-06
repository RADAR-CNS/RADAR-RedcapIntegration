package org.radarbase.redcap.config

import com.fasterxml.jackson.annotation.JsonProperty

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
/**
 * Configuration file entry to define Project settings. Each item provides an instance of
 * [RedCapInfo] and an instance of [ManagementPortalInfo] defining respectively
 * REDCap and Management Portal configurations.
 */
data class ProjectInfo(
    @JsonProperty("redcap_info") val redCapInfo: RedCapInfo,
    @JsonProperty("mp_info") val mpInfo: ManagementPortalInfo
)