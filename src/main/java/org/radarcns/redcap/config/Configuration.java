package org.radarcns.redcap.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URL;
import java.util.Set;

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
 * Java class that defines the configuration required by the web app to handle authentication and
 *      authorisation against Management Portal and REDCap instances.
 */
public class Configuration {

    private final String version;

    private final String released;

    private final String oauthClientId;

    private final String oauthClientSecret;

    private final URL managementPortalUrl;

    private final String tokenEndpoint;

    private final String projectEndpoint;

    private final String subjectEndpoint;

    private final Set<ProjectInfo> projects;

    /**
     * Constructor.
     * @param version {@link String} reporting the web app current version
     * @param released {@link String} reporting the web app released date
     * @param oauthClientId {@link String} representing OAuth2 client identifier
     * @param oauthClientSecret {@link String} representing OAuth2 client identifier
     * @param managementPortalUrl {@link URL} pointing a Management Portal instane
     * @param tokenEndpoint {@link String} representing Management Portal web root to renew tokens
     * @param projectEndpoint {@link String} representing Management Portal web root to access
     *      project data
     * @param subjectEndpoint {@link String} representing Management Portal web root to manage
     *      subject
     * @param projects {@link Set} of {@link ProjectInfo} providing information about REDCap and
     *      Management Portal instances
     */
    @JsonCreator
    protected Configuration(
            @JsonProperty("version") String version,
            @JsonProperty("released") String released,
            @JsonProperty("oauth_client_id") String oauthClientId,
            @JsonProperty("oauth_client_secret") String oauthClientSecret,
            @JsonProperty("management_portal_url") URL managementPortalUrl,
            @JsonProperty("token_endpoint") String tokenEndpoint,
            @JsonProperty("project_endpoint") String projectEndpoint,
            @JsonProperty("subject_endpoint") String subjectEndpoint,
            @JsonProperty("projects") Set<ProjectInfo> projects) {
        this.version = version;
        this.released = released;
        this.oauthClientId = oauthClientId;
        this.oauthClientSecret = oauthClientSecret;
        this.managementPortalUrl = managementPortalUrl;
        this.tokenEndpoint = tokenEndpoint;
        this.projectEndpoint = projectEndpoint;
        this.subjectEndpoint = subjectEndpoint;
        this.projects = projects;
    }

    public String getVersion() {
        return version;
    }

    public String getReleased() {
        return released;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    public URL getManagementPortalUrl() {
        return managementPortalUrl;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getProjectEndpoint() {
        return projectEndpoint;
    }

    public String getSubjectEndpoint() {
        return subjectEndpoint;
    }

    public Set<ProjectInfo> getProjects() {
        return projects;
    }

    @Override
    public String toString() {
        return "Configuration {" + "\n"
            + "version='" + version + "'\n"
            + "released='" + released + "'\n"
            + "oauthClientId = '" + oauthClientId + "'\n"
            + "oauthClientSecret = '" + oauthClientSecret + "'\n"
            + "managementPortalUrl = " + managementPortalUrl + "\n"
            + "tokenEndpoint = '" + tokenEndpoint + "'\n"
            + "projectEndpoint = '" + projectEndpoint + "'\n"
            + "subjectEndpoint = '" + subjectEndpoint + "'\n"
            + "projects=" + projects  + "\n"
            + '}';
    }
}
