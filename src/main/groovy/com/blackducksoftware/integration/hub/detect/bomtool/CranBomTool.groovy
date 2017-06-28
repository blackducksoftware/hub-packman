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

import java.nio.charset.StandardCharsets

import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.detect.bomtool.cran.PackratPackager
import com.blackducksoftware.integration.hub.detect.type.BomToolType

@Component
class CranBomTool extends BomTool {
	List<String> matchingSourcePaths = []

	BomToolType getBomToolType() {
		return BomToolType.CRAN
	}

	boolean isBomToolApplicable() {
		matchingSourcePaths = sourcePathSearcher.findFilenamePattern('packrat.lock')

		!matchingSourcePaths.isEmpty()
	}

	List<DependencyNode> extractDependencyNodes() {
		List<DependencyNode> projectNodes = []
		matchingSourcePaths.each { sourcePath ->
			File sourceDirectory = new File(sourcePath)
			File packratLockFile = new File(sourceDirectory, 'packrat.lock')

			final InputStream packratLockStream
			try {
				packratLockStream = new FileInputStream(packratLockFile)
				String potentialProjectName = sourceDirectory.getName()
				String packratLock = IOUtils.toString(packratLockStream, StandardCharsets.UTF_8)
				def packratPackager = new PackratPackager(projectInfoGatherer, nameVersionNodeTransformer)
				def projects = packratPackager.makeDependencyNodes(sourcePath, packratLock)
				projectNodes.addAll(projects)
			} finally {
				IOUtils.closeQuietly(packratLockStream)
			}
		}

		projectNodes
	}
}