package org.radarcns.redcap.util;

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

import static org.radarcns.redcap.util.RedCapTrigger.TriggerParameter.INSTRUMENT_STATUS;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.radarcns.redcap.config.RedCapManager;

/**
 * <p>Represents data send by REDCap upon triggering.</p>
 * <p>In the REDCap Project Setup view, under {@code Additional customisations}, it is possible
 * to set a {@code Data Entry Trigger}. Upon any form or survey creation or update, REDCap
 * automatically triggers a request to the set end point.</p>
 * <p>The sent parameters are<ul>
 *     <li>project_id: The unique ID number of the REDCap project
 *          (i.e. the 'pid' value found in the URL when accessing the project in REDCap).</li>
 *     <li>username: The username of the REDCap user that is triggering the Data Entry Trigger.
 *          Note: If it is triggered by a survey page (as opposed to a data entry form), then
 *          the username that will be reported will be '[survey respondent]'.</li>
 *     <li>instrument: The unique name of the current data collection instrument(all your project's
 *          unique instrument names can be found in column B in the data dictionary).</li>
 *     <li>record: The name of the record being created or modified, which is the record's value
 *          for the project's first field.</li>
 *     <li>redcap_event_name: The unique event name of the event for which the record was modified
 *          (for longitudinal projects only).</li>
 *     <li>redcap_data_access_group:  The unique group name of the Data Access Group to which the
 *          record belongs (if the record belongs to a group).</li>
 *     <li>[instrument]_complete:  The status of the record for this particular data collection
 *          instrument, in which the value will be 0, 1, or 2. For data entry forms, 0=Incomplete,
 *          1=Unverified, 2=Complete. For surveys, 0=partial survey response and 2=completed survey
 *          response. This parameter's name will be the variable name of this particular
 *          instrument's status field, which is the name of the instrument + '_complete'.</li>
 *     <li>redcap_url: The base web address to REDCap (URL of REDCap's home page).
 *     <li>project_url: The base web address to the current REDCap project (URL of its Project
 *          Home page).</li>
 * </ul></p>
 */
public class RedCapTrigger {

    //private static final Logger LOGGER = LoggerFactory.getLogger(RedCapTrigger.class);

    public enum InstrumentStatus {
        INCOMPLETE(0),
        UNVERIFIED(1),
        COMPLETE(2);

        private int status;

        InstrumentStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    protected enum TriggerParameter {
        PROJECT_ID("project_id"),
        USERNAME("username"),
        INSTRUMENT("instrument"),
        RECORD("record"),
        REDCAP_EVENT_NAME("redcap_event_name"),
        REDCAP_DATA_ACCESS_GROUP("redcap_data_access_group"),
        INSTRUMENT_STATUS("_complete"),
        REDCAP_URL("redcap_url"),
        PROJECT_URL("project_url");

        private String name;

        TriggerParameter(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static String getInstrumentStatus(String instrument) {
            return instrument.concat(INSTRUMENT_STATUS.name);
        }
    }

    private Integer projectId;
    private String username;
    private String instrument;
    private Integer record;
    private String redcapEventName;
    private String redcapDataAccessGroup;
    private InstrumentStatus status;
    private URL projectUrl;
    private URL redcapUrl;

    /**
     * Constructor.
     * @param value {@link String} representation of REDCap trigger parameters.
     */
    public RedCapTrigger(String value) {
        try {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            String input = new String(bytes, StandardCharsets.UTF_8);
            parser(input.split("&"));
        } catch (IOException exc) {
            throw new IllegalArgumentException(exc);
        }
    }

    /** REDCap provides trigger parameters as a sequence of values separated by &. */
    private void parser(String[] values) throws UnsupportedEncodingException,
            MalformedURLException {
        int markerIndex;
        for (String val : values) {
            markerIndex = val.indexOf("=");
            switch (convertParameter(val, markerIndex)) {
                case REDCAP_URL:
                    redcapUrl = new URL(java.net.URLDecoder.decode(
                            val.substring(markerIndex + 1).trim(), StandardCharsets.UTF_8.name()));
                    break;
                case PROJECT_URL:
                    projectUrl = new URL(java.net.URLDecoder.decode(
                            val.substring(markerIndex + 1).trim(), StandardCharsets.UTF_8.name()));

                    //Override REDCap URL
                    String temp = projectUrl.toString();
                    redcapUrl = new URL(temp.substring(0, temp.indexOf("index.php?")));

                    break;
                case PROJECT_ID:
                    projectId = Integer.parseInt(val.substring(markerIndex + 1).trim());
                    break;
                case USERNAME:
                    username = val.substring(markerIndex + 1).trim();
                    break;
                case RECORD:
                    record = Integer.parseInt(val.substring(markerIndex + 1).trim());
                    break;
                case REDCAP_EVENT_NAME:
                    redcapEventName = val.substring(markerIndex + 1).trim();
                    break;
                case INSTRUMENT:
                    instrument = val.substring(markerIndex + 1).trim();
                    break;
                case REDCAP_DATA_ACCESS_GROUP:
                    redcapDataAccessGroup = val.substring(markerIndex + 1).trim();
                    break;
                case INSTRUMENT_STATUS:
                    status = getInstrumentStatus(Integer.valueOf(val.substring(markerIndex + 1)));
                    break;
                default:
                    throw new IllegalArgumentException(val + " cannot be parsed.");
            }
        }
    }

    private TriggerParameter convertParameter(String val, int markerIndex) {
        String name = val.substring(0, markerIndex).trim();
        for (TriggerParameter param : TriggerParameter.values()) {
            if (param.getName().equals(name)) {
                return param;
            }
        }

        if (val.startsWith(TriggerParameter.getInstrumentStatus(instrument))) {
            return INSTRUMENT_STATUS;
        }

        throw new IllegalArgumentException(" No enum constant for " + name);
    }

    private static InstrumentStatus getInstrumentStatus(int value) {
        switch (value) {
            case 0: return InstrumentStatus.INCOMPLETE;
            case 1: return InstrumentStatus.UNVERIFIED;
            case 2: return InstrumentStatus.COMPLETE;
            default: throw new IllegalArgumentException(value + " cannot be converted in "
                    + InstrumentStatus.class.getName());
        }
    }

    public Integer getProjectId() {
        return projectId;
    }

    public String getUsername() {
        return username;
    }

    public String getInstrument() {
        return instrument;
    }

    public Integer getRecord() {
        return record;
    }

    public String getRedcapEventName() {
        return redcapEventName;
    }

    public String getRedcapDataAccessGroup() {
        return redcapDataAccessGroup;
    }

    public InstrumentStatus getStatus() {
        return status;
    }

    public URL getRedcapUrl() {
        return redcapUrl;
    }

    public URL getProjectUrl() {
        return projectUrl;
    }

    /**
     * Checks if the event related to the trigger is the integration event.
     * @return {@code true} if the event that has triggered the update is the integration one,
     *      {@code false} otherwise.
     */
    public boolean isEnrolment() {
        return redcapEventName.equalsIgnoreCase(
            RedCapManager.getInfo(this).getEnrolmentEvent());
    }

    @Override
    public String toString() {
        return "RedCapTrigger{"
                + "projectId='" + projectId + '\''
                + ", username='" + username + '\''
                + ", instrument='" + instrument + '\''
                + ", record='" + record + '\''
                + ", redcapEventName='" + redcapEventName + '\''
                + ", redcapDataAccessGroup='" + redcapDataAccessGroup + '\''
                + ", status=" + status
                + ", redcapUrl='" + redcapUrl + '\''
                + ", projectUrl='" + projectUrl + '\''
                + '}';
    }

    /**
     * Gets the status field related to the instrument given in input.
     * @param instrument {@link String} representing instrument field
     * @return {@link String} representing status field related to the given instrument
     */
    public static String getInstrumentStatusField(String instrument) {
        Objects.requireNonNull(instrument);
        return instrument.concat(INSTRUMENT_STATUS.getName());
    }
}