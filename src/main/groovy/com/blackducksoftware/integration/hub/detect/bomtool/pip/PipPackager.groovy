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
package com.blackducksoftware.integration.hub.detect.bomtool.pip

import java.nio.charset.StandardCharsets

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.bdio.simple.model.Forge
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.NameVersionExternalId
import com.blackducksoftware.integration.hub.detect.DetectConfiguration
import com.blackducksoftware.integration.hub.detect.nameversion.NameVersionNodeTransformer
import com.blackducksoftware.integration.hub.detect.type.BomToolType
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager
import com.blackducksoftware.integration.hub.detect.util.ProjectInfoGatherer
import com.blackducksoftware.integration.hub.detect.util.executable.Executable
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunnerException

@Component
class PipPackager {
    final Logger logger = LoggerFactory.getLogger(this.getClass())
    private final String INSPECTOR_NAME = 'pip-inspector.py'

    @Autowired
    ExecutableRunner executableRunner

    @Autowired
    DetectConfiguration detectConfiguration

    @Autowired
    ProjectInfoGatherer projectInfoGatherer

    @Autowired
    NameVersionNodeTransformer nameVersionNodeTransformer

    @Autowired
    DetectFileManager detectFileManager

    List<DependencyNode> makeDependencyNodes(File sourceDirectory, VirtualEnvironment virtualEnv) throws ExecutableRunnerException {
        String pipPath = virtualEnv.pipPath
        String pythonPath = virtualEnv.pythonPath
        def setupFile = detectFileManager.findFile(sourceDirectory, 'setup.py')

        String inpsectorScriptContents = getClass().getResourceAsStream("/${INSPECTOR_NAME}").getText(StandardCharsets.UTF_8.name())
        def inspectorScript = detectFileManager.createFile(BomToolType.PIP, INSPECTOR_NAME)
        detectFileManager.writeToFile(inspectorScript, inpsectorScriptContents)
        def pipInspectorOptions = [
            inspectorScript.absolutePath
        ]

        // Install pytest-runner to avoid a zip_flag error if the project uses pytest-runner
        def installPytestRunner = new Executable(sourceDirectory, pipPath, ['install', 'pytest-runner'])
        executableRunner.execute(installPytestRunner)

        // Install requirements file and add it as an option for the inspector
        if (detectConfiguration.requirementsFilePath) {
            def requirementsFile = new File(detectConfiguration.requirementsFilePath)
            pipInspectorOptions += "--requirements=${requirementsFile.absolutePath}"

            def installRequirements = new Executable(sourceDirectory, pipPath, [
                'install',
                '-r',
                requirementsFile.absolutePath
            ])
            executableRunner.execute(installRequirements)
        }

        // Install project if it can find one and pass its name to the inspector
        if (setupFile) {
            def installProjectExecutable = new Executable(sourceDirectory, pipPath, ['install', '.', '-I'])
            executableRunner.execute(installProjectExecutable)
            def projectName = detectConfiguration.pipProjectName
            if (!projectName) {
                def findProjectNameExecutable = new Executable(sourceDirectory, pythonPath, [
                    setupFile.absolutePath,
                    '--name'
                ])
                projectName = executableRunner.execute(findProjectNameExecutable).standardOutput.trim()
            }
            pipInspectorOptions += "--projectname=${projectName}"
        }

        def pipInspector = new Executable(sourceDirectory, pythonPath, pipInspectorOptions)
        def inspectorOutput = executableRunner.execute(pipInspector).standardOutput
        def parser = new PipInspectorTreeParser()
        DependencyNode project = parser.parse(nameVersionNodeTransformer, inspectorOutput)

        if (project.name == PipInspectorTreeParser.UNKOWN_PROJECT_NAME && project.version == PipInspectorTreeParser.UNKOWN_PROJECT_VERSION) {
            project.name = projectInfoGatherer.getDefaultProjectName(BomToolType.PIP, sourceDirectory.getAbsolutePath())
            project.version = projectInfoGatherer.getDefaultProjectVersionName()
            project.externalId = new NameVersionExternalId(Forge.PYPI, project.name, project.version)
        }

        [project]
    }
}
