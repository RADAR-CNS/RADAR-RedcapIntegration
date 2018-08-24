package org.radarcns.redcap.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import org.radarcns.config.YamlConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Singleton class to manage configuration files.
 */
public final class Properties {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    private static final String HTTPS = "https";

    /** Path to the configuration file. */
    private static final String PATH_FILE = "/usr/local/etc/radar-redcap-int/";

    /** Placeholder alternative path for the config folder. */
    private static final String CONFIG_FOLDER = "REDCAP_INTEGRATION_CONFIG_FOLDER";

    /** API Config file name. */
    public static final String NAME_CONFIG_FILE = "radar.yml";

    /** Path where the config file is located. */
    //private static String validPath;

    private static final Configuration CONFIG;

    static {
        try {
            CONFIG = loadApiConfig();
        } catch (IOException exec) {
            LOGGER.error(exec.getMessage(), exec);
            throw new ExceptionInInitializerError(exec);
        }
    }

    private Properties() {
        //Nothing to do
    }


    /**
     * Loads the API configuration file. First of all, the {@code CONFIG_FOLDER} env variable is
     *      checked to verify if points a valid config file. If not, the default location for AWS
     *      and Docker image deployment are checked. In the last instance, the config file is
     *      searched inside the default projects resources folder.
     */
    private static Configuration loadApiConfig() throws IOException {
        String[] paths = new String[]{
            System.getenv(CONFIG_FOLDER),
                PATH_FILE
        };

        Configuration config;
        for (int i = 0; i < paths.length; i++) {
            config = loadApiConfig(paths[i]);
            if (config != null) {
                return config;
            }
        }

        try {
            String path = Properties.class.getClassLoader().getResource(NAME_CONFIG_FILE).getFile();
            //validPath = new File(path).getParent() + "/";

            LOGGER.info("Loading Config file located at : {}", path);

            return new YamlConfigLoader().load(new File(path), Configuration.class);
        } catch (NullPointerException exc) {
            String[] folders = Arrays.copyOfRange(paths,
                    System.getenv(CONFIG_FOLDER) == null ? 1 : 0, paths.length);
            LOGGER.error("Config file {} cannot be found at {} or in the resources"
                    + "folder.", NAME_CONFIG_FILE, folders, CONFIG_FOLDER);
            throw new FileNotFoundException(NAME_CONFIG_FILE + " cannot be found.");
        }
    }

    private static Configuration loadApiConfig(String path) throws IOException {
        //validPath = path;
        String filePath = path + NAME_CONFIG_FILE;

        if (checkFileExist(filePath)) {
            LOGGER.info("Loading Config file located at : {}", path);
            return new YamlConfigLoader().load(new File(filePath), Configuration.class);
        }

        //validPath = null;
        return null;
    }

    /**
     * Checks whether the give path points a file.
     *
     * @param path that should point a file
     * @return true if {@code path} points a file, false otherwise
     */
    private static boolean checkFileExist(String path) {
        return path == null ? false : new File(path).exists();
    }

    /**
     * Loads all configurations and converts them to {@link String}. If the conversion
     *      fails, it means that the config files are wrong.
     * @return a {@link String} representing the loaded configurations
     */
    public static String validate() {
        return CONFIG.toString();
    }

    protected static boolean isSupportedInstance(URL url, Integer projectId) {
        RedCapInfo identifier = new RedCapInfo(url, projectId);

        for (ProjectInfo info : CONFIG.getProjects()) {
            if (info.getRedCapInfo().equals(identifier)) {
                return true;
            }
        }

        return false;
    }

    protected static RedCapInfo getRedCapInfo(URL url, Integer projectId) {
        RedCapInfo identifier = new RedCapInfo(url, projectId);
        for (ProjectInfo info : CONFIG.getProjects()) {
            if (info.getRedCapInfo().equals(identifier)) {
                return info.getRedCapInfo();
            }
        }

        throw new IllegalArgumentException("No project " + projectId + " for instance "
                + url.toString());
    }

    protected static ManagementPortalInfo getMpInfo(URL url, Integer projectId) {
        RedCapInfo identifier = new RedCapInfo(url, projectId);
        for (ProjectInfo info : CONFIG.getProjects()) {
            if (info.getRedCapInfo().equals(identifier)) {
                return info.getMpInfo();
            }
        }

        throw new IllegalArgumentException("No project " + projectId + " for instance "
            + url.toString());
    }

    /**
     * Get the OAuth2 client id to access ManagementPortal.
     * @return the client id
     */
    public static String getOauthClientId() {
        return CONFIG.getOauthClientId();
    }

    /**
     * Get the OAuth2 client secret to access ManagementPortal.
     * @return the client secret
     */
    public static String getOauthClientSecret() {
        return CONFIG.getOauthClientSecret();
    }

    /**
     * Generates the token end point {@link URL} needed to refresh tokens against Management Portal.
     * @return {@link URL} useful to refresh tokens
     * @throws MalformedURLException in case the {@link URL} cannot be generated
     */
    public static URL getTokenEndPoint() throws MalformedURLException {
        return new URL(validateMpUrl(), CONFIG.getTokenEndpoint());
    }

    /**
     * Generates the token end point {@link URL} needed to manage subjects on Management Portal.
     * @return {@link URL} useful create and update subjects
     * @throws MalformedURLException in case the {@link URL} cannot be generated
     */
    public static URL getSubjectEndPoint() throws MalformedURLException {
        return new URL(validateMpUrl(), CONFIG.getSubjectEndpoint());
    }

    /**
     * Generates the Project end point {@link URL} needed to read projects on Management Portal.
     * @param mpInfo {@link ManagementPortalInfo} used to extract the Management Portal project
     *      identifier
     * @return {@link URL} useful to read project information
     * @throws MalformedURLException in case the {@link URL} cannot be generated
     */
    public static URL getProjectEndPoint(ManagementPortalInfo mpInfo) throws MalformedURLException {
        return new URL(validateMpUrl(), CONFIG.getProjectEndpoint().concat(
                mpInfo.getProjectName()));
    }

    /**
     * Generates the base Project end point {@link URL} needed to read projects on Management Portal.
     *
     * @return {@link URL} useful to read project information
     * @throws MalformedURLException in case the {@link URL} cannot be generated
     */
    public static URL getProjectEndPoint() throws MalformedURLException {
        return new URL(validateMpUrl(), CONFIG.getProjectEndpoint());
    }

    /**
     * Checks if the provided {@link URL} is using a secure connection or not.
     * @param url {@link URL} to check
     * @return {@code true} if the protocol is {@code HTTPS}, {@code false} otherwise
     */
    private static boolean isSecureConnection(URL url) {
        return url.getProtocol().equals(HTTPS);
    }

    /**
     * Returns a {@link URL} pointing a Management Portal instance and Checks if it is using a
     *      secure connection.
     * @return {@link URL} pointing the Management Portal instance specified on the config file
     */
    public static URL validateMpUrl() {
        if (!isSecureConnection(CONFIG.getManagementPortalUrl())) {
            LOGGER.warn("The provided Management Portal instance is not using an encrypted"
                    + " connection.");
        }
        return CONFIG.getManagementPortalUrl();
    }

    /**
     * Checks if the provided {@link URL} is using a secure connection and returns it.
     * @param url {@link URL} to has to be checked
     * @return the provided {@link URL}
     */
    public static URL validateRedcapUrl(URL url) {
        if (!isSecureConnection(url)) {
            LOGGER.warn("The provided REDCap instance is not using an encrypted connection.");
        }
        return url;
    }
}
