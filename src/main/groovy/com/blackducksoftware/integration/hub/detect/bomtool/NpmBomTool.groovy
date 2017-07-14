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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.detect.bomtool.npm.NpmCliDependencyFinder
import com.blackducksoftware.integration.hub.detect.bomtool.output.DetectCodeLocation
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.ExecutableExistsPrerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.Prerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.SourceFileExistsPrerequisite
import com.blackducksoftware.integration.hub.detect.type.BomToolType
import com.blackducksoftware.integration.hub.detect.type.ExecutableType

@Component
class NpmBomTool extends BomTool {
    private final Logger logger = LoggerFactory.getLogger(NpmBomTool.class)

    public static final String NODE_MODULES = 'node_modules'
    public static final String PACKAGE_JSON = 'package.json'
    public static final String OUTPUT_FILE = 'detect_npm_proj_dependencies.json'
    public static final String ERROR_FILE = 'detect_npm_error.json'

    @Autowired
    NpmCliDependencyFinder cliDependencyFinder

    @Override
    public BomToolType getBomToolType() {
        BomToolType.NPM
    }

    public List<Prerequisite> getPrerequisites() {
        def prerequisites = []

        prerequisites.add(new SourceFileExistsPrerequisite(detectFileManager, detectConfiguration, NODE_MODULES))
        prerequisites.add(new SourceFileExistsPrerequisite(detectFileManager, detectConfiguration, PACKAGE_JSON))
        prerequisites.add(new ExecutableExistsPrerequisite(executableManager, ExecutableType.NPM, detectConfiguration.getNpmPath()))

        prerequisites
    }

    List<DetectCodeLocation> extractDetectCodeLocations() {
        String npmExePath = executableManager.getPathOfExecutable(ExecutableType.NPM, detectConfiguration.getNpmPath())

        File npmLsOutputFile = detectFileManager.createFile(BomToolType.NPM, NpmBomTool.OUTPUT_FILE)
        File npmLsErrorFile = detectFileManager.createFile(BomToolType.NPM, NpmBomTool.ERROR_FILE)
        def npmExe = executableRunner.runExeToFile(npmExePath, npmLsOutputFile, npmLsErrorFile, 'ls', '-json')

        if (npmLsErrorFile.length() == 0) {
            def dependencyNode = cliDependencyFinder.generateDependencyNode(npmLsOutputFile)
            def detectCodeLocation = new DetectCodeLocation(getBomToolType(), sourcePath, dependencyNode)

            return [detectCodeLocation]
        } else {
            logger.error("Error when running npm ls command\n${npmLsErrorFile.text}")
        }

        []
    }
}
