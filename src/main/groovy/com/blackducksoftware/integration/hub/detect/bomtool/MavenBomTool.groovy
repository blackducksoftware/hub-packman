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
import com.blackducksoftware.integration.hub.detect.bomtool.maven.MavenPackager
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
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableOutput

@Component
class MavenBomTool extends BomTool {
    private final Logger logger = LoggerFactory.getLogger(MavenBomTool.class)

    static final String POM_FILENAME = 'pom.xml'
    static final String POM_WRAPPER_FILENAME = 'pom.groovy'

    @Autowired
    MavenPackager mavenPackager

    @Autowired
    HubSignatureScanner hubSignatureScanner

    BomToolType getBomToolType() {
        return BomToolType.MAVEN
    }

    public List<Prerequisite> getPrerequisites() {
        Prerequisite mvnWrapperExists = new ExecutableExistsPrerequisite(executableManager, ExecutableType.MVNW, detectConfiguration.getMavenPath())
        Prerequisite mvnExists = new ExecutableExistsPrerequisite(executableManager, ExecutableType.MVN, detectConfiguration.getMavenPath())
        Prerequisite mvnOrWrapperExist = new CompositePrerequisite(mvnExists, mvnWrapperExists, Operator.OR)
        mvnOrWrapperExist.setExecutablePrerequisite(true)

        Prerequisite pomXmlExists = new SourceFileExistsPrerequisite(detectFileManager, detectConfiguration, POM_FILENAME)
        Prerequisite pomGroovyExists = new SourceFileExistsPrerequisite(detectFileManager, detectConfiguration, POM_WRAPPER_FILENAME)
        Prerequisite pomXmlSatisfied = new CompositePrerequisite(mvnExists, pomXmlExists, Operator.AND)
        Prerequisite pomWrapperSatisfied = new CompositePrerequisite(mvnWrapperExists, pomGroovyExists, Operator.AND)
        Prerequisite mavenSatisfied = new CompositePrerequisite(pomXmlSatisfied, pomWrapperSatisfied, Operator.OR)

        def prerequisites = []

        prerequisites.add(mavenSatisfied)
        prerequisites.add(mvnOrWrapperExist)

        prerequisites
    }

    List<DetectCodeLocation> extractDetectCodeLocations() {
        List<DetectCodeLocation> codeLocations = []

        def arguments = ["dependency:tree"]
        if (detectConfiguration.getMavenScope()?.trim()) {
            arguments.add("-Dscope=${detectConfiguration.getMavenScope()}")
        }
        String mvnExecutablePath = findMavenExecutablePath()
        final Executable mvnExecutable = new Executable(detectConfiguration.sourceDirectory, mvnExecutablePath, arguments)
        final ExecutableOutput mvnOutput = executableRunner.execute(mvnExecutable)

        List<DependencyNode> sourcePathProjectNodes = mavenPackager.makeDependencyNodes(mvnOutput.standardOutput)
        sourcePathProjectNodes.each {
            DetectCodeLocation detectCodeLocation = new DetectCodeLocation(getBomToolType(), sourcePath, it)
            codeLocations.add(detectCodeLocation)
        }

        //there may also be subprojects, so just look one level down (depth = 2) for any/all target directories
        File[] additionalTargets = detectFileManager.findFilesToDepth(detectConfiguration.sourceDirectory, 'target', 2)
        if (additionalTargets) {
            additionalTargets.each { hubSignatureScanner.registerDirectoryToScan(it) }
        }

        codeLocations
    }

    private String findMavenExecutablePath() {
        if (StringUtils.isNotBlank(detectConfiguration.getMavenPath())) {
            return detectConfiguration.getMavenPath()
        }

        String wrapperPath = executableManager.getPathOfExecutable(ExecutableType.MVNW)
        if (wrapperPath) {
            return wrapperPath
        }

        executableManager.getPathOfExecutable(ExecutableType.MVN)
    }
}