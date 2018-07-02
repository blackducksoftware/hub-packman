/*
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
package com.blackducksoftware.integration.hub.detect.bomtool.nuget.parse

import java.nio.charset.StandardCharsets

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.model.Forge
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory
import com.blackducksoftware.integration.hub.detect.bomtool.BomToolGroupType
import com.blackducksoftware.integration.hub.detect.bomtool.BomToolType
import com.blackducksoftware.integration.hub.detect.bomtool.nuget.model.NugetContainer
import com.blackducksoftware.integration.hub.detect.bomtool.nuget.model.NugetContainerType
import com.blackducksoftware.integration.hub.detect.bomtool.nuget.model.NugetInspection
import com.blackducksoftware.integration.hub.detect.nameversion.NameVersionNodeTransformer
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner
import com.blackducksoftware.integration.hub.detect.workflow.codelocation.DetectCodeLocation
import com.google.gson.Gson

import groovy.transform.TypeChecked

@Component
@TypeChecked
class NugetInspectorPackager {
    private final Logger logger = LoggerFactory.getLogger(NugetInspectorPackager.class)

    private final DetectFileManager detectFileManager
    private final ExecutableRunner executableRunner
    private final Gson gson
    private final NameVersionNodeTransformer nameVersionNodeTransformer
    private final ExternalIdFactory externalIdFactory

    @Autowired
    NugetInspectorPackager(final DetectFileManager detectFileManager, final ExecutableRunner executableRunner, final Gson gson, final NameVersionNodeTransformer nameVersionNodeTransformer, final ExternalIdFactory externalIdFactory) {
        this.detectFileManager = detectFileManager
        this.executableRunner = executableRunner
        this.gson = gson
        this.nameVersionNodeTransformer = nameVersionNodeTransformer
        this.externalIdFactory = externalIdFactory
    }

    public NugetParseResult createDetectCodeLocation(BomToolType bomToolType, File dependencyNodeFile) {
        String text = dependencyNodeFile.getText(StandardCharsets.UTF_8.toString())
        NugetInspection nugetInspection = gson.fromJson(text, NugetInspection.class)

        def codeLocations = new ArrayList<DetectCodeLocation>()
        def projectName
        def projectVersion
        nugetInspection.containers.each {
            def result = createDetectCodeLocationFromNugetContainer(bomToolType, it)
            if (result.projectName) {
                projectName = result.projectName;
                projectVersion = result.projectVersion;
            }
            codeLocations.addAll(result.codeLocations)
        }

        new NugetParseResult(projectName, projectVersion, codeLocations)
    }

    private NugetParseResult createDetectCodeLocationFromNugetContainer(BomToolType bomToolType, NugetContainer nugetContainer) {
        String projectName = ''
        String projectVersionName = ''
        if (NugetContainerType.SOLUTION == nugetContainer.type) {
            projectName = nugetContainer.name
            projectVersionName = nugetContainer.version
            def codeLocations = nugetContainer.children.collect { container ->
                def builder = new NugetDependencyNodeBuilder(externalIdFactory)
                builder.addPackageSets(container.packages)
                def children = builder.createDependencyGraph(container.dependencies)
                def sourcePath = container.sourcePath

                if (!projectVersionName) {
                    projectVersionName = container.version
                }
                new DetectCodeLocation.Builder(BomToolGroupType.NUGET, bomToolType, sourcePath, externalIdFactory.createNameVersionExternalId(Forge.NUGET, projectName, projectVersionName), children).build()
            }
            return new NugetParseResult(projectName, projectVersionName, codeLocations);
        } else if (NugetContainerType.PROJECT == nugetContainer.type) {
            projectName = nugetContainer.name
            projectVersionName = nugetContainer.version
            String sourcePath = nugetContainer.sourcePath
            def builder = new NugetDependencyNodeBuilder(externalIdFactory)
            builder.addPackageSets(nugetContainer.packages)
            def children = builder.createDependencyGraph(nugetContainer.dependencies)

            DetectCodeLocation codeLocation = new DetectCodeLocation.Builder(BomToolGroupType.NUGET, bomToolType, sourcePath, externalIdFactory.createNameVersionExternalId(Forge.NUGET, projectName, projectVersionName), children).build();
            return new NugetParseResult(projectName, projectVersionName, codeLocation);
        }
    }
}