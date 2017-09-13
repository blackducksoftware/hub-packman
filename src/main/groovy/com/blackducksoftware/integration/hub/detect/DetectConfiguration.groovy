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
package com.blackducksoftware.integration.hub.detect

import java.lang.reflect.Modifier
import java.nio.charset.StandardCharsets

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.EnumerablePropertySource
import org.springframework.core.env.MutablePropertySources
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.detect.bomtool.BomTool
import com.blackducksoftware.integration.hub.detect.bomtool.DockerBomTool
import com.blackducksoftware.integration.hub.detect.exception.DetectException
import com.blackducksoftware.integration.hub.detect.model.BomToolType
import com.blackducksoftware.integration.util.ResourceUtil
import com.google.gson.Gson

import groovy.transform.TypeChecked

@Component
@TypeChecked
class DetectConfiguration {
    private final Logger logger = LoggerFactory.getLogger(DetectProperties.class)

    static final String DETECT_PROPERTY_PREFIX = 'detect.'
    static final String DOCKER_PROPERTY_PREFIX = 'detect.docker.passthrough.'

    @Autowired
    ConfigurableEnvironment configurableEnvironment

    @Autowired
    DetectProperties detectProperties

    @Autowired
    DockerBomTool dockerBomTool

    @Autowired
    Gson gson

    BuildInfo buildInfo

    File sourceDirectory
    File outputDirectory
    Set<String> allDetectPropertyKeys = new HashSet<>()
    Set<String> additionalDockerPropertyNames = new HashSet<>()

    private boolean usingDefaultSourcePath
    private boolean usingDefaultOutputPath

    List<String> excludedScanPaths = []

    void init() {
        buildInfo = gson.fromJson(ResourceUtil.getResourceAsString('buildInfo.json', StandardCharsets.UTF_8.toString()), BuildInfo.class)

        if (!detectProperties.sourcePath) {
            usingDefaultSourcePath = true
            detectProperties.sourcePath = System.getProperty('user.dir')
        }

        sourceDirectory = new File(detectProperties.sourcePath)
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            throw new DetectException("The source path ${detectProperties.sourcePath} either doesn't exist, isn't a directory, or doesn't have appropriate permissions.")
        }
        //make sure the path is absolute
        detectProperties.sourcePath = sourceDirectory.canonicalPath

        if (StringUtils.isBlank(detectProperties.outputPath)) {
            usingDefaultOutputPath = true
            detectProperties.outputPath = System.getProperty('user.home') + File.separator + 'blackduck'
        }

        detectProperties.nugetInspectorName = detectProperties.nugetInspectorName.trim()
        detectProperties.nugetInspectorVersion = detectProperties.nugetInspectorVersion.trim()

        outputDirectory = new File(detectProperties.outputPath)
        outputDirectory.mkdirs()
        if (!outputDirectory.exists() || !outputDirectory.isDirectory()) {
            throw new DetectException("The output directory ${detectProperties.outputPath} does not exist. The system property 'user.home' will be used by default, but the output directory must exist.")
        }
        detectProperties.outputPath = detectProperties.outputPath.trim()

