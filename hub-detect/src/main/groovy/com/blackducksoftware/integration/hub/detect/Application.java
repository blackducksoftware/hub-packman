/**
 * hub-detect
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
package com.blackducksoftware.integration.hub.detect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.detect.configuration.AdditionalPropertyConfig;
import com.blackducksoftware.integration.hub.detect.configuration.ConfigurationManager;
import com.blackducksoftware.integration.hub.detect.configuration.DetectConfigWrapper;
import com.blackducksoftware.integration.hub.detect.configuration.DetectProperty;
import com.blackducksoftware.integration.hub.detect.exception.DetectUserFriendlyException;
import com.blackducksoftware.integration.hub.detect.exitcode.ExitCodeReporter;
import com.blackducksoftware.integration.hub.detect.exitcode.ExitCodeType;
import com.blackducksoftware.integration.hub.detect.help.ArgumentState;
import com.blackducksoftware.integration.hub.detect.help.ArgumentStateParser;
import com.blackducksoftware.integration.hub.detect.help.DetectOption;
import com.blackducksoftware.integration.hub.detect.help.DetectOption.OptionValidationResult;
import com.blackducksoftware.integration.hub.detect.help.DetectOptionManager;
import com.blackducksoftware.integration.hub.detect.help.html.HelpHtmlWriter;
import com.blackducksoftware.integration.hub.detect.help.print.HelpPrinter;
import com.blackducksoftware.integration.hub.detect.hub.HubServiceWrapper;
import com.blackducksoftware.integration.hub.detect.interactive.InteractiveManager;
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager;
import com.blackducksoftware.integration.hub.detect.workflow.DetectProjectManager;
import com.blackducksoftware.integration.hub.detect.workflow.PhoneHomeManager;
import com.blackducksoftware.integration.hub.detect.workflow.diagnostic.DetectRunManager;
import com.blackducksoftware.integration.hub.detect.workflow.diagnostic.DiagnosticManager;
import com.blackducksoftware.integration.hub.detect.workflow.hub.HubManager;
import com.blackducksoftware.integration.hub.detect.workflow.project.DetectProject;
import com.blackducksoftware.integration.hub.detect.workflow.summary.DetectSummaryManager;
import com.blackducksoftware.integration.log.SilentLogger;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@SpringBootApplication
@Import({ BeanConfiguration.class })
public class Application implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(Application.class);

    private final DetectOptionManager detectOptionManager;
    private final DetectInfo detectInfo;
    private final AdditionalPropertyConfig additionalPropertyConfig;
    private final DetectConfigWrapper detectConfigWrapper;
    private final ConfigurationManager configurationManager;
    private final DetectProjectManager detectProjectManager;
    private final HelpPrinter helpPrinter;
    private final HelpHtmlWriter helpHtmlWriter;
    private final HubManager hubManager;
    private final HubServiceWrapper hubServiceWrapper;
    private final DetectSummaryManager detectSummaryManager;
    private final InteractiveManager interactiveManager;
    private final DetectFileManager detectFileManager;
    private final List<ExitCodeReporter> exitCodeReporters;
    private final PhoneHomeManager phoneHomeManager;
    private final ArgumentStateParser argumentStateParser;
    private final DiagnosticManager diagnosticManager;
    private final DetectRunManager detectRunManager;

    private enum WorkflowStep {
        EXIT_WITH_SUCCESS,
        RUN_DETECT;
    }

    @Autowired
    public Application(final DetectOptionManager detectOptionManager, final DetectInfo detectInfo, final AdditionalPropertyConfig additionalPropertyConfig, final DetectConfigWrapper detectConfigWrapper,
            final ConfigurationManager configurationManager, final DetectProjectManager detectProjectManager, final HelpPrinter helpPrinter, final HelpHtmlWriter helpHtmlWriter, final HubManager hubManager,
            final HubServiceWrapper hubServiceWrapper, final DetectSummaryManager detectSummaryManager, final InteractiveManager interactiveManager, final DetectFileManager detectFileManager,
            final List<ExitCodeReporter> exitCodeReporters, final PhoneHomeManager phoneHomeManager, final ArgumentStateParser argumentStateParser, final DetectRunManager detectRunManager, final DiagnosticManager diagnosticManager) {
        this.detectOptionManager = detectOptionManager;
        this.detectInfo = detectInfo;
        this.additionalPropertyConfig = additionalPropertyConfig;
        this.detectConfigWrapper = detectConfigWrapper;
        this.configurationManager = configurationManager;
        this.detectProjectManager = detectProjectManager;
        this.helpPrinter = helpPrinter;
        this.helpHtmlWriter = helpHtmlWriter;
        this.hubManager = hubManager;
        this.hubServiceWrapper = hubServiceWrapper;
        this.detectSummaryManager = detectSummaryManager;
        this.interactiveManager = interactiveManager;
        this.detectFileManager = detectFileManager;
        this.exitCodeReporters = exitCodeReporters;
        this.phoneHomeManager = phoneHomeManager;
        this.argumentStateParser = argumentStateParser;
        this.detectRunManager = detectRunManager;
        this.diagnosticManager = diagnosticManager;
    }

    public static void main(final String[] args) {
        new SpringApplicationBuilder(Application.class).logStartupInfo(false).run(args);
    }

    @Override
    public void run(final ApplicationArguments applicationArguments) throws Exception {
        final long startTime = System.currentTimeMillis();

        ExitCodeType detectExitCode = ExitCodeType.SUCCESS;
        try {
            final WorkflowStep nextWorkflowStep = initializeDetect(applicationArguments.getSourceArgs());
            if (nextWorkflowStep == WorkflowStep.RUN_DETECT) {
                runDetect();
                detectExitCode = getExitCodeFromCompletedRun(detectExitCode);
            }
        } catch (final Exception e) {
            detectExitCode = getExitCodeFromExceptionDetails(e);
        } finally {
            cleanupRun(detectExitCode);
        }

        endRun(startTime, detectExitCode);
    }

    private WorkflowStep initializeDetect(final String[] sourceArgs) throws IntegrationException, DetectUserFriendlyException {
        detectInfo.init();
        detectRunManager.init();
        additionalPropertyConfig.init();
        detectConfigWrapper.init();
        detectOptionManager.init();

        final List<DetectOption> options = detectOptionManager.getDetectOptions();

        final ArgumentState argumentState = argumentStateParser.parseArgs(sourceArgs);

        if (argumentState.isHelp() || argumentState.isDeprecatedHelp() || argumentState.isVerboseHelp()) {
            helpPrinter.printAppropriateHelpMessage(System.out, options, argumentState);
            return WorkflowStep.EXIT_WITH_SUCCESS;
        }

        if (argumentState.isHelpDocument()) {
            helpHtmlWriter.writeHelpMessage(String.format("hub-detect-%s-help.html", detectInfo.getDetectVersion()));
            return WorkflowStep.EXIT_WITH_SUCCESS;
        }

        if (argumentState.isInteractive()) {
            interactiveManager.configureInInteractiveMode();
        }

        final List<String> defaultBdioLocation = new ArrayList<>();
        defaultBdioLocation.add("bdio");
        if (argumentState.isDiagnostic()) {
            defaultBdioLocation.add(detectRunManager.getRunId());
        }
        configurationManager.initialize(options, defaultBdioLocation);
        detectOptionManager.postInit();

        logger.info("Configuration processed completely.");

        diagnosticManager.init(argumentState.isDiagnostic(), argumentState.isDiagnosticProtected());

        if (!detectConfigWrapper.getBooleanProperty(DetectProperty.DETECT_SUPPRESS_CONFIGURATION_OUTPUT)) {
            configurationManager.printConfiguration(System.out, detectInfo, options);
        }

        if (detectConfigWrapper.getBooleanProperty(DetectProperty.DETECT_FAIL_CONFIG_WARNING) && options.stream().anyMatch(DetectOption::hasWarnings)) {
            throw new DetectUserFriendlyException("Failing because the configuration had warnings.", ExitCodeType.FAILURE_CONFIGURATION);
        }

        final List<OptionValidationResult> invalidDetectOptionResults = detectOptionManager.getAllInvalidOptionResults();
        if (!invalidDetectOptionResults.isEmpty()) {
            throw new DetectUserFriendlyException(invalidDetectOptionResults.get(0).getValidationMessage(), ExitCodeType.FAILURE_GENERAL_ERROR);
        }

        if (detectConfigWrapper.getBooleanProperty(DetectProperty.DETECT_TEST_CONNECTION)) {
            hubServiceWrapper.assertHubConnection(new SilentLogger());
            return WorkflowStep.EXIT_WITH_SUCCESS;
        }

        if (detectConfigWrapper.getBooleanProperty(DetectProperty.DETECT_DISABLE_WITHOUT_HUB) && !hubServiceWrapper.testHubConnection(new SilentLogger())) {
            logger.info(String.format("%s is set to 'true' so Detect will not run.", DetectProperty.DETECT_DISABLE_WITHOUT_HUB.getPropertyName()));
            return WorkflowStep.EXIT_WITH_SUCCESS;
        }

        if (detectConfigWrapper.getBooleanProperty(DetectProperty.BLACKDUCK_HUB_OFFLINE_MODE)) {
            phoneHomeManager.initOffline();
        } else {
            hubServiceWrapper.init();
            phoneHomeManager.init(hubServiceWrapper.createPhoneHomeService());
            phoneHomeManager.startPhoneHome();
        }

        return WorkflowStep.RUN_DETECT;
    }

    private void runDetect() throws IntegrationException, DetectUserFriendlyException, InterruptedException {
        final DetectProject detectProject = detectProjectManager.createDetectProject();

        logger.info(String.format("Project Name: %s", detectProject.getProjectName()));
        logger.info(String.format("Project Version Name: %s", detectProject.getProjectVersion()));

        if (detectConfigWrapper.getBooleanProperty(DetectProperty.BLACKDUCK_HUB_OFFLINE_MODE)) {
            hubManager.performOfflineHubActions(detectProject);
            for (final File bdio : detectProject.getBdioFiles()) {
                diagnosticManager.registerGlobalFileOfInterest(bdio);
            }
        } else {
            final ProjectVersionView projectVersionView = hubManager.updateHubProjectVersion(detectProject);
            hubManager.performPostHubActions(detectProject, projectVersionView);
        }
    }

    private ExitCodeType getExitCodeFromCompletedRun(final ExitCodeType initialExitCodeType) {
        ExitCodeType completedRunExitCodeType = initialExitCodeType;

        for (final ExitCodeReporter exitCodeReporter : exitCodeReporters) {
            completedRunExitCodeType = ExitCodeType.getWinningExitCodeType(completedRunExitCodeType, exitCodeReporter.getExitCodeType());
        }

        return completedRunExitCodeType;
    }

    private ExitCodeType getExitCodeFromExceptionDetails(final Exception e) {
        final ExitCodeType exceptionExitCodeType;

        if (e instanceof DetectUserFriendlyException) {
            if (e.getCause() != null) {
                logger.debug(e.getCause().getMessage(), e.getCause());
            }
            final DetectUserFriendlyException friendlyException = (DetectUserFriendlyException) e;
            exceptionExitCodeType = friendlyException.getExitCodeType();
        } else if (e instanceof IntegrationException) {
            logger.error("An unrecoverable error occurred - most likely this is due to your environment and/or configuration. Please double check the Hub Detect documentation: https://blackducksoftware.atlassian.net/wiki/x/Y7HtAg");
            logger.debug(e.getMessage(), e);
            exceptionExitCodeType = ExitCodeType.FAILURE_GENERAL_ERROR;
        } else {
            logger.error("An unknown/unexpected error occurred");
            logger.debug(e.getMessage(), e);
            exceptionExitCodeType = ExitCodeType.FAILURE_UNKNOWN_ERROR;
        }
        logger.error(e.getMessage());

        return exceptionExitCodeType;
    }

    private void cleanupRun(final ExitCodeType currentExitCodeType) {
        try {
            phoneHomeManager.endPhoneHome();
        } catch (final Exception e) {
            logger.debug(String.format("Error trying to end the phone home task: %s", e.getMessage()));
        }

        if (!detectConfigWrapper.getBooleanProperty(DetectProperty.DETECT_SUPPRESS_RESULTS_OUTPUT)) {
            detectSummaryManager.logDetectResults(new Slf4jIntLogger(logger), currentExitCodeType);
        }

        detectFileManager.cleanup();
    }

    private void endRun(final long startTime, final ExitCodeType finalExitCodeType) {
        final long endTime = System.currentTimeMillis();
        final int finalExitCode = finalExitCodeType.getExitCode();

        logger.info(String.format("Hub-Detect run duration: %s", DurationFormatUtils.formatPeriod(startTime, endTime, "HH'h' mm'm' ss's' SSS'ms'")));

        try { // diagnostics manager must finish as close to the true end as possible.
            diagnosticManager.finish();
        } catch (final Exception e) {
            logger.error("Failed to finish diagnostic mode.");
        }

        if (detectConfigWrapper.getBooleanProperty(DetectProperty.DETECT_FORCE_SUCCESS) && finalExitCode != 0) {
            logger.warn(String.format("Forcing success: Exiting with exit code 0. Ignored exit code was %s.", finalExitCode));
            System.exit(0);
        } else if (finalExitCode != 0) {
            logger.error(String.format("Exiting with code %s - %s", finalExitCode, finalExitCodeType.toString()));
        }

        System.exit(finalExitCode);
    }

}
