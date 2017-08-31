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

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.detect.help.ValueDescription

import groovy.transform.TypeChecked

/**
 * THIS IS A GENERATED FILE. DO NOT MODIFY
 */
@Component
@TypeChecked
class DetectProperties {
    private static final String GROUP_PROJECT_INFO = 'project info'
    private static final String GROUP_HUB_CONFIGURATION = 'hub configuration'
    private static final String GROUP_LOGGING = 'logging'
    private static final String GROUP_CLEANUP = 'cleanup'
    private static final String GROUP_PATHS = 'paths'
    private static final String GROUP_BOMTOOL = 'bomtool'
    private static final String GROUP_CONDA = 'conda'
    private static final String GROUP_CPAN = 'cpan'
    private static final String GROUP_DOCKER = 'docker'
    private static final String GROUP_GO = 'go'
    private static final String GROUP_GRADLE = 'gradle'
    private static final String GROUP_MAVEN = 'maven'
    private static final String GROUP_NPM = 'npm'
    private static final String GROUP_NUGET = 'nuget'
    private static final String GROUP_PACKAGIST = 'packagist'
    private static final String GROUP_PEAR = 'pear'
    private static final String GROUP_PIP = 'pip'
    private static final String GROUP_POLICY_CHECK = 'policy check'
    private static final String GROUP_SBT = 'sbt'
    private static final String GROUP_SIGNITURE_SCANNER = 'signiture scanner'

