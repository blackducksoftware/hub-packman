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
package com.blackducksoftware.integration.hub.detect.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.detect.DetectConfiguration
import com.blackducksoftware.integration.hub.detect.type.BomToolType

@Component
class DetectFileManager {
    private final Logger logger = LoggerFactory.getLogger(DetectFileManager.class)

    @Autowired
    DetectConfiguration detectConfiguration

    @Autowired
    FileFinder fileFinder

    File createDirectory(BomToolType bomToolType) {
        def outputDirectory = new File(detectConfiguration.outputDirectory, bomToolType.toString().toLowerCase())
        outputDirectory.mkdir()
        if (detectConfiguration.cleanupBomToolFiles) {
            outputDirectory.deleteOnExit()
        }

        outputDirectory
    }

    File createFile(BomToolType bomToolType, String fileName) {
        File outputDirectory = createDirectory(bomToolType)
        def newFile = new File(outputDirectory, fileName)
        if (detectConfiguration.cleanupBomToolFiles) {
            newFile.deleteOnExit()
        }

        newFile
    }

    File writeToFile(File file, String contents) {
        writeToFile(file, contents, true)
    }

    File writeToFile(File file, String contents, boolean overwrite) {
        if (!file) {
            return null
        }
        if (overwrite) {
            file.delete()
        }
        if (file.exists()) {
            logger.info("${file.getAbsolutePath()} exists and not being overwritten")
        } else {
            file << contents
        }

        file
    }

    public boolean containsAllFiles(String sourcePath, String... filenamePatterns) {
        return fileFinder.containsAllFiles(sourcePath, filenamePatterns)
    }

    public boolean containsAllFilesToDepth(String sourcePath, int maxDepth, String... filenamePatterns) {
        return fileFinder.containsAllFilesToDepth(sourcePath, maxDepth, filenamePatterns)
    }

    public File findFile(String sourcePath, String filenamePattern) {
        return fileFinder.findFile(sourcePath, filenamePattern)
    }

    public File findFile(File sourceDirectory, String filenamePattern) {
        return fileFinder.findFile(sourceDirectory, filenamePattern)
    }

    public File[] findFiles(File sourceDirectory, String filenamePattern) {
        return fileFinder.findFiles(sourceDirectory, filenamePattern)
    }

    public File[] findFilesToDepth(File sourceDirectory, String filenamePattern, int maxDepth) {
        return fileFinder.findFilesToDepth(sourceDirectory, filenamePattern, maxDepth)
    }

    public File[] findDirectoriesContainingFilesToDepth(File sourceDirectory, String filenamePattern, int maxDepth) {
        return fileFinder.findDirectoriesContainingFilesToDepth(sourceDirectory, filenamePattern, maxDepth)
    }
}
