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

import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.bdio.simple.model.Forge
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.NameVersionExternalId
import com.blackducksoftware.integration.hub.detect.bomtool.nuget.NugetInspectorPackager
import com.blackducksoftware.integration.hub.detect.type.BomToolType
import com.blackducksoftware.integration.hub.detect.type.ExecutableType

@Component
class NugetBomTool extends BomTool {
    private final Logger logger = LoggerFactory.getLogger(NugetBomTool.class)

    static final String SOLUTION_PATTERN = '*.sln'
    static final String PROJECT_PATTERN = '*.*proj'

    @Autowired
    NugetInspectorPackager nugetInspectorPackager

    String nugetExecutablePath
    List<String> matchingSourcePaths = []

    BomToolType getBomToolType() {
        return BomToolType.NUGET
    }

    boolean isBomToolApplicable() {
        nugetExecutablePath = findNugetExecutable()
        detectConfiguration.sourcePaths.each { sourcePath ->
            def solutionFile = detectFileManager.findFile(sourcePath, SOLUTION_PATTERN)
            def projectFile = detectFileManager.findFile(sourcePath, PROJECT_PATTERN)
            if (solutionFile || projectFile) {
                matchingSourcePaths.add(sourcePath)
            }
        }

        if (!matchingSourcePaths.isEmpty() && !nugetExecutablePath) {
            logger.warn('The nuget executable must be on the path - are you sure you are running on a windows system?')
        }

        nugetExecutablePath && !matchingSourcePaths.isEmpty()
    }

    List<DependencyNode> extractDependencyNodes() {
        List<DependencyNode> projectNodes = []
        matchingSourcePaths.each { sourcePath ->
            DependencyNode root = nugetInspectorPackager.makeDependencyNode(sourcePath, nugetExecutablePath)
            if (!root) {
                logger.info('Unable to extract any dependencies from nuget')
            } else {
                if (isSolution(root)) {
                    root.name = projectInfoGatherer.getDefaultProjectName(BomToolType.NUGET, sourcePath, root.name)
                    root.version = projectInfoGatherer.getDefaultProjectVersionName(root.version)
                    root.externalId = new NameVersionExternalId(Forge.NUGET, root.name, root.version)
                    if (detectConfiguration.getNugetAggregateBom()) {
                        projectNodes.add(root)
                    } else {
                        projectNodes.addAll(root.children as List)
                    }
                } else {
                    root.name = projectInfoGatherer.getDefaultProjectName(BomToolType.NUGET, sourcePath, root.name)
                    root.version = projectInfoGatherer.getDefaultProjectVersionName(root.version)
                    root.externalId = new NameVersionExternalId(Forge.NUGET, root.name, root.version)
                    projectNodes.add(root)
                }
            }
        }

        projectNodes
    }

    boolean isSolution(DependencyNode root) {
        root.children != null && root.children.size() > 0 && root.children[0].children != null && root.children[0].children.size() > 0
    }

    private File findNugetExecutable() {
        if (StringUtils.isNotBlank(detectConfiguration.getNugetPath())) {
            new File(detectConfiguration.getNugetPath())
        } else {
            executableManager.getExecutable(ExecutableType.NUGET)
        }
    }
}