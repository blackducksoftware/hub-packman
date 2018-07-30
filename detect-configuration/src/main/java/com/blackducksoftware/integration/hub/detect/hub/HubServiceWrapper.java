/**
 * detect-configuration
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.detect.hub;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.response.CurrentVersionView;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.detect.configuration.AdditionalPropertyConfig;
import com.blackducksoftware.integration.hub.detect.configuration.DetectConfigWrapper;
import com.blackducksoftware.integration.hub.detect.configuration.DetectProperty;
import com.blackducksoftware.integration.hub.detect.exception.DetectUserFriendlyException;
import com.blackducksoftware.integration.hub.detect.exitcode.ExitCodeType;
import com.blackducksoftware.integration.hub.rest.BlackduckRestConnection;
import com.blackducksoftware.integration.hub.service.CodeLocationService;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.service.ReportService;
import com.blackducksoftware.integration.hub.service.ScanStatusService;
import com.blackducksoftware.integration.hub.service.SignatureScannerService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.Slf4jIntLogger;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.util.ResourceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class HubServiceWrapper {
    private final Logger logger = LoggerFactory.getLogger(HubServiceWrapper.class);

    private final DetectConfigWrapper detectConfigWrapper;
    private final AdditionalPropertyConfig additionalPropertyConfig;
    private final Gson gson;
    private final JsonParser jsonParser;

    private Slf4jIntLogger slf4jIntLogger;
    private HubServerConfig hubServerConfig;
    private HubServicesFactory hubServicesFactory;

    public HubServiceWrapper(final DetectConfigWrapper detectConfigWrapper, final AdditionalPropertyConfig additionalPropertyConfig, final Gson gson, final JsonParser jsonParser) {
        this.detectConfigWrapper = detectConfigWrapper;
        this.additionalPropertyConfig = additionalPropertyConfig;
        this.gson = gson;
        this.jsonParser = jsonParser;
    }

    public void init() throws IntegrationException, DetectUserFriendlyException {
        try {
            slf4jIntLogger = new Slf4jIntLogger(logger);
            hubServerConfig = createHubServerConfig(slf4jIntLogger);
            hubServicesFactory = createHubServicesFactory(slf4jIntLogger, hubServerConfig);
        } catch (IllegalStateException | EncryptionException e) {
            throw new DetectUserFriendlyException(String.format("Not able to initialize Hub connection: %s", e.getMessage()), e, ExitCodeType.FAILURE_HUB_CONNECTIVITY);
        }
        final HubService hubService = createHubService();
        final CurrentVersionView currentVersion = hubService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
        logger.info(String.format("Successfully connected to Hub (version %s)!", currentVersion.version));
    }

    public boolean testHubConnection(final IntLogger intLogger) {
        try {
            assertHubConnection(intLogger);
            return true;
        } catch (final IntegrationException e) {
            intLogger.error(String.format("Could not reach the Hub server or the credentials were invalid: %s", e.getMessage()), e);
        }
        return false;
    }

    public void assertHubConnection(final IntLogger intLogger) throws IntegrationException {
        logger.info("Attempting connection to the Hub");
        RestConnection connection = null;

        try {
            final HubServerConfig hubServerConfig = createHubServerConfig(intLogger);
            connection = hubServerConfig.createRestConnection(intLogger);
            connection.connect();
            logger.info("Connection to the Hub was successful");
        } catch (final IllegalStateException e) {
            throw new IntegrationException(e.getMessage(), e);
        } finally {
            ResourceUtil.closeQuietly(connection);
        }
    }

    public HubService createHubService() {
        return hubServicesFactory.createHubService();
    }

    public ProjectService createProjectService() {
        return hubServicesFactory.createProjectService();
    }

    public PhoneHomeService createPhoneHomeService() {
        return hubServicesFactory.createPhoneHomeService();
    }

    public CodeLocationService createCodeLocationService() {
        return hubServicesFactory.createCodeLocationService();
    }

    public ScanStatusService createScanStatusService() {
        return hubServicesFactory.createScanStatusService(detectConfigWrapper.getLongProperty(DetectProperty.DETECT_API_TIMEOUT));
    }

    public ReportService createReportService() throws IntegrationException {
        return hubServicesFactory.createReportService(detectConfigWrapper.getLongProperty(DetectProperty.DETECT_API_TIMEOUT));
    }

    public SignatureScannerService createSignatureScannerService(final ExecutorService executorService) {
        return hubServicesFactory.createSignatureScannerService(executorService);
    }

    private HubServicesFactory createHubServicesFactory(final IntLogger slf4jIntLogger, final HubServerConfig hubServerConfig) throws IntegrationException {
        final BlackduckRestConnection restConnection = hubServerConfig.createRestConnection(slf4jIntLogger);

        return new HubServicesFactory(gson, jsonParser, restConnection, slf4jIntLogger);
    }

    private HubServerConfig createHubServerConfig(final IntLogger slf4jIntLogger) {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setLogger(slf4jIntLogger);

        final Map<String, String> blackduckHubProperties = additionalPropertyConfig.getBlackduckHubProperties();
        hubServerConfigBuilder.setFromProperties(blackduckHubProperties);

        return hubServerConfigBuilder.build();
    }

    public HubServerConfig getHubServerConfig() {
        return hubServerConfig;
    }

    public HubServicesFactory getHubServicesFactory() {
        return hubServicesFactory;
    }

}