    //////////////////////// GROUP_PROJECT_INFO ////////////////////////
    @ValueDescription(description='If set, this will aggregate all the BOMs to create a single BDIO file with the name provided. For Co-Pilot use only', defaultValue='', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.bom.aggregate.name)}')
    String bomAggregateName

    @ValueDescription(description='An override for the name to use for the Hub project. If not supplied, detect will attempt to use the tools to figure out a reasonable project name. If that fails, the final part of the directory path where the inspection is taking place will be used', defaultValue='', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.project.name)}')
    String projectName

    @ValueDescription(description='An override for the Project level matches', defaultValue='true', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.project.level.adjustments)}')
    Boolean projectLevelAdjustments

    @ValueDescription(description='An override for the version to use for the Hub project. If not supplied, detect will attempt to use the tools to figure out a reasonable version name. If that fails, the current date will be used', defaultValue='', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.project.version.name)}')
    String projectVersionName

    @ValueDescription(description='An override for the Project Version phase', defaultValue='Development', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.project.version.phase)}')
    String projectVersionPhase

    @ValueDescription(description='An override for the Project Version distribution', defaultValue='External', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.project.version.distribution)}')
    String projectVersionDistribution

    @ValueDescription(description='A prefix to the name of the codelocations created by Detect. Useful for running against the same projects on multiple machines', defaultValue='', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.project.codelocation.prefix)}')
    String projectCodelocationPrefix

    @ValueDescription(description='The scheme to use when the package managers can not determine a version, either &#39;text&#39; or &#39;timestamp&#39;', defaultValue='text', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.default.project.version.scheme)}')
    String defaultProjectVersionScheme

    @ValueDescription(description='The text to use as the default project version', defaultValue='Detect Unkown Version', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.default.project.version.text)}')
    String defaultProjectVersionText

    @ValueDescription(description='The timestamp format to use as the default project version', defaultValue='yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.default.project.version.timeformat)}')
    String defaultProjectVersionTimeformat

    @ValueDescription(description='When set to true, a Black Duck risk report in PDF form will be created', defaultValue='false', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.risk.report.pdf)}')
    Boolean riskReportPdf

    @ValueDescription(description='The output directory for risk report in PDF. Default is the source directory', defaultValue='.', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.risk.report.pdf.path)}')
    String riskReportPdfPath

    @ValueDescription(description='When set to true, a Black Duck notices report in text form will be created in your source directory', defaultValue='false', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.notices.report)}')
    Boolean noticesReport

    @ValueDescription(description='The output directory for notices report. Default is the source directory', defaultValue='.', group=DetectProperties.GROUP_PROJECT_INFO)
    @Value('${(detect.notices.report.path)}')
    String noticesReportPath

    //////////////////////// GROUP_HUB_CONFIGURATION ////////////////////////
    @ValueDescription(description='URL of the Hub server', defaultValue='', group=DetectProperties.GROUP_HUB_CONFIGURATION)
    @Value('${(blackduck.hub.url)}')
    String hubUrl

    @ValueDescription(description='Time to wait for rest connections to complete', defaultValue='120', group=DetectProperties.GROUP_HUB_CONFIGURATION)
    @Value('${(blackduck.hub.timeout)}')
    Integer hubTimeout

    @ValueDescription(description='Hub username', defaultValue='', group=DetectProperties.GROUP_HUB_CONFIGURATION)
    @Value('${(blackduck.hub.username)}')
    String hubUsername

    @ValueDescription(description='Hub password', defaultValue='', group=DetectProperties.GROUP_HUB_CONFIGURATION)
    @Value('${(blackduck.hub.password)}')
    String hubPassword

    @ValueDescription(description='If true the Hub https certificate will be automatically imported', defaultValue='false', group=DetectProperties.GROUP_HUB_CONFIGURATION)
    @Value('${(blackduck.hub.auto.import.cert)}')
    Boolean hubAutoImportCert

    @ValueDescription(description='This can disable any Hub communication - if true, Detect will not upload BDIO files, it will not check policies, and it will not download and install the signature scanner', defaultValue='false', group=DetectProperties.GROUP_HUB_CONFIGURATION)
    @Value('${(blackduck.hub.offline.mode)}')
    Boolean hubOfflineMode

    @ValueDescription(description='Proxy host', defaultValue='', group=DetectProperties.GROUP_HUB_CONFIGURATION)
    @Value('${(blackduck.hub.proxy.host)}')
    String hubProxyHost

    @ValueDescription(description='Proxy port', defaultValue='', group=DetectProperties.GROUP_HUB_CONFIGURATION)
    @Value('${(blackduck.hub.proxy.port)}')
    String hubProxyPort

    @ValueDescription(description='Proxy username', defaultValue='', group=DetectProperties.GROUP_HUB_CONFIGURATION)
    @Value('${(blackduck.hub.proxy.username)}')
    String hubProxyUsername

    @ValueDescription(description='Project password', defaultValue='', group=DetectProperties.GROUP_HUB_CONFIGURATION)
    @Value('${(blackduck.hub.proxy.password)}')
    String hubProxyPassword

    //////////////////////// GROUP_LOGGING ////////////////////////
    @ValueDescription(description='If true, the default behavior of printing your configuration properties at startup will be suppressed', defaultValue='false', group=DetectProperties.GROUP_LOGGING)
    @Value('${(detect.suppress.configuration.output)}')
    Boolean suppressConfigurationOutput

    @ValueDescription(description='The logging level of Detect (ALL|TRACE|DEBUG|INFO|WARN|ERROR|FATAL|OFF)', defaultValue='INFO', group=DetectProperties.GROUP_LOGGING)
    @Value('${(logging.level.com.blackducksoftware.integration)}')
    String levelComBlackducksoftwareIntegration

    @ValueDescription(description='The logging level to run Detect at', defaultValue='${LOG_LEVEL_PATTERN}%clr(---){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:%wEx}', group=DetectProperties.GROUP_LOGGING)
    @Value('${(logging.pattern.console)}')
    String patternConsole

    //////////////////////// GROUP_CLEANUP ////////////////////////
    @ValueDescription(description='Detect creates temporary files in the output directory. If set to true this will clean them up after execution', defaultValue='true', group=DetectProperties.GROUP_CLEANUP)
    @Value('${(detect.cleanup.bom.tool.files)}')
    Boolean cleanupBomToolFiles

    @ValueDescription(description='If true the bdio files will be deleted after upload', defaultValue='true', group=DetectProperties.GROUP_CLEANUP)
    @Value('${(detect.cleanup.bdio.files)}')
    Boolean cleanupBdioFiles

    //////////////////////// GROUP_PATHS ////////////////////////
    @ValueDescription(description='Source path to inspect', defaultValue='', group=DetectProperties.GROUP_PATHS)
    @Value('${(detect.source.path)}')
    String sourcePath

    @ValueDescription(description='Output path', defaultValue='', group=DetectProperties.GROUP_PATHS)
    @Value('${(detect.output.path)}')
    String outputPath

    @ValueDescription(description='Depth from source paths to search for files.', defaultValue='3', group=DetectProperties.GROUP_PATHS)
    @Value('${(detect.search.depth)}')
    Integer searchDepth

    @ValueDescription(description='Path of the bash executable', defaultValue='', group=DetectProperties.GROUP_PATHS)
    @Value('${(detect.bash.path)}')
    String bashPath

    //////////////////////// GROUP_BOMTOOL ////////////////////////
    @ValueDescription(description='By default, all tools will be included. If you want to exclude specific tools, specify the ones to exclude here. Exclusion rules always win.', defaultValue='', group=DetectProperties.GROUP_BOMTOOL)
    @Value('${(detect.excluded.bom.tool.types)}')
    String excludedBomToolTypes

    @ValueDescription(description='By default, all tools will be included. If you want to include only specific tools, specify the ones to include here. Exclusion rules always win.', defaultValue='', group=DetectProperties.GROUP_BOMTOOL)
    @Value('${(detect.included.bom.tool.types)}')
    String includedBomToolTypes

    //////////////////////// GROUP_CONDA ////////////////////////
    @ValueDescription(description='The path to the conda executable', defaultValue='', group=DetectProperties.GROUP_CONDA)
    @Value('${(detect.conda.path)}')
    String condaPath

    @ValueDescription(description='The name of the anaconda environment used by your project', defaultValue='', group=DetectProperties.GROUP_CONDA)
    @Value('${(detect.conda.environment.name)}')
    String condaEnvironmentName

    //////////////////////// GROUP_CPAN ////////////////////////
    @ValueDescription(description='The path to the cpan executable', defaultValue='', group=DetectProperties.GROUP_CPAN)
    @Value('${(detect.cpan.path)}')
    String cpanPath

    @ValueDescription(description='The path to the cpanm executable', defaultValue='', group=DetectProperties.GROUP_CPAN)
    @Value('${(detect.cpanm.path)}')
    String cpanmPath

    @ValueDescription(description='The path to the perl executable', defaultValue='', group=DetectProperties.GROUP_CPAN)
    @Value('${(detect.perl.path)}')
    String perlPath

    //////////////////////// GROUP_DOCKER ////////////////////////
    @ValueDescription(description='This is used to override using the hosted script by github url. You can provide your own script at this path', defaultValue='', group=DetectProperties.GROUP_DOCKER)
    @Value('${(detect.docker.inspector.path)}')
    String dockerInspectorPath

    @ValueDescription(description='Version of the Hub Docker Inspector to use', defaultValue='latest', group=DetectProperties.GROUP_DOCKER)
    @Value('${(detect.docker.inspector.version)}')
    String dockerInspectorVersion

    @ValueDescription(description='A saved docker image - must be a .tar file. For detect to run docker either this property or detect.docker.image must be set', defaultValue='', group=DetectProperties.GROUP_DOCKER)
    @Value('${(detect.docker.tar)}')
    String dockerTar

    @ValueDescription(description='The docker image name to inspect. For detect to run docker either this property or detect.docker.tar must be set', defaultValue='', group=DetectProperties.GROUP_DOCKER)
    @Value('${(detect.docker.image)}')
    String dockerImage

    @ValueDescription(description='The path to the docker executable', defaultValue='', group=DetectProperties.GROUP_DOCKER)
    @Value('${(detect.docker.path)}')
    String dockerPath

    //////////////////////// GROUP_GO ////////////////////////
    @ValueDescription(description='The path to the Go Dep executable', defaultValue='', group=DetectProperties.GROUP_GO)
    @Value('${(detect.go.dep.path)}')
    String goDepPath

    //////////////////////// GROUP_GRADLE ////////////////////////
    @ValueDescription(description='Version of the Gradle Inspector', defaultValue='0.2.1', group=DetectProperties.GROUP_GRADLE)
    @Value('${(detect.gradle.inspector.version)}')
    String gradleInspectorVersion

    @ValueDescription(description='The path to the directory containing the air gap dependencies for the gradle inspector', defaultValue='', group=DetectProperties.GROUP_GRADLE)
    @Value('${(detect.gradle.inspector.air.gap.path)}')
    String gradleInspectorAirGapPath

    @ValueDescription(description='The path to the Gradle executable', defaultValue='', group=DetectProperties.GROUP_GRADLE)
    @Value('${(detect.gradle.path)}')
    String gradlePath

    @ValueDescription(description='Gradle build command', defaultValue='dependencies', group=DetectProperties.GROUP_GRADLE)
    @Value('${(detect.gradle.build.command)}')
    String gradleBuildCommand

    @ValueDescription(description='The names of the dependency configurations to exclude', defaultValue='', group=DetectProperties.GROUP_GRADLE)
    @Value('${(detect.gradle.excluded.configurations)}')
    String gradleExcludedConfigurations

    @ValueDescription(description='The names of the dependency configurations to include', defaultValue='', group=DetectProperties.GROUP_GRADLE)
    @Value('${(detect.gradle.included.configurations)}')
    String gradleIncludedConfigurations

    @ValueDescription(description='The names of the projects to exclude', defaultValue='', group=DetectProperties.GROUP_GRADLE)
    @Value('${(detect.gradle.excluded.projects)}')
    String gradleExcludedProjects

    @ValueDescription(description='The names of the projects to include', defaultValue='', group=DetectProperties.GROUP_GRADLE)
    @Value('${(detect.gradle.included.projects)}')
    String gradleIncludedProjects

    @ValueDescription(description='Set this to false if you do not want the &#39;blackduck&#39; directory in your build directory to be deleted', defaultValue='true', group=DetectProperties.GROUP_GRADLE)
    @Value('${(detect.gradle.cleanup.build.blackduck.directory)}')
    Boolean gradleCleanupBuildBlackduckDirectory

    //////////////////////// GROUP_MAVEN ////////////////////////
    @ValueDescription(description='The name of the dependency scope to include', defaultValue='', group=DetectProperties.GROUP_MAVEN)
    @Value('${(detect.maven.scope)}')
    String mavenScope

    @ValueDescription(description='The path to the Maven executable', defaultValue='', group=DetectProperties.GROUP_MAVEN)
    @Value('${(detect.maven.path)}')
    String mavenPath

    //////////////////////// GROUP_NPM ////////////////////////
    @ValueDescription(description='The path to the Npm executable', defaultValue='', group=DetectProperties.GROUP_NPM)
    @Value('${(detect.npm.path)}')
    String npmPath

    //////////////////////// GROUP_NUGET ////////////////////////
    @ValueDescription(description='Name of the Nuget Inspector', defaultValue='IntegrationNugetInspector', group=DetectProperties.GROUP_NUGET)
    @Value('${(detect.nuget.inspector.name)}')
    String nugetInspectorName

    @ValueDescription(description='Version of the Nuget Inspector', defaultValue='2.0.0', group=DetectProperties.GROUP_NUGET)
    @Value('${(detect.nuget.inspector.version)}')
    String nugetInspectorVersion

    @ValueDescription(description='The path to the nuget inspector nupkg file', defaultValue='', group=DetectProperties.GROUP_NUGET)
    @Value('${(detect.nuget.inspector.air.gap.path)}')
    String nugetInspectorAirGapPath

    @ValueDescription(description='The source for nuget packages', defaultValue='https://www.nuget.org/api/v2/', group=DetectProperties.GROUP_NUGET)
    @Value('${(detect.nuget.packages.repo.url)}')
    String nugetPackagesRepoUrl

    @ValueDescription(description='If true errors will be logged and then ignored', defaultValue='false', group=DetectProperties.GROUP_NUGET)
    @Value('${(detect.nuget.ignore.failure)}')
    Boolean nugetIgnoreFailure

    @ValueDescription(description='The names of the projects in a solution to exclude', defaultValue='', group=DetectProperties.GROUP_NUGET)
    @Value('${(detect.nuget.excluded.modules)}')
    String nugetExcludedModules

    @ValueDescription(description='The path to the Nuget executable', defaultValue='', group=DetectProperties.GROUP_NUGET)
    @Value('${(detect.nuget.path)}')
    String nugetPath

    //////////////////////// GROUP_PACKAGIST ////////////////////////
    @ValueDescription(description='Set this value to false if you would like to exclude your dev requires dependencies when ran', defaultValue='true', group=DetectProperties.GROUP_PACKAGIST)
    @Value('${(detect.packagist.include.dev.dependencies)}')
    Boolean packagistIncludeDevDependencies

    //////////////////////// GROUP_PEAR ////////////////////////
    @ValueDescription(description='The path to the Pear executable', defaultValue='', group=DetectProperties.GROUP_PEAR)
    @Value('${(detect.pear.path)}')
    String pearPath

    @ValueDescription(description='Set to true if you would like to include the packages that are not required', defaultValue='false', group=DetectProperties.GROUP_PEAR)
    @Value('${(detect.pear.not.required.dependencies)}')
    Boolean pearNotRequiredDependencies

    //////////////////////// GROUP_PIP ////////////////////////
    @ValueDescription(description='The path to the python executable', defaultValue='', group=DetectProperties.GROUP_PIP)
    @Value('${(detect.python.path)}')
    String pythonPath

    @ValueDescription(description='The path to the pip executable', defaultValue='', group=DetectProperties.GROUP_PIP)
    @Value('${(detect.pip.path)}')
    String pipPath

    @ValueDescription(description='Override for pip inspector to find your project', defaultValue='', group=DetectProperties.GROUP_PIP)
    @Value('${(detect.pip.project.name)}')
    String pipProjectName

    @ValueDescription(description='If true, detect will use pip3 if available on path', defaultValue='false', group=DetectProperties.GROUP_PIP)
    @Value('${(detect.pip.pip3)}')
    Boolean pipPip3

    @ValueDescription(description='The path to a requirements file', defaultValue='', group=DetectProperties.GROUP_PIP)
    @Value('${(detect.pip.requirements.path)}')
    String pipRequirementsPath

    @ValueDescription(description='The path to a user&#39;s virtual environment', defaultValue='', group=DetectProperties.GROUP_PIP)
    @Value('${(detect.pip.virtualEnv.path)}')
    String pipVirtualEnvPath

    //////////////////////// GROUP_POLICY_CHECK ////////////////////////
    @ValueDescription(description='Set to true if you would like a policy check from the hub for your project', defaultValue='false', group=DetectProperties.GROUP_POLICY_CHECK)
    @Value('${(detect.policy.check)}')
    Boolean policyCheck

    @ValueDescription(description='Timeout for the Hub&#39;s policy check response. When changing this value, keep in mind the checking of policies might have to wait for a new scan to process which can take some time', defaultValue='300000', group=DetectProperties.GROUP_POLICY_CHECK)
    @Value('${(detect.policy.check.timeout)}')
    Long policyCheckTimeout

    //////////////////////// GROUP_SBT ////////////////////////
    @ValueDescription(description='The names of the sbt configurations to exclude', defaultValue='', group=DetectProperties.GROUP_SBT)
    @Value('${(detect.sbt.excluded.configurations)}')
    String sbtExcludedConfigurations

    @ValueDescription(description='The names of the sbt configurations to include', defaultValue='', group=DetectProperties.GROUP_SBT)
    @Value('${(detect.sbt.included.configurations)}')
    String sbtIncludedConfigurations

    //////////////////////// GROUP_SIGNITURE_SCANNER ////////////////////////
    @ValueDescription(description='The relative paths of directories to be excluded from scan registration', defaultValue='', group=DetectProperties.GROUP_SIGNITURE_SCANNER)
    @Value('${(detect.hub.signature.scanner.relative.paths.to.exclude)}')
    String[] hubSignatureScannerRelativePathsToExclude

    @ValueDescription(description='Enables you to specify sub-directories to exclude from scans', defaultValue='', group=DetectProperties.GROUP_SIGNITURE_SCANNER)
    @Value('${(detect.hub.signature.scanner.exclusion.patterns)}')
    String[] hubSignatureScannerExclusionPatterns

    @ValueDescription(description='These paths and only these paths will be scanned', defaultValue='', group=DetectProperties.GROUP_SIGNITURE_SCANNER)
    @Value('${(detect.hub.signature.scanner.paths)}')
    String[] hubSignatureScannerPaths

    @ValueDescription(description='The memory for the scanner to use', defaultValue='4096', group=DetectProperties.GROUP_SIGNITURE_SCANNER)
    @Value('${(detect.hub.signature.scanner.memory)}')
    Integer hubSignatureScannerMemory

    @ValueDescription(description='Set to true to disable the Hub Signature Scanner', defaultValue='false', group=DetectProperties.GROUP_SIGNITURE_SCANNER)
    @Value('${(detect.hub.signature.scanner.disabled)}')
    Boolean hubSignatureScannerDisabled

    @ValueDescription(description='If set to true, the signature scanner results will not be uploaded to the Hub and the scanner results will be written to disk', defaultValue='false', group=DetectProperties.GROUP_SIGNITURE_SCANNER)
    @Value('${(detect.hub.signature.scanner.dry.run)}')
    Boolean hubSignatureScannerDryRun

    @ValueDescription(description='To use a local signature scanner, set its location with this property. This will be the path that contains the &#39;Hub_Scan_Installation&#39; directory where the signature scanner was unzipped', defaultValue='', group=DetectProperties.GROUP_SIGNITURE_SCANNER)
    @Value('${(detect.hub.signature.scanner.offline.local.path)}')
    String hubSignatureScannerOfflineLocalPath

}
