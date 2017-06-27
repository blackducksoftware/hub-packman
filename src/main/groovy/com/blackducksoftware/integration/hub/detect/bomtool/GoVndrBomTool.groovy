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
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.ExternalId
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.NameVersionExternalId
import com.blackducksoftware.integration.hub.detect.bomtool.go.vndr.VndrParser
import com.blackducksoftware.integration.hub.detect.type.BomToolType

@Component
class GoVndrBomTool extends BomTool {
    private final Logger logger = LoggerFactory.getLogger(GoVndrBomTool.class)

    public static final String[] FILE_SEARCH_PATTERN = ['vendor.conf']

    List<String> matchingSourcePaths = []

    @Override
    public BomToolType getBomToolType() {
        return BomToolType.GO_VNDR
    }

    @Override
    public boolean isBomToolApplicable() {
        matchingSourcePaths = sourcePathSearcher.findFilenamePattern(FILE_SEARCH_PATTERN)
        !matchingSourcePaths.isEmpty()
    }

    @Override
    public List<DependencyNode> extractDependencyNodes() {
        def nodes = []
        VndrParser vndrParser = new VndrParser(projectInfoGatherer)
        matchingSourcePaths.each {
            def vendorConf = new File(it, "vendor.conf")
            if (vendorConf.exists()) {
                final String rootName = projectInfoGatherer.getDefaultProjectName(BomToolType.GO_VNDR, it)
                final String rootVersion = projectInfoGatherer.getDefaultProjectVersionName()
                final ExternalId rootExternalId = new NameVersionExternalId(GoDepBomTool.GOLANG, rootName, rootVersion)
                final DependencyNode projectNode = new DependencyNode(rootName, rootVersion, rootExternalId)
                def children = vndrParser.parseVendorConf(vendorConf.text)
                projectNode.children.addAll(children)
                nodes.add(projectNode)
            }
        }
        return nodes
    }
}
