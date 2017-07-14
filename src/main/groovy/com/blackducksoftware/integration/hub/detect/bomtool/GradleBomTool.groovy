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
package com.blackducksoftware.integration.hub.detect.bomtool

import java.nio.charset.StandardCharsets

import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.detect.bomtool.output.DetectCodeLocation
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.CompositePrerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.ExecutableExistsPrerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.Operator
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.Prerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.SourceFileExistsPrerequisite
import com.blackducksoftware.integration.hub.detect.hub.HubSignatureScanner
import com.blackducksoftware.integration.hub.detect.type.BomToolType
import com.blackducksoftware.integration.hub.detect.type.ExecutableType
import com.blackducksoftware.integration.hub.detect.util.executable.Executable
import com.google.gson.Gson

@Component
class GradleBomTool extends BomTool {
    private final Logger logger = LoggerFactory.getLogger(GradleBomTool.class)

    static final String BUILD_GRADLE = 'build.gradle'

    @Autowired
    Gson gson

    @Autowired
    HubSignatureScanner hubSignatureScanner

    BomToolType getBomToolType() {
        return BomToolType.GRADLE
    }

    public List<Prerequisite> getPrerequisites() {
        Prerequisite gradleWrapperExists = new ExecutableExistsPrerequisite(executableManager, ExecutableType.GRADLEW, detectConfiguration.getGradlePath())
        Prerequisite gradleExists = new ExecutableExistsPrerequisite(executableManager, ExecutableType.GRADLE, detectConfiguration.getGradlePath())
        Prerequisite gradleWrapperOrGradleExist = new CompositePrerequisite(gradleWrapperExists, gradleExists, Operator.OR)
        gradleWrapperOrGradleExist.setExecutablePrerequisite(true)

        def prerequisites = []

        prerequisites.add(new SourceFileExistsPrerequisite(detectFileManager, detectConfiguration, BUILD_GRADLE))
        prerequisites.add(gradleWrapperOrGradleExist)

        prerequisites
    }

    List<DetectCodeLocation> extractDetectCodeLocations() {
        DependencyNode rootProjectNode = extractRootProjectNode()
        DetectCodeLocation detectCodeLocation = new DetectCodeLocation(getBomToolType(), sourcePath, rootProjectNode)

        //there may also be subprojects, so just look up to two levels down (depth = 3) for any/all build directories
        File[] additionalTargets = detectFileManager.findFilesToDepth(detectConfiguration.sourceDirectory, 'build', 3)
        if (additionalTargets) {
            additionalTargets.each { hubSignatureScanner.registerDirectoryToScan(it) }
        }

        [detectCodeLocation]
    }

    DependencyNode extractRootProjectNode() {
        File initScriptFile = detectFileManager.createFile(BomToolType.GRADLE, 'init-detect.gradle')
        String initScriptContents = getClass().getResourceAsStream('/init-script-gradle').getText(StandardCharsets.UTF_8.name())
        initScriptContents = initScriptContents.replace('GRADLE_INSPECTOR_VERSION', detectConfiguration.getGradleInspectorVersion())
        initScriptContents = initScriptContents.replace('EXCLUDED_PROJECT_NAMES', detectConfiguration.getGradleExcludedProjectNames())
        initScriptContents = initScriptContents.replace('INCLUDED_PROJECT_NAMES', detectConfiguration.getGradleIncludedProjectNames())
        initScriptContents = initScriptContents.replace('EXCLUDED_CONFIGURATION_NAMES', detectConfiguration.getGradleExcludedConfigurationNames())
        initScriptContents = initScriptContents.replace('INCLUDED_CONFIGURATION_NAMES', detectConfiguration.getGradleIncludedConfigurationNames())

        detectFileManager.writeToFile(initScriptFile, initScriptContents)
        String initScriptPath = initScriptFile.absolutePath
        logger.info("using ${initScriptPath} as the path for the gradle init script")
        String gradleExecutable = findGradleExecutable()
        Executable executable = new Executable(sourceDirectory, gradleExecutable, [
            detectConfiguration.getGradleBuildCommand(),
            "--init-script=${initScriptPath}"
        ])
        executableRunner.execute(executable)

        File buildDirectory = new File(sourcePath, 'build')
        File blackduckDirectory = new File(buildDirectory, 'blackduck')
        File dependencyNodeFile = new File(blackduckDirectory, 'dependencyNodes.json')
        String dependencyNodeJson = dependencyNodeFile.getText(StandardCharsets.UTF_8.name())
        DependencyNode rootProjectDependencyNode = gson.fromJson(dependencyNodeJson, DependencyNode.class)

        if (detectConfiguration.gradleCleanupBuildBlackduckDirectory) {
            blackduckDirectory.deleteDir()
        }

        rootProjectDependencyNode
    }

    private String findGradleExecutable() {
        String gradlePath = detectConfiguration.getGradlePath()
        if (StringUtils.isBlank(gradlePath)) {
            logger.debug('detect.gradle.path not set in config - first try to find the gradle wrapper')
            gradlePath = executableManager.getPathOfExecutable(ExecutableType.GRADLEW)
        }

        if (StringUtils.isBlank(gradlePath)) {
            logger.debug('gradle wrapper not found - trying to find gradle on the PATH')
            gradlePath = executableManager.getPathOfExecutable(ExecutableType.GRADLE)
        }

        gradlePath
    }

}