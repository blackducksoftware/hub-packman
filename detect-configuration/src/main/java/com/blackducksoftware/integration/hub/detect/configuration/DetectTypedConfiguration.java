package com.blackducksoftware.integration.hub.detect.configuration;

public class DetectTypedConfiguration {
    private final DetectConfiguration detectConfiguration;

    public DetectTypedConfiguration(final DetectConfiguration detectConfiguration) {
        this.detectConfiguration = detectConfiguration;
    }

    public Boolean getDetectForceSuccess() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_FORCE_SUCCESS);
    }

    public Boolean getDetectSuppressConfigurationOutput() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_SUPPRESS_CONFIGURATION_OUTPUT);
    }

    public Boolean getDetectSuppressResultsOutput() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_SUPPRESS_RESULTS_OUTPUT);
    }

    public Boolean getDetectCleanup() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_CLEANUP);
    }

    public Boolean getDetectTestConnection() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_TEST_CONNECTION);
    }

    public Long getDetectApiTimeout() {
        return detectConfiguration.getLongProperty(DetectProperty.DETECT_API_TIMEOUT);
    }

    public String getBlackduckHubUrl() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_HUB_URL);
    }

    public String getBlackduckUrl() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_URL);
    }

    public Integer getBlackduckHubTimeout() {
        return detectConfiguration.getIntegerProperty(DetectProperty.BLACKDUCK_HUB_TIMEOUT);
    }

    public Integer getBlackduckTimeout() {
        return detectConfiguration.getIntegerProperty(DetectProperty.BLACKDUCK_TIMEOUT);
    }

    public String getBlackduckHubUsername() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_HUB_USERNAME);
    }

    public String getBlackduckUsername() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_USERNAME);
    }

    public String getBlackduckHubPassword() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_HUB_PASSWORD);
    }

    public String getBlackduckPassword() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_PASSWORD);
    }

    public String getBlackduckHubApiToken() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_HUB_API_TOKEN);
    }

    public String getBlackduckApiToken() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_API_TOKEN);
    }

    public String getBlackduckHubProxyHost() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_HUB_PROXY_HOST);
    }

    public String getBlackduckProxyHost() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_PROXY_HOST);
    }

    public String getBlackduckHubProxyPort() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_HUB_PROXY_PORT);
    }

    public String getBlackduckProxyPort() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_PROXY_PORT);
    }

    public String getBlackduckHubProxyUsername() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_HUB_PROXY_USERNAME);
    }

    public String getBlackduckProxyUsername() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_PROXY_USERNAME);
    }

    public String getBlackduckHubProxyPassword() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_HUB_PROXY_PASSWORD);
    }

    public String getBlackduckProxyPassword() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_PROXY_PASSWORD);
    }

    public String getBlackduckHubProxyNtlmDomain() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_HUB_PROXY_NTLM_DOMAIN);
    }

    public String getBlackduckProxyNtlmDomain() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_PROXY_NTLM_DOMAIN);
    }

    public String getBlackduckHubProxyIgnoredHosts() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_HUB_PROXY_IGNORED_HOSTS);
    }

    public String getBlackduckProxyIgnoredHosts() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_PROXY_IGNORED_HOSTS);
    }

    public String getBlackduckHubProxyNtlmWorkstation() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_HUB_PROXY_NTLM_WORKSTATION);
    }

    public String getBlackduckProxyNtlmWorkstation() {
        return detectConfiguration.getProperty(DetectProperty.BLACKDUCK_PROXY_NTLM_WORKSTATION);
    }

    public Boolean getBlackduckHubTrustCert() {
        return detectConfiguration.getBooleanProperty(DetectProperty.BLACKDUCK_HUB_TRUST_CERT);
    }

    public Boolean getBlackduckTrustCert() {
        return detectConfiguration.getBooleanProperty(DetectProperty.BLACKDUCK_TRUST_CERT);
    }

    public Boolean getBlackduckHubOfflineMode() {
        return detectConfiguration.getBooleanProperty(DetectProperty.BLACKDUCK_HUB_OFFLINE_MODE);
    }

    public Boolean getBlackduckOfflineMode() {
        return detectConfiguration.getBooleanProperty(DetectProperty.BLACKDUCK_OFFLINE_MODE);
    }

    public Boolean getDetectDisableWithoutHub() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_DISABLE_WITHOUT_HUB);
    }

    public Boolean getDetectDisableWithoutBlackduck() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_DISABLE_WITHOUT_BLACKDUCK);
    }

    public Boolean getDetectResolveTildeInPaths() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_RESOLVE_TILDE_IN_PATHS);
    }

    public String getDetectSourcePath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_SOURCE_PATH);
    }

    public String getDetectOutputPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_OUTPUT_PATH);
    }

    public String getDetectBdioOutputPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_BDIO_OUTPUT_PATH);
    }

    public String getDetectScanOutputPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_SCAN_OUTPUT_PATH);
    }

    public Integer getDetectSearchDepth() {
        return detectConfiguration.getIntegerProperty(DetectProperty.DETECT_SEARCH_DEPTH);
    }

    public String getDetectProjectBomTool() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PROJECT_BOM_TOOL);
    }

    public Integer getDetectBomToolSearchDepth() {
        return detectConfiguration.getIntegerProperty(DetectProperty.DETECT_BOM_TOOL_SEARCH_DEPTH);
    }

    public Boolean getDetectBomToolSearchContinue() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_BOM_TOOL_SEARCH_CONTINUE);
    }

    public String[] getDetectBomToolSearchExclusion() {
        return detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_BOM_TOOL_SEARCH_EXCLUSION);
    }

    public Boolean getDetectBomToolSearchExclusionDefaults() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_BOM_TOOL_SEARCH_EXCLUSION_DEFAULTS);
    }

    public String getDetectExcludedBomToolTypes() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_EXCLUDED_BOM_TOOL_TYPES);
    }

    public String getDetectIncludedBomToolTypes() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_INCLUDED_BOM_TOOL_TYPES);
    }

    public String getDetectCodeLocationName() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_CODE_LOCATION_NAME);
    }

    public String getDetectProjectName() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PROJECT_NAME);
    }

    public String getDetectProjectDescription() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PROJECT_DESCRIPTION);
    }

    public String getDetectProjectVersionName() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PROJECT_VERSION_NAME);
    }

    public String getDetectProjectVersionNotes() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PROJECT_VERSION_NOTES);
    }

    public Integer getDetectProjectTier() {
        return detectConfiguration.getIntegerProperty(DetectProperty.DETECT_PROJECT_TIER);
    }

    public String getDetectProjectCodelocationPrefix() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PROJECT_CODELOCATION_PREFIX);
    }

    public String getDetectProjectCodelocationSuffix() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PROJECT_CODELOCATION_SUFFIX);
    }

    public Boolean getDetectProjectCodelocationUnmap() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_PROJECT_CODELOCATION_UNMAP);
    }

    public Boolean getDetectProjectLevelAdjustments() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_PROJECT_LEVEL_ADJUSTMENTS);
    }

    public String getDetectProjectVersionPhase() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PROJECT_VERSION_PHASE);
    }

    public String[] getDetectProjectCloneCategories() {
        return detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_PROJECT_CLONE_CATEGORIES);
    }

    public String getDetectCloneProjectName() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_CLONE_PROJECT_NAME);
    }

    public String getDetectCloneProjectVersionName() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_CLONE_PROJECT_VERSION_NAME);
    }

    public String getDetectProjectVersionDistribution() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PROJECT_VERSION_DISTRIBUTION);
    }

    public Boolean getDetectProjectVersionUpdate() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_PROJECT_VERSION_UPDATE);
    }

    public String getDetectPolicyCheckFailOnSeverities() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_POLICY_CHECK_FAIL_ON_SEVERITIES);
    }

    public String getDetectGradleInspectorVersion() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_INSPECTOR_VERSION);
    }

    public String getDetectGradleBuildCommand() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_BUILD_COMMAND);
    }

    public String getDetectGradleExcludedConfigurations() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_EXCLUDED_CONFIGURATIONS);
    }

    public String getDetectGradleIncludedConfigurations() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_INCLUDED_CONFIGURATIONS);
    }

    public String getDetectGradleExcludedProjects() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_EXCLUDED_PROJECTS);
    }

    public String getDetectGradleIncludedProjects() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_INCLUDED_PROJECTS);
    }

    public String getDetectNugetConfigPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_CONFIG_PATH);
    }

    public String getDetectNugetInspectorName() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_INSPECTOR_NAME);
    }

    public String getDetectNugetInspectorVersion() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_INSPECTOR_VERSION);
    }

    public String getDetectNugetExcludedModules() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_EXCLUDED_MODULES);
    }

    public String getDetectNugetIncludedModules() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_INCLUDED_MODULES);
    }

    public Boolean getDetectNugetIgnoreFailure() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_NUGET_IGNORE_FAILURE);
    }

    public String getDetectMavenScope() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_MAVEN_SCOPE);
    }

    public String getDetectMavenBuildCommand() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_MAVEN_BUILD_COMMAND);
    }

    public String getDetectGradlePath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_PATH);
    }

    public String getDetectMavenPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_MAVEN_PATH);
    }

    public String getDetectMavenExcludedModules() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_MAVEN_EXCLUDED_MODULES);
    }

    public String getDetectMavenIncludedModules() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_MAVEN_INCLUDED_MODULES);
    }

    public String getDetectNugetPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_PATH);
    }

    public String getDetectPipProjectName() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PIP_PROJECT_NAME);
    }

    public String getDetectPipProjectVersionName() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PIP_PROJECT_VERSION_NAME);
    }

    public Boolean getDetectPythonPython3() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_PYTHON_PYTHON3);
    }

    public String getDetectPythonPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PYTHON_PATH);
    }

    public String getDetectPipenvPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PIPENV_PATH);
    }

    public String getDetectNpmPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_NPM_PATH);
    }

    public Boolean getDetectNpmIncludeDevDependencies() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_NPM_INCLUDE_DEV_DEPENDENCIES);
    }

    public String getDetectNpmNodePath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_NPM_NODE_PATH);
    }

    public String getDetectPearPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PEAR_PATH);
    }

    public Boolean getDetectPearOnlyRequiredDeps() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_PEAR_ONLY_REQUIRED_DEPS);
    }

    public String getDetectPipRequirementsPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PIP_REQUIREMENTS_PATH);
    }

    public String getDetectGoDepPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_GO_DEP_PATH);
    }

    public Boolean getDetectGoRunDepInit() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_GO_RUN_DEP_INIT);
    }

    public String getDetectDockerPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_PATH);
    }

    public Boolean getDetectDockerPathRequired() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_DOCKER_PATH_REQUIRED);
    }

    public String getDetectDockerInspectorPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_INSPECTOR_PATH);
    }

    public String getDetectDockerInspectorVersion() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_INSPECTOR_VERSION);
    }

    public String getDetectDockerTar() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_TAR);
    }

    public String getDetectDockerImage() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_IMAGE);
    }

    public String getDetectBashPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_BASH_PATH);
    }

    public String getLoggingLevelComBlackducksoftwareIntegration() {
        return detectConfiguration.getProperty(DetectProperty.LOGGING_LEVEL_COM_BLACKDUCKSOFTWARE_INTEGRATION);
    }

    public Boolean getDetectHubSignatureScannerDryRun() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_DRY_RUN);
    }

    public Boolean getDetectBlackduckSignatureScannerDryRun() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_DRY_RUN);
    }

    public Boolean getDetectHubSignatureScannerSnippetMode() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_SNIPPET_MODE);
    }

    public Boolean getDetectBlackduckSignatureScannerSnippetMode() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_SNIPPET_MODE);
    }

    public String[] getDetectHubSignatureScannerExclusionPatterns() {
        return detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_EXCLUSION_PATTERNS);
    }

    public String[] getDetectBlackduckSignatureScannerExclusionPatterns() {
        return detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_EXCLUSION_PATTERNS);
    }

    public String[] getDetectHubSignatureScannerPaths() {
        return detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_PATHS);
    }

    public String[] getDetectBlackduckSignatureScannerPaths() {
        return detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_PATHS);
    }

    public String[] getDetectHubSignatureScannerExclusionNamePatterns() {
        return detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_EXCLUSION_NAME_PATTERNS);
    }

    public String[] getDetectBlackduckSignatureScannerExclusionNamePatterns() {
        return detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_EXCLUSION_NAME_PATTERNS);
    }

    public Integer getDetectHubSignatureScannerMemory() {
        return detectConfiguration.getIntegerProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_MEMORY);
    }

    public Integer getDetectBlackduckSignatureScannerMemory() {
        return detectConfiguration.getIntegerProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_MEMORY);
    }

    public Boolean getDetectHubSignatureScannerDisabled() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_DISABLED);
    }

    public Boolean getDetectBlackduckSignatureScannerDisabled() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_DISABLED);
    }

    public String getDetectHubSignatureScannerOfflineLocalPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_OFFLINE_LOCAL_PATH);
    }

    public String getDetectBlackduckSignatureScannerOfflineLocalPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_OFFLINE_LOCAL_PATH);
    }

    public String getDetectHubSignatureScannerLocalPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_LOCAL_PATH);
    }

    public String getDetectBlackduckSignatureScannerLocalPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_LOCAL_PATH);
    }

    public String getDetectHubSignatureScannerHostUrl() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_HOST_URL);
    }

    public String getDetectBlackduckSignatureScannerHostUrl() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_HOST_URL);
    }

    public Integer getDetectHubSignatureScannerParallelProcessors() {
        return detectConfiguration.getIntegerProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_PARALLEL_PROCESSORS);
    }

    public Integer getDetectBlackduckSignatureScannerParallelProcessors() {
        return detectConfiguration.getIntegerProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_PARALLEL_PROCESSORS);
    }

    public String getDetectHubSignatureScannerArguments() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_ARGUMENTS);
    }

    public String getDetectBlackduckSignatureScannerArguments() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_ARGUMENTS);
    }

    public String getDetectBinaryScanFilePath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_BINARY_SCAN_FILE);
    }

    public Boolean getDetectPackagistIncludeDevDependencies() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_PACKAGIST_INCLUDE_DEV_DEPENDENCIES);
    }

    public String getDetectPerlPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_PERL_PATH);
    }

    public String getDetectCpanPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_CPAN_PATH);
    }

    public String getDetectCpanmPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_CPANM_PATH);
    }

    public String getDetectSbtExcludedConfigurations() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_SBT_EXCLUDED_CONFIGURATIONS);
    }

    public String getDetectSbtIncludedConfigurations() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_SBT_INCLUDED_CONFIGURATIONS);
    }

    public String getDetectDefaultProjectVersionScheme() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_DEFAULT_PROJECT_VERSION_SCHEME);
    }

    public String getDetectDefaultProjectVersionText() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_DEFAULT_PROJECT_VERSION_TEXT);
    }

    public String getDetectDefaultProjectVersionTimeformat() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_DEFAULT_PROJECT_VERSION_TIMEFORMAT);
    }

    public String getDetectBomAggregateName() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_BOM_AGGREGATE_NAME);
    }

    public Boolean getDetectRiskReportPdf() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_RISK_REPORT_PDF);
    }

    public String getDetectRiskReportPdfPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_RISK_REPORT_PDF_PATH);
    }

    public Boolean getDetectNoticesReport() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_NOTICES_REPORT);
    }

    public String getDetectNoticesReportPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_NOTICES_REPORT_PATH);
    }

    public String getDetectCondaPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_CONDA_PATH);
    }

    public String getDetectCondaEnvironmentName() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_CONDA_ENVIRONMENT_NAME);
    }

    public String getDetectDockerInspectorAirGapPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_INSPECTOR_AIR_GAP_PATH);
    }

    public String getDetectGradleInspectorAirGapPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_INSPECTOR_AIR_GAP_PATH);
    }

    public String getDetectNugetInspectorAirGapPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_INSPECTOR_AIR_GAP_PATH);
    }

    public String[] getDetectNugetPackagesRepoUrl() {
        return detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_NUGET_PACKAGES_REPO_URL);
    }

    public String getDetectGradleInspectorRepositoryUrl() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_INSPECTOR_REPOSITORY_URL);
    }

    public String getDetectHexRebar3Path() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_HEX_REBAR3_PATH);
    }

    public String getDetectYarnPath() {
        return detectConfiguration.getProperty(DetectProperty.DETECT_YARN_PATH);
    }

    public Boolean getDetectYarnProdOnly() {
        return detectConfiguration.getBooleanProperty(DetectProperty.DETECT_YARN_PROD_ONLY);
    }

}
