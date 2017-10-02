/* Copyright (C) 2017 Black Duck Software, Inc.
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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.detect.nameversion.NameVersionNodeTransformer

import groovy.transform.TypeChecked

@Component
@TypeChecked
public class PackratPackager {
    @Autowired
    private final NameVersionNodeTransformer nameVersionNodeTransformer

    public List<DependencyNode> extractProjectDependencies(final String packratLock) {
        def packRatNodeParser = new PackRatNodeParser()
        List<DependencyNode> dependencies = packRatNodeParser.parseProjectDependencies(nameVersionNodeTransformer, packratLock)

        dependencies
    }

    public String getProjectName(final String descriptionContents) {
        String[] lines = descriptionContents.split("\\u000D\\u000A|[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029]")
        String name

        for (String line : lines) {
            if (line.contains('Package: ')) {
                name = line.replace('Package: ', '').trim()
                break
            }
        }

        name
    }

    public String getVersion(String descriptionContents) {
        String[] lines = descriptionContents.split("\\u000D\\u000A|[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029]")
        String versionLine = lines.find { it.contains('Version: ') }

        if (versionLine != null) {
            return versionLine.replace('Version: ', '').trim()
        }

        null
    }
}
