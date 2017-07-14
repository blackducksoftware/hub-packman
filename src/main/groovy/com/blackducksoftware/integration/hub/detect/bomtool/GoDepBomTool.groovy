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
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.ExternalId
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.PathExternalId
import com.blackducksoftware.integration.hub.detect.bomtool.go.DepPackager
import com.blackducksoftware.integration.hub.detect.bomtool.output.DetectCodeLocation
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.BooleanPrerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.CompositePrerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.ExecutableExistsPrerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.Operator
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.Prerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.SearchSourceFileExistsPrerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.SourceFileExistsPrerequisite
import com.blackducksoftware.integration.hub.detect.type.BomToolType
import com.blackducksoftware.integration.hub.detect.type.ExecutableType
import com.blackducksoftware.integration.hub.detect.util.executable.Executable

@Component
class GoDepBomTool extends BomTool {
    private final Logger logger = LoggerFactory.getLogger(GoDepBomTool.class)

    public static final Forge GOLANG = new Forge("golang",":")

    public static final String GO_PKG_LOCK_FILENAME = 'Gopkg.lock'
    public static final String GO_FILE_PATTERN = '*.go'

    @Autowired
    GoGodepsBomTool goGodepsBomTool

    @Autowired
    GoVndrBomTool goVndrBomTool

    @Autowired
    DepPackager goPackager

    @Override
    public BomToolType getBomToolType() {
        return BomToolType.GO_DEP
    }

    @Override
    public List<Prerequisite> getPrerequisites() {
        Prerequisite goExecutableExists = new ExecutableExistsPrerequisite(executableManager, ExecutableType.GO, null)

        Prerequisite goPkgLockExists = new SourceFileExistsPrerequisite(detectFileManager, detectConfiguration, GO_PKG_LOCK_FILENAME)
        Prerequisite godepsNotApplicable = new BooleanPrerequisite(!goGodepsBomTool.isBomToolApplicable(), 'Godeps is applicable')
        Prerequisite goVndrNotApplicable = new BooleanPrerequisite(!goVndrBomTool.isBomToolApplicable(), 'Vndr is applicable')
        Prerequisite foundGoFiles = new SearchSourceFileExistsPrerequisite(detectFileManager, detectConfiguration, GO_FILE_PATTERN)

        Prerequisite otherBomToolsNotBetter = new CompositePrerequisite(godepsNotApplicable, goVndrNotApplicable, Operator.OR)
        Prerequisite isGoProjectAndOtherBomToolsNotApplicable = new CompositePrerequisite(otherBomToolsNotBetter, foundGoFiles, Operator.AND)

        Prerequisite isTheBestBomTool = new CompositePrerequisite(goPkgLockExists, isGoProjectAndOtherBomToolsNotApplicable, Operator.OR)

        def prerequisites = []

        prerequisites.add(isTheBestBomTool)
        prerequisites.add(goExecutableExists)

        prerequisites
    }

    List<DetectCodeLocation> extractDetectCodeLocations() {
        String goDepExecutable = findGoDepExecutable()

        List<DependencyNode> dependencies = goPackager.makeDependencyNodes(sourcePath, goDepExecutable)
        ExternalId externalId = new PathExternalId(GOLANG, sourcePath)
        DetectCodeLocation detectCodeLocation = new DetectCodeLocation(getBomToolType(), sourcePath, '', '', '', externalId, new HashSet(dependencies))

        [detectCodeLocation]
    }

    private String findGoDepExecutable() {
        String goDepPath = detectConfiguration.goDepPath
        if (StringUtils.isBlank(goDepPath)) {
            def goDep = getBuiltGoDep()
            if (goDep.exists()) {
                goDepPath = goDep.getAbsolutePath()
            } else {
                goDepPath = executableManager.getPathOfExecutable(ExecutableType.GO_DEP)
            }
        }
        if (!goDepPath?.trim()) {
            def goExecutable = executableManager.getPathOfExecutable(ExecutableType.GO)
            goDepPath = installGoDep(goExecutable)
        }
        goDepPath
    }

    private String installGoDep(String goExecutable) {
        File goDep = getBuiltGoDep()
        def goOutputDirectory = goDep.getParentFile()
        goOutputDirectory.mkdirs()
        logger.debug("Retrieving the Go Dep tool")
        Executable getGoDep = new Executable(goOutputDirectory, goExecutable, [
            'get',
            '-u',
            '-v',
            '-d',
            'github.com/golang/dep/cmd/dep'
        ])
        executableRunner.execute(getGoDep)

        logger.debug("Building the Go Dep tool in ${goOutputDirectory}")
        Executable buildGoDep = new Executable(goOutputDirectory, goExecutable, [
            'build',
            'github.com/golang/dep/cmd/dep'
        ])
        executableRunner.execute(buildGoDep)
        goDep.getAbsolutePath()
    }

    private File getBuiltGoDep() {
        def goOutputDirectory = new File(detectConfiguration.outputDirectory, 'Go')
        new File(goOutputDirectory, executableManager.getExecutableName(ExecutableType.GO_DEP))
    }
}