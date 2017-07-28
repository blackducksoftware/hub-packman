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

import java.nio.charset.StandardCharsets

import javax.annotation.PostConstruct

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean

import com.blackducksoftware.integration.hub.bdio.simple.BdioNodeFactory
import com.blackducksoftware.integration.hub.bdio.simple.BdioPropertyHelper
import com.blackducksoftware.integration.hub.bdio.simple.DependencyNodeTransformer
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.ExternalId
import com.blackducksoftware.integration.hub.detect.bomtool.output.DetectProject
import com.blackducksoftware.integration.hub.detect.help.HelpPrinter
import com.blackducksoftware.integration.hub.detect.help.ValueDescriptionAnnotationFinder
import com.blackducksoftware.integration.hub.detect.hub.HubManager
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableManager
import com.blackducksoftware.integration.util.IntegrationEscapeUtil
import com.blackducksoftware.integration.util.ResourceUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder

@SpringBootApplication
class Application {
    private final Logger logger = LoggerFactory.getLogger(Application.class)
    public static final String VERSION = ResourceUtil.getResourceAsString('version.txt', StandardCharsets.UTF_8)

    @Autowired
    ValueDescriptionAnnotationFinder valueDescriptionAnnotationFinder

    @Autowired
    DetectConfiguration detectConfiguration

    @Autowired
    ExecutableManager executableManager

    @Autowired
    DetectProjectManager detectProjectManager

    @Autowired
    BdioPropertyHelper bdioPropertyHelper

    @Autowired
    BdioNodeFactory bdioNodeFactory

    @Autowired
    ApplicationArguments applicationArguments

    @Autowired
    HelpPrinter helpPrinter

    @Autowired
    HubManager hubManager

    static void main(final String[] args) {
        new SpringApplicationBuilder(Application.class).logStartupInfo(false).run(args)
    }

    @PostConstruct
    void init() {
        valueDescriptionAnnotationFinder.init()
        if ('-h' in applicationArguments.getSourceArgs() || '--help' in applicationArguments.getSourceArgs()) {
            helpPrinter.printHelpMessage(System.out)
        } else {
            detectConfiguration.init()
            executableManager.init()
            logger.info('Configuration processed completely.')
            if (Boolean.FALSE == detectConfiguration.suppressConfigurationOutput) {
                detectConfiguration.logConfiguration()
            }
            DetectProject detectProject = detectProjectManager.createDetectProject()
            List<File> createdBdioFiles = detectProjectManager.createBdioFiles(detectProject)
            int postResult = hubManager.performPostActions(detectProject, createdBdioFiles)
            System.exit(postResult)
        }
    }

    @Bean
    Gson gson() {
        new GsonBuilder().setPrettyPrinting().registerTypeAdapter(ExternalId.class, new ExternalIdTypeAdapter()).create()
    }

    @Bean
    BdioPropertyHelper bdioPropertyHelper() {
        new BdioPropertyHelper()
    }

    @Bean
    BdioNodeFactory bdioNodeFactory() {
        new BdioNodeFactory(bdioPropertyHelper)
    }

    @Bean
    DependencyNodeTransformer dependencyNodeTransformer() {
        new DependencyNodeTransformer(bdioNodeFactory(), bdioPropertyHelper())
    }

    @Bean
    IntegrationEscapeUtil integrationEscapeUtil() {
        new IntegrationEscapeUtil()
    }
}
