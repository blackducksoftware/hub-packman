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
package com.blackducksoftware.integration.hub.detect.bomtool.pear

import org.apache.commons.lang3.BooleanUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.bdio.simple.model.Forge
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.NameVersionExternalId
import com.blackducksoftware.integration.hub.detect.DetectConfiguration
import com.blackducksoftware.integration.hub.detect.nameversion.NameVersionNodeImpl
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableOutput

import groovy.transform.TypeChecked

@Component
@TypeChecked
//TODO Separate the different parsing into multiple files.
class PearDependencyFinder {
    private static final String EQUALS_LINE = '==='
    private static final String DEPENDENCY_TYPE_PACKAGE = 'package'

    private final Logger logger = LoggerFactory.getLogger(PearDependencyFinder.class)

    @Autowired
    DetectConfiguration detectConfiguration

    public Set<DependencyNode> parsePearDependencyList(ExecutableOutput pearInstalledDependencies, ExecutableOutput pearPackageXmlDependencies) {
        if (pearPackageXmlDependencies.errorOutput || pearInstalledDependencies.errorOutput) {
            logger.error("There was an error during execution.${pearInstalledDependencies.errorOutput}${pearPackageXmlDependencies.errorOutput}")
            if (!pearPackageXmlDependencies.standardOutput && !pearInstalledDependencies.standardOutput) {
                return (Set<DependencyNode>) []
            }
        } else if (!pearPackageXmlDependencies.standardOutput || !pearInstalledDependencies.standardOutput) {
            logger.error("Not enough information retrieved from running pear commands")
            return (Set<DependencyNode>) []
        }

        Set<String> dependenciesFromPackageXml = findDependencyNames(pearPackageXmlDependencies.standardOutput)
        Set<DependencyNode> childNodes = createPearDependencyNodeFromList(pearInstalledDependencies.standardOutput, dependenciesFromPackageXml)

        childNodes
    }

    private Set<String> findDependencyNames(String packageXmlDependencies) {
        Set<String> dependenciesList = []
        List<String> cleanedDependenciesList = cleanExecutableOutput(packageXmlDependencies)

        cleanedDependenciesList.each {
            PackageXmlDependency packageDependency= createDependencyFromPackageXml(it)
            //TODO swap the comparison
            if (packageDependency.type?.equalsIgnoreCase(DEPENDENCY_TYPE_PACKAGE)) {
                if (detectConfiguration.pearNotRequiredDependencies) {
                    dependenciesList.add(packageDependency.name)
                } else {
                    if (packageDependency.required) {
                        dependenciesList.add(packageDependency.name)
                    }
                }
            }
        }

        dependenciesList
    }

    //TODO change list name to something
    private Set<DependencyNode> createPearDependencyNodeFromList(String list, Set<String> packageXmlDependencyNames) {
        Set<DependencyNode> dependencyNodes = []
        List<String> installedDependencies = cleanExecutableOutput(list)

        installedDependencies.each {
            NameVersionNodeImpl dependencyItem = createDependencyNameVersion(it)
            if (packageXmlDependencyNames.contains(dependencyItem.name)) {
                dependencyNodes.add(new DependencyNode(dependencyItem.name, dependencyItem.version, new NameVersionExternalId(Forge.PEAR, dependencyItem.name, dependencyItem.version)))
            }
        }

        dependencyNodes
    }

    //TODO Create second list by adding the items I need
    private List<String> cleanExecutableOutput(String executableOutput) {
        def lines = executableOutput.split(System.lineSeparator()).toList()
        lines.removeAll('')
        def headerText = []

        lines.eachWithIndex { line, index ->
            if (line.contains(EQUALS_LINE)) {
                headerText.add(lines.get(index - 1))
                headerText.add(lines.get(index))
                //TODO check array size
                headerText.add(lines.get(index + 1))
            }
        }

        lines.removeAll(headerText)
        lines
    }

    private NameVersionNodeImpl createDependencyNameVersion(String installedDependency) {
        def dependencyNameVersion = new NameVersionNodeImpl()

        //TODO lookup whitespace regex
        def dependencyNameVersionParts = installedDependency.split('\\W+').toList()
        dependencyNameVersionParts.removeAll('')
        dependencyNameVersion.name = dependencyNameVersionParts[0]
        dependencyNameVersion.version = dependencyNameVersionParts[1]

        dependencyNameVersion
    }

    private PackageXmlDependency createDependencyFromPackageXml(String packageXmlDependencies) {
        def packageDependency = new PackageXmlDependency()

        def packageDependencyParts = packageXmlDependencies.split(' ').toList()
        packageDependencyParts.removeAll('')
        packageDependency.required = BooleanUtils.toBoolean(packageDependencyParts.get(0))
        packageDependency.setType(packageDependencyParts.get(1))
        String messyName = packageDependencyParts.get(2)
        int slashIndex = messyName.indexOf('/') + 1
        packageDependency.name = messyName.substring(slashIndex)

        packageDependency
    }
}
