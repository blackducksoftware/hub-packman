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

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.bdio.simple.model.Forge
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.ExternalId
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.PathExternalId
import com.blackducksoftware.integration.hub.detect.bomtool.cpan.CpanPackager
import com.blackducksoftware.integration.hub.detect.model.BomToolType
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation
import com.blackducksoftware.integration.hub.detect.type.ExecutableType
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableOutput

import groovy.transform.TypeChecked

@Component
@TypeChecked
class CpanBomTool extends BomTool {
    private final Logger logger = LoggerFactory.getLogger(CpanBomTool.class)

    public static Forge CPAN_FORGE = new Forge('cpan', '-')

    @Autowired
    CpanPackager cpanPackager

    private String cpanExecutablePath
    private String cpanmExecutablePath

    @Override
    public BomToolType getBomToolType() {
        BomToolType.CPAN
    }

    @Override
    public boolean isBomToolApplicable() {
        def containsFiles = detectFileManager.containsAllFiles(sourcePath, 'Makefile.PL')
        if (containsFiles) {
            cpanExecutablePath = findExecutablePath(ExecutableType.CPAN, true, detectConfiguration.getCpanPath())
            cpanmExecutablePath = findExecutablePath(ExecutableType.CPANM, true, detectConfiguration.getCpanmPath())
            if (!cpanExecutablePath) {
                logger.warn("Could not find the ${executableManager.getExecutableNames(ExecutableType.CPAN).join(' or ')} executable")
            }
            if (!cpanmExecutablePath) {
                logger.warn("Could not find the ${executableManager.getExecutableNames(ExecutableType.CPANM).join(' or ')} executable")
            }
        }

        containsFiles && cpanExecutablePath && cpanmExecutablePath
    }

    @Override
    public List<DetectCodeLocation> extractDetectCodeLocations() {
        ExecutableOutput cpanListOutput = executableRunner.runExe(cpanExecutablePath, '-l')
        String listText = cpanListOutput.getStandardOutput()

        ExecutableOutput showdepsOutput = executableRunner.runExe(cpanmExecutablePath, '--showdeps', '.')
        String showdeps = showdepsOutput.getStandardOutput()

        Set<DependencyNode> dependenciesSet = cpanPackager.makeDependencyNodes(listText, showdeps)
        ExternalId externalId = new PathExternalId(CPAN_FORGE, detectConfiguration.sourcePath)
        def detectCodeLocation = new DetectCodeLocation(BomToolType.CPAN, detectConfiguration.sourcePath, externalId, dependenciesSet)

        [detectCodeLocation]
    }
}
