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
package com.blackducksoftware.integration.hub.detect.summary

import java.util.Map.Entry

import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.detect.model.BomToolType
import com.blackducksoftware.integration.log.IntLogger

import groovy.transform.TypeChecked

@Component
@TypeChecked
class DetectSummary {
    private Map<BomToolType, Result> bomToolResults = new HashMap<>()
    private Map<File, Result> scanResults = new HashMap<>()
    Result overallResult = null

    public void addApplicableBomToolType(BomToolType bomToolType) {
        bomToolResults.put(bomToolType, null)
    }

    public void setBomToolResult(BomToolType bomToolType, Result result) {
        if (result != null && Result.FAILURE != bomToolResults.get(bomToolType) ) {
            bomToolResults.put(bomToolType, result)
        }
    }

    public void addPathToBeScanned(File scanPath) {
        scanResults.put(scanPath, null)
    }

    public void setPathScanResult(File scanPath, Result result) {
        if (result != null && Result.FAILURE != scanResults.get(scanPath) ) {
            scanResults.put(scanPath, result)
        }
    }

    public void setOverallFailure() {
        overallResult = Result.FAILURE
    }

    public void logResults(IntLogger logger) {
        if (overallResult == null) {
            if (bomToolResults.containsValue(Result.FAILURE) || bomToolResults.containsValue(null) || scanResults.containsValue(Result.FAILURE) || scanResults.containsValue(null)) {
                overallResult = Result.FAILURE
            } else {
                overallResult = Result.SUCCESS
            }
        }

        logger.info("")
        logger.info("======== Detect Results ========")
        for (Entry<BomToolType, Result> entry : bomToolResults.entrySet()) {
            logger.info("${entry.getKey().toString()} : ${entry.getValue().toString()}")
        }
        for (Entry<File, Result> entry : scanResults.entrySet()) {
            logger.info("Scan Target ${entry.getKey().getAbsolutePath()} : ${entry.getValue().toString()}")
        }
        logger.info("")
        logger.info("Overall Status : ${overallResult.toString()}")
        logger.info("================================")
        logger.info("")
    }
}
