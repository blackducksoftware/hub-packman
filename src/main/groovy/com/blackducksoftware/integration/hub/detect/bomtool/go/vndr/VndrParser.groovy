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
package com.blackducksoftware.integration.hub.detect.bomtool.go.vndr

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.ExternalId
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.NameVersionExternalId
import com.blackducksoftware.integration.hub.detect.bomtool.GoDepBomTool

import groovy.transform.TypeChecked

@TypeChecked
class VndrParser {
    public List<DependencyNode> parseVendorConf(String vendorConfContents) {
        List<DependencyNode> nodes = new ArrayList<>()
        String contents = vendorConfContents.trim()
        def lines = contents.split("\\u000D\\u000A|[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029]")
        //TODO test against moby
        lines.each { String line ->
            if (line?.trim() && !line.startsWith('#')) {
                def parts = line.split(' ')
                final ExternalId dependencyExternalId = new NameVersionExternalId(GoDepBomTool.GOLANG, parts[0], parts[1])
                final DependencyNode dependency = new DependencyNode(parts[0], parts[1], dependencyExternalId)
                nodes.add(dependency)
            }
        }

        return nodes
    }
}
