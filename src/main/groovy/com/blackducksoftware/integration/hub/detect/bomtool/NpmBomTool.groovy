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

import com.blackducksoftware.integration.hub.detect.DetectConfiguration
import com.blackducksoftware.integration.hub.detect.bomtool.npm.NpmDependencyFinder
import com.blackducksoftware.integration.hub.detect.bomtool.npm.NpmLockfilePackager
import com.blackducksoftware.integration.hub.detect.hub.HubSignatureScanner
import com.blackducksoftware.integration.hub.detect.model.BomToolType
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation

import groovy.transform.TypeChecked

@Component
@TypeChecked
class NpmBomTool extends BomTool {
    private final Logger logger = LoggerFactory.getLogger(NpmBomTool.class)

    public static final String NODE_MODULES = 'node_modules'
    public static final String PACKAGE_JSON = 'package.json'
    public static final String PACKAGE_LOCK_JSON = 'package-lock.json'
    public static final String SHRINKWRAP_JSON = 'npm-shrinkwrap.json'
    public static final String OUTPUT_FILE = 'detect_npm_proj_dependencies.json'
    public static final String ERROR_FILE = 'detect_npm_error.json'

    @Autowired
    NpmLockfilePackager npmLockfilePackager

    @Autowired
    YarnBomTool yarnBomTool

    @Autowired
    HubSignatureScanner hubSignatureScanner

    @Autowired
    DetectConfiguration detectConfiguration

    @Autowired
    NpmDependencyFinder npmDependencyFinder

    private File packageLockJson
    private File shrinkwrapJson

    @Override
    public BomToolType getBomToolType() {
        BomToolType.NPM
    }

    @Override
    public boolean isBomToolApplicable() {
        if (yarnBomTool.isBomToolApplicable()) {
            logger.debug("Not running npm bomtool because Yarn is applicable")
            return false
        }

        packageLockJson = detectFileManager.findFile(sourcePath, PACKAGE_LOCK_JSON)
        shrinkwrapJson = detectFileManager.findFile(sourcePath, SHRINKWRAP_JSON)

        boolean containsNodeModules = detectFileManager.containsAllFiles(sourcePath, NODE_MODULES)
        boolean containsPackageJson = detectFileManager.containsAllFiles(sourcePath, PACKAGE_JSON)
        boolean containsPackageLockJson = packageLockJson
        boolean containsShrinkwrapJson = shrinkwrapJson

        if (containsPackageLockJson) {
            logger.info("Using ${PACKAGE_LOCK_JSON}")
        } else if (shrinkwrapJson) {
            logger.info("Using ${SHRINKWRAP_JSON}")
        } else if (containsPackageJson && !containsNodeModules) {
            logger.warn("package.json was located in ${sourcePath}, but the node_modules folder was NOT located. Please run 'npm install' in that location and try again.")
        } else if (containsPackageJson && containsNodeModules) {
            logger.info("Using node_modules traversal method")
        }

        boolean lockFileIsApplicable = containsShrinkwrapJson || containsPackageLockJson
        boolean isApplicable =  lockFileIsApplicable || (containsNodeModules && containsPackageJson)

        isApplicable
    }

    List<DetectCodeLocation> extractDetectCodeLocations() {
        List<DetectCodeLocation> codeLocations= []
        if (packageLockJson) {
            codeLocations.addAll(extractFromLockFile(packageLockJson))
        } else if (shrinkwrapJson) {
            codeLocations.addAll(extractFromLockFile(shrinkwrapJson))
        } else {
            codeLocations.addAll(extractFromTraversal())
        }

        if (!codeLocations.empty) {
            hubSignatureScanner.registerPathToScan(sourceDirectory, NODE_MODULES)
        }

        codeLocations
    }

    private List<DetectCodeLocation> extractFromLockFile(File lockFile) {
        String lockFileText = lockFile.getText()
        DetectCodeLocation detectCodeLocation = npmLockfilePackager.parse(sourcePath, lockFileText)

        [detectCodeLocation]
    }

    private List<DetectCodeLocation> extractFromTraversal() {
        def detectCodeLocation = npmDependencyFinder.createDependencyGraph(sourcePath)
        [detectCodeLocation]
    }
}
