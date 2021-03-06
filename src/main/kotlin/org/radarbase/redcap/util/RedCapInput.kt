package org.radarbase.redcap.util

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
 * Interface that generalises REDCap input parameters. It forces the override of
 * [Object.equals] and [Object.hashCode] in order to create
 * [java.util.Set] of [RedCapInput] leaving to Java the overhead of checking for
 * duplicated inputs.
 * @see [org.radarbase.redcap.integration.IntegrationData]
 */
interface RedCapInput {
    /**
     * Returns the REDCap Record Id involved in the update.
     * @return [Integer] REDCap Record Id
     */
    val record: Int?

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}