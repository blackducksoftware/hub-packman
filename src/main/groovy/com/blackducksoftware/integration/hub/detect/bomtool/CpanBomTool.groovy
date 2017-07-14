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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.bdio.simple.model.Forge
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.ExternalId
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.PathExternalId
import com.blackducksoftware.integration.hub.detect.bomtool.cpan.CpanPackager
import com.blackducksoftware.integration.hub.detect.bomtool.output.DetectCodeLocation
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.CompositePrerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.ExecutableExistsPrerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.Operator
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.Prerequisite
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.SourceFileExistsPrerequisite
import com.blackducksoftware.integration.hub.detect.type.BomToolType
import com.blackducksoftware.integration.hub.detect.type.ExecutableType
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableOutput

@Component
class CpanBomTool extends BomTool {
    @Autowired
    CpanPackager cpanPackager

    @Override
    public BomToolType getBomToolType() {
        BomToolType.CPAN
    }

    public List<Prerequisite> getPrerequisites() {
        Prerequisite cpanExists = new ExecutableExistsPrerequisite(executableManager, ExecutableType.CPAN, detectConfiguration.getCpanPath())
        Prerequisite cpanmExists = new ExecutableExistsPrerequisite(executableManager, ExecutableType.CPANM, detectConfiguration.getCpanmPath())
        Prerequisite bothExist = new CompositePrerequisite(cpanExists, cpanmExists, Operator.AND)
        bothExist.setExecutablePrerequisite(true)

        def prerequisites = []

        prerequisites.add(new SourceFileExistsPrerequisite(detectFileManager, detectConfiguration, 'cpanfile'))
        prerequisites.add(bothExist)

        prerequisites
    }

    @Override
    public List<DetectCodeLocation> extractDetectCodeLocations() {
        String cpanExecutablePath = executableManager.getPathOfExecutable(ExecutableType.CPAN, detectConfiguration.getCpanPath())
        String cpanmExecutablePath = executableManager.getPathOfExecutable(ExecutableType.CPANM, detectConfiguration.getCpanmPath())

        ExecutableOutput cpanListOutput = executableRunner.runExe(cpanExecutablePath, '-l')
        String listText = cpanListOutput.getStandardOutput()

        ExecutableOutput showdepsOutput = executableRunner.runExe(cpanmExecutablePath, '--showdeps', '.')
        String showdeps = showdepsOutput.getStandardOutput()

        Set<DependencyNode> dependenciesSet = cpanPackager.makeDependencyNodes(detectConfiguration.sourceDirectory, listText, showdeps)
        ExternalId externalId = new PathExternalId(Forge.CPAN, detectConfiguration.sourcePath)
        def detectCodeLocation = new DetectCodeLocation(BomToolType.CPAN, detectConfiguration.sourcePath, '', '', '', externalId, dependenciesSet)

        [detectCodeLocation]
    }
}
