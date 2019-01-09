/**
 * hub-detect
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.detect.tool.bazel;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.detect.configuration.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.configuration.DetectProperty;
import com.blackducksoftware.integration.hub.detect.configuration.PropertyAuthority;
import com.blackducksoftware.integration.hub.detect.detector.ExtractionId;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner;
import com.blackducksoftware.integration.hub.detect.workflow.codelocation.DetectCodeLocation;
import com.blackducksoftware.integration.hub.detect.workflow.extraction.Extraction;

public class BazelExtractor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DetectConfiguration detectConfiguration;
    private final ExecutableRunner executableRunner;
    private final BazelQueryXmlOutputParser parser;
    private final BazelExternalIdExtractionSimpleRules simpleRules;
    private final BazelBdioBuilder bdioGenerator;
    private final BazelExternalIdExtractionXPathRuleJsonProcessor bazelExternalIdExtractionXPathRuleJsonProcessor;

    public BazelExtractor(final DetectConfiguration detectConfiguration, final ExecutableRunner executableRunner, BazelQueryXmlOutputParser parser, final BazelExternalIdExtractionSimpleRules simpleRules,
        final BazelBdioBuilder bdioGenerator, final BazelExternalIdExtractionXPathRuleJsonProcessor bazelExternalIdExtractionXPathRuleJsonProcessor) {
        this.detectConfiguration = detectConfiguration;
        this.executableRunner = executableRunner;
        this.parser = parser;
        this.simpleRules = simpleRules;
        this.bdioGenerator = bdioGenerator;
        this.bazelExternalIdExtractionXPathRuleJsonProcessor = bazelExternalIdExtractionXPathRuleJsonProcessor;
    }

    public Extraction extract(final String bazelExe, final File workspaceDir, final int depth, final ExtractionId extractionId) {
        logger.debug("Bazel extract()");
        try {
            bdioGenerator.setWorkspaceDir(workspaceDir);
            final String xPathRulesPath = detectConfiguration.getProperty(DetectProperty.DETECT_BAZEL_ADVANCED_RULES_PATH, PropertyAuthority.None);
            final String bazelTarget = detectConfiguration.getProperty(DetectProperty.DETECT_BAZEL_TARGET, PropertyAuthority.None);
            List<BazelExternalIdExtractionXPathRule> xPathRules;
            if (StringUtils.isNotBlank(xPathRulesPath)) {
                xPathRules = loadXPathRulesFromFile(xPathRulesPath);
                logger.debug(String.format("Read %d rule(s) from %s", xPathRules.size(), xPathRulesPath));
            } else {
                xPathRules = simpleRules.getRules().stream()
                                      .map(BazelExternalIdExtractionXPathRule::new).collect(Collectors.toList());
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Using default rules:\n%s", bazelExternalIdExtractionXPathRuleJsonProcessor.toJson(xPathRules)));
                }
            }
            BazelExternalIdGenerator externalIdGenerator = new BazelExternalIdGenerator(executableRunner, bazelExe, parser, workspaceDir, bazelTarget);
            xPathRules.stream()
                .map(externalIdGenerator::generate)
                .flatMap(Collection::stream)
                .forEach(bdioGenerator::addDependency);
            if (externalIdGenerator.isErrors()) {
                return new Extraction.Builder().failure(externalIdGenerator.getErrorMessage()).build();
            }
            final List<DetectCodeLocation> codeLocations = bdioGenerator.build();
            final Extraction.Builder builder = new Extraction.Builder().success(codeLocations);
            return builder.build();
        } catch (Exception e) {
            final String msg = String.format("Bazel processing exception: %s", e.getMessage());
            logger.debug(msg, e);
            return new Extraction.Builder().failure(msg).build();
        }
    }

    private List<BazelExternalIdExtractionXPathRule> loadXPathRulesFromFile(final String xPathRulesJsonFilePath) throws IOException {
        final File jsonFile = new File(xPathRulesJsonFilePath);
        List<BazelExternalIdExtractionXPathRule> loadedRules = bazelExternalIdExtractionXPathRuleJsonProcessor.load(jsonFile);
        return loadedRules;
    }
}