package org.radarcns.redcap.listener;

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

import org.radarcns.oauth.OAuth2AccessToken;
import org.radarcns.oauth.OAuth2Client;
import org.radarcns.redcap.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

/**
 * Refreshes the OAuth2 token needed to authenticate against the Management Portal and adds it to
 *      the {@link javax.servlet.ServletContext} in this way multiple function can make reuse of it.
 */
@WebListener
public class TokenManagerListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenManagerListener.class);

    private static final String ACCESS_TOKEN = "TOKEN";

    private static final OAuth2Client client;
    private static OAuth2AccessToken token;

    static {
        try {
            client = new OAuth2Client()
                        .managementPortalUrl(Properties.getTokenEndPoint().toString())
                        .clientId(Properties.getOauthClientId())
                        .clientSecret(Properties.getOauthClientSecret())
                        .addScope("read")
                        .addScope("write");
        }
        catch (MalformedURLException exc) {
            LOGGER.error("Properties cannot be loaded. Check the log for more information.", exc);
            throw new ExceptionInInitializerError(exc);
        }
        token = new OAuth2AccessToken();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            getToken(sce.getServletContext());
        } catch (IllegalStateException exc) {
            LOGGER.warn("{} cannot be generated: {}", ACCESS_TOKEN, exc.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().setAttribute(ACCESS_TOKEN, null);
        LOGGER.info("{} has been invalidated.", ACCESS_TOKEN);
    }

    /**
     * Returns the {@code Access Token} needed to interact with the Management Portal. If the token
     *      available in {@link ServletContext} is still valid, it will be returned. In case it has
     *      expired, the functional will automatically renew it.
     * @param context {@link ServletContext} where the last used {@code Access Token} has been
     *      stored
     * @return a valid {@code Access Token} to contact Management Portal
     * @throws IllegalStateException If the refresh was completed but did not yield a valid token
     */
    public static String getToken(ServletContext context) throws IllegalStateException {
        if (token.isExpired()) {
            refresh(context);
        }

        // this checks that token is not null and error is null
        if (!token.isValid()) {
            throw new IllegalStateException(token.getError() + ": " + token.getErrorDescription());
        }

        return token.getAccessToken();
    }

    private static synchronized void refresh(ServletContext context) {
        // Multiple threads can be waiting to enter this method when the token is expired, we need
        // only the first one to request a new token, subsequent threads can safely exit immediately
        if (!token.isExpired()) {
            return;
        }
        token = client.getAccessToken();

        context.setAttribute(ACCESS_TOKEN, token.getAccessToken());

        // we need to supply date in millis, token.getIssueDate() and getExpiresIn() are in seconds
        LOGGER.info("Refreshed token at {} valid till {}", getDate(Instant.now().toEpochMilli()),
                getDate((token.getIssueDate() + token.getExpiresIn()) * 1000));
    }

    private static String getDate(long time) {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS").format(new Date(time));
    }
}
