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

import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import com.blackducksoftware.integration.hub.detect.DetectConfiguration
import com.blackducksoftware.integration.hub.detect.bomtool.output.DetectCodeLocation
import com.blackducksoftware.integration.hub.detect.bomtool.prerequisite.Prerequisite
import com.blackducksoftware.integration.hub.detect.nameversion.NameVersionNodeTransformer
import com.blackducksoftware.integration.hub.detect.type.BomToolType
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableManager
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner

abstract class BomTool {
    private final Logger logger = LoggerFactory.getLogger(getClass())

    @Autowired
    DetectConfiguration detectConfiguration

    @Autowired
    ExecutableManager executableManager

    @Autowired
    ExecutableRunner executableRunner

    @Autowired
    DetectFileManager detectFileManager

    @Autowired
    NameVersionNodeTransformer nameVersionNodeTransformer

    abstract BomToolType getBomToolType()
    abstract List<Prerequisite> getPrerequisites()

    /**
     * A BomTool is responsible for doing its best to create at least one, but possibly many, DetectCodeLocations.
     */
    abstract List<DetectCodeLocation> extractDetectCodeLocations()

    boolean isBomToolApplicable() {
        List<Prerequisite> prerequisites = getPrerequisites()
        int totalPrerequisites = prerequisites.size()
        int metPrerequisites = 0
        int metExecutablePrerequisites = 0
        List<String> failureMessages = []
        prerequisites.each {
            if (it.isMet()) {
                metPrerequisites++
                if (it.isExecutablePrerequisite()) {
                    metExecutablePrerequisites++
                }
            } else {
                failureMessages.add(it.failureMessage())
            }
        }
        if (metPrerequisites > metExecutablePrerequisites) {
            if (metPrerequisites == totalPrerequisites) {
                return true
            }
            logger.error("Some prerequisites for ${bomToolType.toString()} were not met: ${failureMessages.join(', ')}")
        }
        return false
    }

    String getSourcePath() {
        detectConfiguration.sourcePath
    }

    File getSourceDirectory() {
        detectConfiguration.sourceDirectory
    }

    String getHash(String text) {
        if(detectConfiguration.getHashVersion()) {
            def version = DigestUtils.sha1Hex(text.getBytes())
            if(detectConfiguration.getShortHash()) {
                version = version.substring(0, 7)
            }
            return version
        }

        ''
    }
}