        MutablePropertySources mutablePropertySources = configurableEnvironment.getPropertySources()
        mutablePropertySources.each { propertySource ->
            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource
                enumerablePropertySource.propertyNames.each { String propertyName ->
                    if (propertyName && propertyName.startsWith(DETECT_PROPERTY_PREFIX)) {
                        allDetectPropertyKeys.add(propertyName)
                    }
                }
            }
        }

        if (dockerBomTool.isBomToolApplicable()) {
            configureForDocker()
        }

        if (detectProperties.hubSignatureScannerRelativePathsToExclude) {
            detectProperties.hubSignatureScannerRelativePathsToExclude.each { String path ->
                excludedScanPaths.add(new File(sourceDirectory, path).getCanonicalPath())
            }
        }
    }

    /**
     * If the default source path is being used AND docker is configured, don't run unless the tool is docker
     */
    public boolean shouldRun(BomTool bomTool) {
        if (usingDefaultSourcePath && dockerBomTool.isBomToolApplicable()) {
            return BomToolType.DOCKER == bomTool.bomToolType
        } else {
            return true
        }
    }

    public String getDetectProperty(String key) {
        configurableEnvironment.getProperty(key)
    }

    private void configureForDocker() {
        allDetectPropertyKeys.each {
            if (it.startsWith(DOCKER_PROPERTY_PREFIX)) {
                additionalDockerPropertyNames.add(it)
            }
        }
    }

    public void logConfiguration() {
        List<String> configurationPieces = []
        configurationPieces.add('')
        configurationPieces.add("Detect Version: ${buildInfo.getDetectVersion()}" as String)
        configurationPieces.add('Current property values:')
        configurationPieces.add('-'.multiply(60))
        def propertyFields = DetectProperties.class.getDeclaredFields().findAll {
            int modifiers = it.modifiers
            !Modifier.isStatic(modifiers) && Modifier.isPrivate(modifiers)
        }.sort { a, b ->
            a.name <=> b.name
        }

        propertyFields.each {
            it.accessible = true
            String fieldName = it.name
            Object fieldValue = it.get(detectProperties)
            if (it.type.isArray()) {
                fieldValue = (fieldValue as String[]).join(', ')
            }
            if (fieldName && fieldValue && 'metaClass' != fieldName) {
                if (fieldName.toLowerCase().contains('password')) {
                    fieldValue = '*'.multiply((fieldValue as String).length())
                }
                configurationPieces.add("${fieldName} = ${fieldValue}" as String)
            }
            it.accessible = false
        }
        configurationPieces.add('-'.multiply(60))
        configurationPieces.add('')
        String configurationMessage = configurationPieces.join(System.lineSeparator())
        logger.info(configurationMessage)
    }

    public List<String> getHubSignatureScannerPathsToExclude() {
        return excludedScanPaths
    }

    private int convertInt(Integer integerObj) {
        return integerObj == null ? 0 : integerObj.intValue()
    }

    private long convertLong(Long longObj) {
        return longObj == null ? 0L : longObj.longValue()
    }

    /**
     * Don't delete this marker - it is so the code between the markers can be auto-generated.
     * If changes to this code are needed, please make them in detectConfiguration.ftl and generate the code again.
     */
    //AUTO-GENERATE PROPERTIES START MARKER
    public boolean getSuppressConfigurationOutput() {
        return BooleanUtils.toBoolean(detectProperties.suppressConfigurationOutput)
    }
    public boolean getCleanupBdioFiles() {
        return BooleanUtils.toBoolean(detectProperties.cleanupBdioFiles)
    }
    public boolean getTestConnection() {
        return BooleanUtils.toBoolean(detectProperties.testConnection)
    }
    public String getHubUrl() {
        return detectProperties.hubUrl?.trim()
    }
    public int getHubTimeout() {
        return convertInt(detectProperties.hubTimeout)
    }
    public String getHubUsername() {
        return detectProperties.hubUsername?.trim()
    }
    public String getHubPassword() {
        return detectProperties.hubPassword?.trim()
    }
    public String getHubProxyHost() {
        return detectProperties.hubProxyHost?.trim()
    }
    public String getHubProxyPort() {
        return detectProperties.hubProxyPort?.trim()
    }
    public String getHubProxyUsername() {
        return detectProperties.hubProxyUsername?.trim()
    }
    public String getHubProxyPassword() {
        return detectProperties.hubProxyPassword?.trim()
    }
    public boolean getHubAutoImportCert() {
        return BooleanUtils.toBoolean(detectProperties.hubAutoImportCert)
    }
    public boolean getHubOfflineMode() {
        return BooleanUtils.toBoolean(detectProperties.hubOfflineMode)
    }
    public String getSourcePath() {
        return detectProperties.sourcePath?.trim()
    }
    public String getOutputPath() {
        return detectProperties.outputPath?.trim()
    }
    public int getSearchDepth() {
        return convertInt(detectProperties.searchDepth)
    }
    public String getExcludedBomToolTypes() {
        return detectProperties.excludedBomToolTypes?.trim()
    }
    public String getIncludedBomToolTypes() {
        return detectProperties.includedBomToolTypes?.trim()
    }
    public String getProjectName() {
        return detectProperties.projectName?.trim()
    }
    public String getProjectVersionName() {
        return detectProperties.projectVersionName?.trim()
    }
    public String getProjectCodelocationPrefix() {
        return detectProperties.projectCodelocationPrefix?.trim()
    }
    public boolean getProjectLevelAdjustments() {
        return BooleanUtils.toBoolean(detectProperties.projectLevelAdjustments)
    }
    public String getProjectVersionPhase() {
        return detectProperties.projectVersionPhase?.trim()
    }
    public String getProjectVersionDistribution() {
        return detectProperties.projectVersionDistribution?.trim()
    }
    public boolean getPolicyCheck() {
        return BooleanUtils.toBoolean(detectProperties.policyCheck)
    }
    public long getPolicyCheckTimeout() {
        return convertLong(detectProperties.policyCheckTimeout)
    }
    public String getGradleInspectorVersion() {
        return detectProperties.gradleInspectorVersion?.trim()
    }
    public String getGradleBuildCommand() {
        return detectProperties.gradleBuildCommand?.trim()
    }
    public String getGradleExcludedConfigurations() {
        return detectProperties.gradleExcludedConfigurations?.trim()
    }
    public String getGradleIncludedConfigurations() {
        return detectProperties.gradleIncludedConfigurations?.trim()
    }
    public String getGradleExcludedProjects() {
        return detectProperties.gradleExcludedProjects?.trim()
    }
    public String getGradleIncludedProjects() {
        return detectProperties.gradleIncludedProjects?.trim()
    }
    public boolean getGradleCleanupBuildBlackduckDirectory() {
        return BooleanUtils.toBoolean(detectProperties.gradleCleanupBuildBlackduckDirectory)
    }
    public String getNugetInspectorName() {
        return detectProperties.nugetInspectorName?.trim()
    }
    public String getNugetInspectorVersion() {
        return detectProperties.nugetInspectorVersion?.trim()
    }
    public String getNugetExcludedModules() {
        return detectProperties.nugetExcludedModules?.trim()
    }
    public boolean getNugetIgnoreFailure() {
        return BooleanUtils.toBoolean(detectProperties.nugetIgnoreFailure)
    }
    public String getMavenScope() {
        return detectProperties.mavenScope?.trim()
    }
    public String getGradlePath() {
        return detectProperties.gradlePath?.trim()
    }
    public String getMavenPath() {
        return detectProperties.mavenPath?.trim()
    }
    public String getNugetPath() {
        return detectProperties.nugetPath?.trim()
    }
    public String getPipProjectName() {
        return detectProperties.pipProjectName?.trim()
    }
    public boolean getPipPip3() {
        return BooleanUtils.toBoolean(detectProperties.pipPip3)
    }
    public String getPythonPath() {
        return detectProperties.pythonPath?.trim()
    }
    public String getPipPath() {
        return detectProperties.pipPath?.trim()
    }
    public String getNpmPath() {
        return detectProperties.npmPath?.trim()
    }
    public String getPearPath() {
        return detectProperties.pearPath?.trim()
    }
    public boolean getPearNotRequiredDependencies() {
        return BooleanUtils.toBoolean(detectProperties.pearNotRequiredDependencies)
    }
    public String getPipVirtualEnvPath() {
        return detectProperties.pipVirtualEnvPath?.trim()
    }
    public String getPipRequirementsPath() {
        return detectProperties.pipRequirementsPath?.trim()
    }
    public String getGoDepPath() {
        return detectProperties.goDepPath?.trim()
    }
    public String getDockerPath() {
        return detectProperties.dockerPath?.trim()
    }
    public String getDockerInspectorPath() {
        return detectProperties.dockerInspectorPath?.trim()
    }
    public String getDockerInspectorVersion() {
        return detectProperties.dockerInspectorVersion?.trim()
    }
    public String getDockerTar() {
        return detectProperties.dockerTar?.trim()
    }
    public String getDockerImage() {
        return detectProperties.dockerImage?.trim()
    }
    public String getBashPath() {
        return detectProperties.bashPath?.trim()
    }
    public String getLoggingLevel() {
        return detectProperties.loggingLevel?.trim()
    }
    public boolean getCleanupBomToolFiles() {
        return BooleanUtils.toBoolean(detectProperties.cleanupBomToolFiles)
    }
    public boolean getHubSignatureScannerDryRun() {
        return BooleanUtils.toBoolean(detectProperties.hubSignatureScannerDryRun)
    }
    public String[] getHubSignatureScannerExclusionPatterns() {
        return detectProperties.hubSignatureScannerExclusionPatterns
    }
    public String[] getHubSignatureScannerPaths() {
        return detectProperties.hubSignatureScannerPaths
    }
    public String[] getHubSignatureScannerRelativePathsToExclude() {
        return detectProperties.hubSignatureScannerRelativePathsToExclude
    }
    public int getHubSignatureScannerMemory() {
        return convertInt(detectProperties.hubSignatureScannerMemory)
    }
    public boolean getHubSignatureScannerDisabled() {
        return BooleanUtils.toBoolean(detectProperties.hubSignatureScannerDisabled)
    }
    public String getHubSignatureScannerOfflineLocalPath() {
        return detectProperties.hubSignatureScannerOfflineLocalPath?.trim()
    }
    public boolean getPackagistIncludeDevDependencies() {
        return BooleanUtils.toBoolean(detectProperties.packagistIncludeDevDependencies)
    }
    public String getPerlPath() {
        return detectProperties.perlPath?.trim()
    }
    public String getCpanPath() {
        return detectProperties.cpanPath?.trim()
    }
    public String getCpanmPath() {
        return detectProperties.cpanmPath?.trim()
    }
    public String getSbtExcludedConfigurations() {
        return detectProperties.sbtExcludedConfigurations?.trim()
    }
    public String getSbtIncludedConfigurations() {
        return detectProperties.sbtIncludedConfigurations?.trim()
    }
    public String getDefaultProjectVersionScheme() {
        return detectProperties.defaultProjectVersionScheme?.trim()
    }
    public String getDefaultProjectVersionText() {
        return detectProperties.defaultProjectVersionText?.trim()
    }
    public String getDefaultProjectVersionTimeformat() {
        return detectProperties.defaultProjectVersionTimeformat?.trim()
    }
    public String getBomAggregateName() {
        return detectProperties.bomAggregateName?.trim()
    }
    public boolean getRiskReportPdf() {
        return BooleanUtils.toBoolean(detectProperties.riskReportPdf)
    }
    public String getRiskReportPdfPath() {
        return detectProperties.riskReportPdfPath?.trim()
    }
    public boolean getNoticesReport() {
        return BooleanUtils.toBoolean(detectProperties.noticesReport)
    }
    public String getNoticesReportPath() {
        return detectProperties.noticesReportPath?.trim()
    }
    public String getCondaPath() {
        return detectProperties.condaPath?.trim()
    }
    public String getCondaEnvironmentName() {
        return detectProperties.condaEnvironmentName?.trim()
    }
    public String getGradleInspectorAirGapPath() {
        return detectProperties.gradleInspectorAirGapPath?.trim()
    }
    public String getGradleInspectorRepositoryUrl() {
        return detectProperties.gradleInspectorRepositoryUrl?.trim()
    }
    public String getNugetInspectorAirGapPath() {
        return detectProperties.nugetInspectorAirGapPath?.trim()
    }
    public String getNugetPackagesRepoUrl() {
        return detectProperties.nugetPackagesRepoUrl?.trim()
    }
    public boolean getNpmIncludeDevDependencies() {
        return BooleanUtils.toBoolean(detectProperties.npmIncludeDevDependencies)
    }
    public boolean getSuppressResultsOutput() {
        return BooleanUtils.toBoolean(detectProperties.suppressResultsOutput)
    }
    //AUTO-GENERATE PROPERTIES END MARKER
}
