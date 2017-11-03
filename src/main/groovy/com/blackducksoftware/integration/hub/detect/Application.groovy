/*
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.detect

import javax.annotation.PostConstruct
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean

import com.blackducksoftware.integration.hub.bdio.BdioTransformer
import com.blackducksoftware.integration.hub.bdio.SimpleBdioFactory
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory
import com.blackducksoftware.integration.hub.detect.exception.DetectException
import com.blackducksoftware.integration.hub.detect.help.DetectOption
import com.blackducksoftware.integration.hub.detect.help.DetectOptionManager
import com.blackducksoftware.integration.hub.detect.help.print.DetectConfigurationPrinter
import com.blackducksoftware.integration.hub.detect.help.print.DetectInfoPrinter
import com.blackducksoftware.integration.hub.detect.help.print.HelpHtmlWriter
import com.blackducksoftware.integration.hub.detect.help.print.HelpPrinter
import com.blackducksoftware.integration.hub.detect.hub.HubManager
import com.blackducksoftware.integration.hub.detect.hub.HubServiceWrapper
import com.blackducksoftware.integration.hub.detect.hub.HubSignatureScanner
import com.blackducksoftware.integration.hub.detect.model.DetectProject
import com.blackducksoftware.integration.hub.detect.onboarding.OnboardingManager
import com.blackducksoftware.integration.hub.detect.onboarding.OnboardingOption
import com.blackducksoftware.integration.hub.detect.profile.manager.ProfileManager
import com.blackducksoftware.integration.hub.detect.summary.DetectSummary
import com.blackducksoftware.integration.hub.detect.summary.Result
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableManager
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView
import com.blackducksoftware.integration.log.Slf4jIntLogger
import com.blackducksoftware.integration.util.IntegrationEscapeUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import freemarker.template.Configuration
import groovy.transform.TypeChecked

@TypeChecked
@SpringBootApplication
class Application {
    public static final int FAIL_DETECT = 1

    private final Logger logger = LoggerFactory.getLogger(Application.class)

    @Autowired
    DetectOptionManager detectOptionManager

    @Autowired
    DetectInfo detectInfo

    @Autowired
    DetectConfiguration detectConfiguration

    @Autowired
    ExecutableManager executableManager

    @Autowired
    DetectProjectManager detectProjectManager

    @Autowired
    ApplicationArguments applicationArguments

    @Autowired
    HelpPrinter helpPrinter

    @Autowired
    HelpHtmlWriter helpHtmlWriter

    @Autowired
    HubManager hubManager

    @Autowired
    HubServiceWrapper hubServiceWrapper

    @Autowired
    HubSignatureScanner hubSignatureScanner

    @Autowired
    DetectSummary detectSummary

    @Autowired
    ProfileManager profileManager

    @Autowired
    OnboardingManager onboardingManager

    @Autowired
    DetectFileManager detectFileManager

    static void main(final String[] args) {
        new SpringApplicationBuilder(Application.class).logStartupInfo(false).run(args)
    }

    private List<String> getPossibleSelectedProfilesFromArgs() {
        List<String> profiles = new ArrayList<String>()
        for (String arg : applicationArguments.getSourceArgs()){
            if (!arg.contains("=")){
                if (arg.startsWith("--")){
                    profiles.add(arg.substring(2))
                }
            }
        }
        return profiles
    }

    @PostConstruct
    void init() {
        int postResult = 0
        try {
            detectInfo.init()
            profileManager.init(getPossibleSelectedProfilesFromArgs())
            detectOptionManager.init(profileManager.selectedProfiles)

            List<DetectOption> options = detectOptionManager.getDetectOptions()
            if ('-h' in applicationArguments.getSourceArgs() || '--help' in applicationArguments.getSourceArgs()) {
                helpPrinter.printHelpMessage(System.out, options, profileManager.availableProfiles(), profileManager.selectedProfiles)
                return
            }

            if ('-hdoc' in applicationArguments.getSourceArgs() || '--helpdocument' in applicationArguments.getSourceArgs()) {
                helpHtmlWriter.writeHelpMessage("hub-detect-${detectInfo.detectVersion}-help.html".toString())
                return
            }

            List<OnboardingOption> onboardedOptions = new ArrayList<>()
            if ('-o' in applicationArguments.getSourceArgs() || '--onboard' in applicationArguments.getSourceArgs()) {
                onboardedOptions = onboardingManager.onboard(profileManager.selectedProfiles)
            }

            executableManager.init()
            detectConfiguration.init()

            logger.info('Configuration processed completely.')

            if (!detectConfiguration.suppressConfigurationOutput) {

                DetectInfoPrinter infoPrinter = new DetectInfoPrinter();
                DetectConfigurationPrinter detectConfigurationPrinter = new DetectConfigurationPrinter()

                infoPrinter.printInfo(System.out, detectInfo)
                helpPrinter.printProfiles(System.out, profileManager.availableProfiles(), profileManager.selectedProfiles)
                detectConfigurationPrinter.printDetailedConfiguration(System.out, detectConfiguration, options, onboardedOptions)
            }

            if (detectConfiguration.testConnection) {
                hubServiceWrapper.testHubConnection()
                return
            }

            if (!detectConfiguration.hubOfflineMode) {
                hubServiceWrapper.init()
            }
            DetectProject detectProject = detectProjectManager.createDetectProject()
            List<File> createdBdioFiles = detectProjectManager.createBdioFiles(detectProject)
            if (!detectConfiguration.hubOfflineMode) {
                ProjectVersionView projectVersionView = hubManager.updateHubProjectVersion(detectProject, createdBdioFiles)
                postResult = hubManager.performPostHubActions(detectProject, projectVersionView)
            } else if (!detectConfiguration.hubSignatureScannerDisabled){
                hubSignatureScanner.scanPathsOffline(detectProject)
            }
        } catch (DetectException e) {
            detectSummary.setOverallFailure()
            logger.error('An unrecoverable error occurred - most likely this is due to your environment and/or configuration. Please double check the Hub Detect documentation: https://blackducksoftware.atlassian.net/wiki/x/Y7HtAg')
            logger.error(e.getMessage())
        }

        if (!detectConfiguration.suppressResultsOutput) {
            detectSummary.logResults(new Slf4jIntLogger(logger))
        }

        detectFileManager.cleanupDirectories()

        if (Result.FAILURE == detectSummary.getOverallResult()) {
            postResult = FAIL_DETECT
        }

        System.exit(postResult)
    }

    @Bean
    Gson gson() {
        new GsonBuilder().setPrettyPrinting().create()
    }

    @Bean
    SimpleBdioFactory simpleBdioFactory() {
        new SimpleBdioFactory()
    }

    @Bean
    BdioTransformer bdioTransformer() {
        new BdioTransformer()
    }

    @Bean
    ExternalIdFactory externalIdFactory() {
        simpleBdioFactory().getExternalIdFactory()
    }

    @Bean
    IntegrationEscapeUtil integrationEscapeUtil() {
        new IntegrationEscapeUtil()
    }

    @Bean
    Configuration configuration() {
        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_26)
        configuration.setClassForTemplateLoading(Application.class, '/')
        configuration.setDefaultEncoding('UTF-8')
        configuration.setLogTemplateExceptions(true)

        configuration
    }

    @Bean
    DocumentBuilder xmlDocumentBuilder() {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()

        factory.newDocumentBuilder()
    }
}
