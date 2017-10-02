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
package com.blackducksoftware.integration.hub.detect.bomtool.cran

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.detect.bomtool.CranBomTool
import com.blackducksoftware.integration.hub.detect.nameversion.NameVersionNode
import com.blackducksoftware.integration.hub.detect.nameversion.NameVersionNodeImpl
import com.blackducksoftware.integration.hub.detect.nameversion.NameVersionNodeTransformer
import com.blackducksoftware.integration.hub.detect.nameversion.builder.NameVersionNodeBuilder

import groovy.transform.TypeChecked

@TypeChecked
public class PackRatNodeParser {
    private final Logger logger = LoggerFactory.getLogger(PackRatNodeParser.class)

    private NameVersionNode rootNameVersionNode
    private NameVersionNodeBuilder nameVersionNodeBuilder
    private HashSet<String> directDependencyNames
    private NameVersionNode currentParent

    List<DependencyNode> parseProjectDependencies(NameVersionNodeTransformer nameVersionNodeTransformer, final String packratLockContents) {
        rootNameVersionNode = new NameVersionNodeImpl([name: 'packratLockFileRoot'])
        nameVersionNodeBuilder = new NameVersionNodeBuilder(rootNameVersionNode)
        directDependencyNames = new HashSet<>()
        currentParent = null

        String[] lines = packratLockContents.split("\\u000D\\u000A|[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029]")
        String name
        String version
        List<DependencyNode> projectDependencies = []
        boolean newDependency = false

        for (String line : lines) {
            if (line.contains('Package: ')) {
                name = line.replace('Package: ', '').trim()
                directDependencyNames.add(name)
                newDependency = true
                continue
            }

            if (line.contains('Version: ')) {
                version = line.replace('Version: ', '').trim()
                NameVersionNode node = this.createNameVersionNodeImpl(name, version)
                nameVersionNodeBuilder.addChildNodeToParent(node, rootNameVersionNode)
            }

            currentParent = this.createNameVersionNodeImpl(name, version)

            if (line.contains('Requires: ')) {
                String[] parts = line.replace('Requires: ','').split(',')
                for (int i; i < parts.size(); i++) {
                    NameVersionNode node = this.createNameVersionNodeImpl(parts[i].trim(), '')
                    nameVersionNodeBuilder.addChildNodeToParent(node, currentParent)
                }
            }
        }

        directDependencyNames.each { directDependencyName ->
            NameVersionNode nameVersionNode = nameVersionNodeBuilder.nodeCache[directDependencyName]
            if (nameVersionNode) {
                DependencyNode directDependencyNode = nameVersionNodeTransformer.createDependencyNode(CranBomTool.CRAN, nameVersionNode)
                projectDependencies.add(directDependencyNode)
            } else {
                logger.error("Could not find ${directDependencyName} in the populated map.")
            }
        }

        projectDependencies
    }

    private NameVersionNode createNameVersionNodeImpl(String name, String version) {
        return new NameVersionNodeImpl([name: name, version: version])
    }
}
