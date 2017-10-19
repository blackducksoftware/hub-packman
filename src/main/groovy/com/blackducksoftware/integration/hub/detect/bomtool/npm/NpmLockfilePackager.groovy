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
package com.blackducksoftware.integration.hub.detect.bomtool.npm

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.graph.MutableMapDependencyGraph
import com.blackducksoftware.integration.hub.bdio.model.Forge
import com.blackducksoftware.integration.hub.bdio.model.dependency.Dependency
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory
import com.blackducksoftware.integration.hub.detect.bomtool.npm.model.NpmPackage
import com.blackducksoftware.integration.hub.detect.model.BomToolType
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation
import com.google.gson.Gson

import groovy.transform.TypeChecked

@Component
@TypeChecked
class NpmLockfilePackager {

    @Autowired
    Gson gson

    @Autowired
    ExternalIdFactory externalIdFactory

    public DetectCodeLocation parse(String sourcePath, String lockFileText) {
        NpmPackage npmProject = gson.fromJson(lockFileText, NpmPackage.class)
        MutableMapDependencyGraph graph = new MutableMapDependencyGraph()

        Dependency root = generateDependency(npmProject.name, npmProject.version)

        npmProject.dependencies.each { name, npmDependency ->
            if (name != null && npmDependency.version != null) {
                Dependency projectDependency = generateDependency(name, npmDependency.version)
                graph.addChildToRoot(projectDependency)

                npmDependency.requires?.each { childName, childVersion ->
                    Dependency child = generateDependency(childName, childVersion)
                    graph.addChildWithParent(child, projectDependency)
                }
            }
        }

        ExternalId projectId = externalIdFactory.createNameVersionExternalId(Forge.NPM, npmProject.name, npmProject.version)
        DetectCodeLocation codeLocation = new DetectCodeLocation(BomToolType.NPM, sourcePath, npmProject.name, npmProject.version, projectId, graph);
    }

    private Dependency generateDependency(String name, String version) {
        final ExternalId externalId = externalIdFactory.createNameVersionExternalId(Forge.NPM, name, version);
        final Dependency dependency = new Dependency(name, version, externalId);
        return dependency;
    }
}
