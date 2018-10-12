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
package com.blackducksoftware.integration.hub.detect.bomtool.bitbake;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.detect.bomtool.BomToolGroupType;
import com.blackducksoftware.integration.hub.detect.bomtool.BomToolType;
import com.blackducksoftware.integration.hub.detect.bomtool.ExtractionId;
import com.blackducksoftware.integration.hub.detect.configuration.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.configuration.DetectProperty;
import com.blackducksoftware.integration.hub.detect.type.ExecutableType;
import com.blackducksoftware.integration.hub.detect.util.DetectFileFinder;
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager;
import com.blackducksoftware.integration.hub.detect.util.executable.Executable;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableManager;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableOutput;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunnerException;
import com.blackducksoftware.integration.hub.detect.workflow.codelocation.DetectCodeLocation;
import com.blackducksoftware.integration.hub.detect.workflow.extraction.Extraction;
import com.paypal.digraph.parser.GraphParser;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.hub.bdio.graph.DependencyGraph;
import com.synopsys.integration.hub.bdio.model.externalid.ExternalId;

public class BitbakeExtractor {
    public static final String RECIPE_DEPENDS_FILE_NAME = "recipe-depends.dot";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExecutableManager executableManager;
    private final ExecutableRunner executableRunner;
    private final DetectConfiguration detectConfiguration;
    private final DetectFileManager detectFileManager;
    private final DetectFileFinder detectFileFinder;
    private final GraphParserTransformer graphParserTransformer;
    private final BitbakeListTasksParser bitbakeListTasksParser;

    public BitbakeExtractor(final ExecutableManager executableManager, final ExecutableRunner executableRunner, final DetectConfiguration detectConfiguration, final DetectFileManager detectFileManager,
        final DetectFileFinder detectFileFinder, final GraphParserTransformer graphParserTransformer, final BitbakeListTasksParser bitbakeListTasksParser) {
        this.executableManager = executableManager;
        this.executableRunner = executableRunner;
        this.detectConfiguration = detectConfiguration;
        this.detectFileManager = detectFileManager;
        this.detectFileFinder = detectFileFinder;
        this.graphParserTransformer = graphParserTransformer;
        this.bitbakeListTasksParser = bitbakeListTasksParser;
    }

    public Extraction extract(final ExtractionId extractionId, final String foundBuildEnvScriptPath, final String sourcePath) {
        final File outputDirectory = detectFileManager.getOutputDirectory(extractionId);
        final File bitbakeBuildDirectory = new File(outputDirectory, "build");
        final String[] packageNames = detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_BITBAKE_PACKAGE_NAMES);

        final List<DetectCodeLocation> detectCodeLocations = new ArrayList<>();
        for (final String packageName : packageNames) {
            final File recipeDependsFile = executeBitbakeForRecipeDependsFile(outputDirectory, bitbakeBuildDirectory, foundBuildEnvScriptPath, packageName);
            final Optional<String> targetArchitecture = executeBitbakeForTargetArchitecture(outputDirectory, foundBuildEnvScriptPath, packageName);

            try {
                if (!targetArchitecture.isPresent()) {
                    throw new IntegrationException("Failed to find a target architecture");
                }

                final InputStream recipeDependsInputStream = FileUtils.openInputStream(recipeDependsFile);
                final GraphParser graphParser = new GraphParser(recipeDependsInputStream);
                final DependencyGraph dependencyGraph = graphParserTransformer.transform(graphParser, targetArchitecture.get());
                final ExternalId externalId = new ExternalId(BitbakeBomTool.YOCTO_FORGE);
                final DetectCodeLocation detectCodeLocation = new DetectCodeLocation.Builder(BomToolGroupType.BITBAKE, BomToolType.BITBAKE_CLI, sourcePath, externalId, dependencyGraph).build();

                detectCodeLocations.add(detectCodeLocation);
            } catch (final IOException | IntegrationException e) {
                logger.error(String.format("Failed to extract a Code Location while running Bitbake against package '%s'", packageName));
                logger.debug(e.getMessage(), e);
            }
        }

        final Extraction extraction;

        if (detectCodeLocations.isEmpty()) {
            extraction = new Extraction.Builder()
                             .failure("No Code Locations were generated during extraction")
                             .build();
        } else {
            extraction = new Extraction.Builder()
                             .success(detectCodeLocations)
                             .build();
        }

        return extraction;
    }

    private File executeBitbakeForRecipeDependsFile(final File outputDirectory, final File bitbakeBuildDirectory, final String foundBuildEnvScriptPath, final String packageName) {
        final String bitbakeCommand = "bitbake -g " + packageName;
        final ExecutableOutput executableOutput = runBitbake(outputDirectory, foundBuildEnvScriptPath, bitbakeCommand);
        final int returnCode = executableOutput.getReturnCode();
        File recipeDependsFile = null;

        if (returnCode == 0) {
            recipeDependsFile = detectFileFinder.findFile(bitbakeBuildDirectory, RECIPE_DEPENDS_FILE_NAME);
        } else {
            logger.error(String.format("Executing command '%s' returned a non-zero exit code %s", bitbakeCommand, returnCode));
        }

        return recipeDependsFile;
    }

    private Optional<String> executeBitbakeForTargetArchitecture(final File outputDirectory, final String foundBuildEnvScriptPath, final String packageName) {
        final String bitbakeCommand = "bitbake -c listtasks " + packageName;
        final ExecutableOutput executableOutput = runBitbake(outputDirectory, foundBuildEnvScriptPath, bitbakeCommand);
        final int returnCode = executableOutput.getReturnCode();
        Optional<String> targetArchitecture = Optional.empty();

        if (returnCode == 0) {
            targetArchitecture = bitbakeListTasksParser.parseTargetArchitecture(executableOutput.getStandardOutput());
        } else {
            logger.error(String.format("Executing command '%s' returned a non-zero exit code %s", bitbakeCommand, returnCode));
        }

        return targetArchitecture;
    }

    private ExecutableOutput runBitbake(final File outputDirectory, final String foundBuildEnvScriptPath, final String bitbakeCommand) {
        final String bashExecutablePath = executableManager.getExecutablePathOrOverride(ExecutableType.BASH, true, "", detectConfiguration.getProperty(DetectProperty.DETECT_BASH_PATH));

        final List<String> arguments = new ArrayList<>();
        arguments.add("-c");
        arguments.add(". " + foundBuildEnvScriptPath + "; " + bitbakeCommand);
        final Executable sourceExecutable = new Executable(outputDirectory, bashExecutablePath, arguments);
        ExecutableOutput executableOutput = null;

        try {
            executableOutput = executableRunner.execute(sourceExecutable);
        } catch (final ExecutableRunnerException e) {
            logger.error(String.format("Failed executing command '%s'", sourceExecutable.getExecutableDescription()));
            logger.debug(e.getMessage(), e);
        }

        return executableOutput;
    }
}
