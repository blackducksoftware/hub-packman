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
import com.blackducksoftware.integration.hub.detect.bomtool.go.godep.GoGodepsParser
import com.blackducksoftware.integration.hub.detect.type.BomToolType
import com.google.gson.Gson

@Component
class GoGodepsBomTool extends BomTool {
    private final Logger logger = LoggerFactory.getLogger(GoGodepsBomTool.class)

    public static final String[] FILE_SEARCH_PATTERN = ['Godeps']

    @Autowired
    Gson gson

    List<String> matchingSourcePaths = []

    @Override
    public BomToolType getBomToolType() {
        return BomToolType.GO_GODEP
    }

    @Override
    public boolean isBomToolApplicable() {
        matchingSourcePaths = sourcePathSearcher.findFilenamePattern(FILE_SEARCH_PATTERN)
        !matchingSourcePaths.isEmpty()
    }

    @Override
    public List<DependencyNode> extractDependencyNodes() {
        def nodes = []
        GoGodepsParser goDepParser = new GoGodepsParser(gson, projectInfoGatherer)
        matchingSourcePaths.each {
            def goDepsDirectory = new File(it, "Godeps")
            def goDepsFile = new File(goDepsDirectory, "Godeps.json")
            if (goDepsFile.exists()) {
                def dependencyNode = goDepParser.parseGoDep(goDepsFile.text)
                nodes.add(dependencyNode)
            }
        }
        return nodes
    }
}
