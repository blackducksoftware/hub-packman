/**
 * hub-detect
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.detect;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import com.blackducksoftware.integration.hub.detect.bomtool.bitbake.BitbakeExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.bitbake.BitbakeListTasksParser;
import com.blackducksoftware.integration.hub.detect.bomtool.bitbake.GraphParserTransformer;
import com.blackducksoftware.integration.hub.detect.bomtool.clang.ApkPackageManager;
import com.blackducksoftware.integration.hub.detect.bomtool.clang.ClangExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.clang.ClangLinuxPackageManager;
import com.blackducksoftware.integration.hub.detect.bomtool.clang.CodeLocationAssembler;
import com.blackducksoftware.integration.hub.detect.bomtool.clang.DependenciesListFileManager;
import com.blackducksoftware.integration.hub.detect.bomtool.clang.DpkgPackageManager;
import com.blackducksoftware.integration.hub.detect.bomtool.clang.RpmPackageManager;
import com.blackducksoftware.integration.hub.detect.bomtool.cocoapods.PodlockExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.cocoapods.PodlockParser;
import com.blackducksoftware.integration.hub.detect.bomtool.conda.CondaCliExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.conda.CondaListParser;
import com.blackducksoftware.integration.hub.detect.bomtool.cpan.CpanCliExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.cpan.CpanListParser;
import com.blackducksoftware.integration.hub.detect.bomtool.cran.PackratLockExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.cran.PackratPackager;
import com.blackducksoftware.integration.hub.detect.bomtool.docker.DockerExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.docker.DockerInspectorManager;
import com.blackducksoftware.integration.hub.detect.bomtool.docker.DockerProperties;
import com.blackducksoftware.integration.hub.detect.bomtool.go.DepPackager;
import com.blackducksoftware.integration.hub.detect.bomtool.go.GoDepExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.go.GoInspectorManager;
import com.blackducksoftware.integration.hub.detect.bomtool.go.GoVndrExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.gradle.GradleExecutableFinder;
import com.blackducksoftware.integration.hub.detect.bomtool.gradle.GradleInspectorExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.gradle.GradleInspectorManager;
import com.blackducksoftware.integration.hub.detect.bomtool.gradle.GradleReportParser;
import com.blackducksoftware.integration.hub.detect.bomtool.hex.Rebar3TreeParser;
import com.blackducksoftware.integration.hub.detect.bomtool.hex.RebarExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.maven.MavenCliExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.maven.MavenCodeLocationPackager;
import com.blackducksoftware.integration.hub.detect.bomtool.maven.MavenExecutableFinder;
import com.blackducksoftware.integration.hub.detect.bomtool.npm.NpmCliDependencyFinder;
import com.blackducksoftware.integration.hub.detect.bomtool.npm.NpmCliExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.npm.NpmExecutableFinder;
import com.blackducksoftware.integration.hub.detect.bomtool.npm.NpmLockfileExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.npm.NpmLockfilePackager;
import com.blackducksoftware.integration.hub.detect.bomtool.nuget.NugetInspectorExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.nuget.NugetInspectorInstaller;
import com.blackducksoftware.integration.hub.detect.bomtool.nuget.NugetInspectorManager;
import com.blackducksoftware.integration.hub.detect.bomtool.nuget.NugetInspectorPackager;
import com.blackducksoftware.integration.hub.detect.bomtool.packagist.ComposerLockExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.packagist.PackagistParser;
import com.blackducksoftware.integration.hub.detect.bomtool.pear.PearCliExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.pear.PearDependencyFinder;
import com.blackducksoftware.integration.hub.detect.bomtool.pip.PipInspectorExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.pip.PipInspectorManager;
import com.blackducksoftware.integration.hub.detect.bomtool.pip.PipInspectorTreeParser;
import com.blackducksoftware.integration.hub.detect.bomtool.pip.PipenvExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.pip.PipenvGraphParser;
import com.blackducksoftware.integration.hub.detect.bomtool.pip.PythonExecutableFinder;
import com.blackducksoftware.integration.hub.detect.bomtool.rubygems.GemlockExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.sbt.SbtResolutionCacheExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.yarn.YarnListParser;
import com.blackducksoftware.integration.hub.detect.bomtool.yarn.YarnLockExtractor;
import com.blackducksoftware.integration.hub.detect.bomtool.yarn.YarnLockParser;
import com.blackducksoftware.integration.hub.detect.configuration.ConfigurationManager;
import com.blackducksoftware.integration.hub.detect.configuration.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.configuration.DetectConfigurationUtility;
import com.blackducksoftware.integration.hub.detect.configuration.DetectPropertyMap;
import com.blackducksoftware.integration.hub.detect.configuration.DetectPropertySource;
import com.blackducksoftware.integration.hub.detect.factory.BomToolFactory;
import com.blackducksoftware.integration.hub.detect.help.ArgumentStateParser;
import com.blackducksoftware.integration.hub.detect.help.DetectOptionManager;
import com.blackducksoftware.integration.hub.detect.help.html.HelpHtmlWriter;
import com.blackducksoftware.integration.hub.detect.help.print.DetectConfigurationPrinter;
import com.blackducksoftware.integration.hub.detect.help.print.DetectInfoPrinter;
import com.blackducksoftware.integration.hub.detect.help.print.HelpPrinter;
import com.blackducksoftware.integration.hub.detect.hub.HubServiceManager;
import com.blackducksoftware.integration.hub.detect.interactive.InteractiveManager;
import com.blackducksoftware.integration.hub.detect.interactive.mode.DefaultInteractiveMode;
import com.blackducksoftware.integration.hub.detect.util.DetectFileFinder;
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager;
import com.blackducksoftware.integration.hub.detect.util.MavenMetadataService;
import com.blackducksoftware.integration.hub.detect.util.TildeInPathResolver;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableManager;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner;
import com.blackducksoftware.integration.hub.detect.workflow.DetectProjectManager;
import com.blackducksoftware.integration.hub.detect.workflow.PhoneHomeManager;
import com.blackducksoftware.integration.hub.detect.workflow.codelocation.CodeLocationNameManager;
import com.blackducksoftware.integration.hub.detect.workflow.codelocation.CodeLocationNameService;
import com.blackducksoftware.integration.hub.detect.workflow.codelocation.DetectCodeLocationManager;
import com.blackducksoftware.integration.hub.detect.workflow.diagnostic.DetectRunManager;
import com.blackducksoftware.integration.hub.detect.workflow.diagnostic.DiagnosticFileManager;
import com.blackducksoftware.integration.hub.detect.workflow.diagnostic.DiagnosticLogManager;
import com.blackducksoftware.integration.hub.detect.workflow.diagnostic.DiagnosticManager;
import com.blackducksoftware.integration.hub.detect.workflow.diagnostic.DiagnosticReportManager;
import com.blackducksoftware.integration.hub.detect.workflow.extraction.ExtractionManager;
import com.blackducksoftware.integration.hub.detect.workflow.extraction.StandardExecutableFinder;
import com.blackducksoftware.integration.hub.detect.workflow.hub.BdioUploader;
import com.blackducksoftware.integration.hub.detect.workflow.hub.BlackDuckBinaryScanner;
import com.blackducksoftware.integration.hub.detect.workflow.hub.BlackDuckSignatureScanner;
import com.blackducksoftware.integration.hub.detect.workflow.hub.HubManager;
import com.blackducksoftware.integration.hub.detect.workflow.hub.PolicyChecker;
import com.blackducksoftware.integration.hub.detect.workflow.profiling.BomToolProfiler;
import com.blackducksoftware.integration.hub.detect.workflow.project.BdioManager;
import com.blackducksoftware.integration.hub.detect.workflow.project.BomToolNameVersionDecider;
import com.blackducksoftware.integration.hub.detect.workflow.report.ExtractionSummaryReporter;
import com.blackducksoftware.integration.hub.detect.workflow.report.PreparationSummaryReporter;
import com.blackducksoftware.integration.hub.detect.workflow.report.ReportManager;
import com.blackducksoftware.integration.hub.detect.workflow.report.SearchSummaryReporter;
import com.blackducksoftware.integration.hub.detect.workflow.search.SearchManager;
import com.blackducksoftware.integration.hub.detect.workflow.search.rules.BomToolSearchProvider;
import com.blackducksoftware.integration.hub.detect.workflow.summary.DetectSummaryManager;
import com.blackducksoftware.integration.hub.detect.workflow.summary.StatusSummaryProvider;
import com.blackducksoftware.integration.hub.detect.workflow.swip.SwipCliManager;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.hub.bdio.BdioNodeFactory;
import com.synopsys.integration.hub.bdio.BdioPropertyHelper;
import com.synopsys.integration.hub.bdio.BdioTransformer;
import com.synopsys.integration.hub.bdio.SimpleBdioFactory;
import com.synopsys.integration.hub.bdio.graph.DependencyGraphTransformer;
import com.synopsys.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntegrationEscapeUtil;

import freemarker.template.Configuration;

@org.springframework.context.annotation.Configuration
public class BeanConfiguration {
    private final Logger logger = LoggerFactory.getLogger(BeanConfiguration.class);

    private final ConfigurableEnvironment configurableEnvironment;

    @Autowired
    public BeanConfiguration(final ConfigurableEnvironment configurableEnvironment) {
        this.configurableEnvironment = configurableEnvironment;
    }

    @Bean
    public Gson gson() {
        return HubServicesFactory.createDefaultGsonBuilder().setPrettyPrinting().create();
    }

    @Bean
    public JsonParser jsonParser() {
        return new JsonParser();
    }

    @Bean
    public DetectRunManager detectRunManager() {
        return new DetectRunManager();
    }

    @Bean
    public DiagnosticFileManager diagnosticFileManager() {
        return new DiagnosticFileManager();
    }

    @Bean
    public DiagnosticManager diagnosticManager() {
        return new DiagnosticManager(detectConfiguration(), diagnosticReportManager(), diagnosticLogManager(), detectRunManager(), diagnosticFileManager());
    }

    @Bean
    public DiagnosticLogManager diagnosticLogManager() {
        return new DiagnosticLogManager();
    }

    @Bean
    public DiagnosticReportManager diagnosticReportManager() {
        return new DiagnosticReportManager(bomToolProfiler());
    }

    @Bean
    public SimpleBdioFactory simpleBdioFactory() {
        final BdioPropertyHelper bdioPropertyHelper = new BdioPropertyHelper();
        final BdioNodeFactory bdioNodeFactory = new BdioNodeFactory(bdioPropertyHelper);
        final DependencyGraphTransformer dependencyGraphTransformer = new DependencyGraphTransformer(bdioPropertyHelper, bdioNodeFactory);
        return new SimpleBdioFactory(bdioPropertyHelper, bdioNodeFactory, dependencyGraphTransformer, externalIdFactory(), gson());
    }

    @Bean
    public BomToolProfiler bomToolProfiler() {
        return new BomToolProfiler();
    }

    @Bean
    public BdioTransformer bdioTransformer() {
        return new BdioTransformer();
    }

    @Bean
    public ExternalIdFactory externalIdFactory() {
        return new ExternalIdFactory();
    }

    @Bean
    public IntegrationEscapeUtil integrationEscapeUtil() {
        return new IntegrationEscapeUtil();
    }

    @Bean
    public Configuration configuration() {
        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_26);
        configuration.setClassForTemplateLoading(BeanConfiguration.class, "/");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLogTemplateExceptions(true);

        return configuration;
    }

    @Bean
    public DocumentBuilder xmlDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder();
    }

    @Bean
    public DetectInfo detectInfo() {
        return new DetectInfo();
    }

    @Bean
    public HelpPrinter helpPrinter() {
        return new HelpPrinter();
    }

    @Bean
    public DetectInfoPrinter detectInfoPrinter() {
        return new DetectInfoPrinter();
    }

    @Bean
    public DetectConfigurationPrinter detectConfigurationPrinter() {
        return new DetectConfigurationPrinter();
    }

    @Bean
    public TildeInPathResolver tildeInPathResolver() {
        return new TildeInPathResolver(ConfigurationManager.USER_HOME, detectInfo().getCurrentOs());
    }

    @Bean
    public DetectConfiguration detectConfiguration() {
        return new DetectConfiguration(detectPropertySource(), detectPropertyMap());
    }

    @Bean
    public DetectPropertyMap detectPropertyMap() {
        return new DetectPropertyMap();
    }

    @Bean
    public ConfigurationManager configurationManager() {
        return new ConfigurationManager(tildeInPathResolver(), detectConfiguration(), detectInfoPrinter(), detectConfigurationPrinter());
    }

    @Bean
    public DetectOptionManager detectOptionManager() {
        return new DetectOptionManager(detectConfiguration(), detectInfo());
    }

    @Bean
    public HelpHtmlWriter helpHtmlWriter() {
        return new HelpHtmlWriter(detectOptionManager(), configuration());
    }

    @Bean
    public ArgumentStateParser argumentStateParser() {
        return new ArgumentStateParser();
    }

    @Bean
    public DefaultInteractiveMode defaultInteractiveMode() {
        return new DefaultInteractiveMode(hubServiceManager(), detectOptionManager());
    }

    @Bean
    public InteractiveManager interactiveManager() {
        return new InteractiveManager(detectOptionManager(), defaultInteractiveMode());
    }

    @Bean
    public DetectPropertySource detectPropertySource() {
        return new DetectPropertySource(configurableEnvironment);
    }

    @Bean
    public HubServiceManager hubServiceManager() {
        return new HubServiceManager(detectConfiguration(), detectConfigurationUtility(), cleanupZipExpander(), gson(), jsonParser());
    }

    @Bean
    public SwipCliManager swipCliManager() {
        return new SwipCliManager(detectFileManager(), executableRunner(), detectConfigurationUtility());
    }

    @Bean
    public DetectFileFinder detectFileFinder() {
        return new DetectFileFinder();
    }

    @Bean
    public DetectFileManager detectFileManager() {
        return new DetectFileManager(detectConfiguration(), detectRunManager(), diagnosticManager());
    }

    @Bean
    public ExecutableRunner executableRunner() {
        return new ExecutableRunner(detectConfiguration());
    }

    @Bean
    public ExecutableManager executableManager() {
        return new ExecutableManager(detectFileFinder(), detectInfo());
    }

    @Bean
    public CodeLocationNameService codeLocationNameService() {
        return new CodeLocationNameService(detectFileFinder());
    }

    @Bean
    public CodeLocationNameManager codeLocationNameManager() {
        return new CodeLocationNameManager(detectConfiguration(), codeLocationNameService());
    }

    @Bean
    public SearchSummaryReporter searchSummaryReporter() {
        return new SearchSummaryReporter();
    }

    @Bean
    public PhoneHomeManager phoneHomeManager() {
        return new PhoneHomeManager(detectInfo(), detectConfiguration(), gson());
    }

    @Bean
    public GraphParserTransformer graphParserTransformer() {
        return new GraphParserTransformer();
    }

    @Bean
    public BitbakeListTasksParser bitbakeListTasksParser() {
        return new BitbakeListTasksParser();
    }

    @Bean
    public BitbakeExtractor bitbakeExtractor() {
        return new BitbakeExtractor(executableManager(), executableRunner(), detectConfiguration(), detectFileManager(), detectFileFinder(), graphParserTransformer(), bitbakeListTasksParser());
    }

    @Bean
    public BomToolFactory bomToolFactory() throws ParserConfigurationException {
        return new BomToolFactory(detectConfiguration(), detectFileFinder(), standardExecutableFinder(), executableRunner(), bitbakeExtractor(), clangExtractor(), clangLinuxPackageManagers(), composerLockExtractor(), condaCliExtractor(),
            cpanCliExtractor(), dockerExtractor(), dockerInspectorManager(), gemlockExtractor(), goDepExtractor(), goInspectorManager(), goVndrExtractor(), gradleExecutableFinder(), gradleInspectorExtractor(), gradleInspectorManager(),
            mavenCliExtractor(), mavenExecutableFinder(), npmCliExtractor(), npmExecutableFinder(), npmLockfileExtractor(), nugetInspectorExtractor(), nugetInspectorManager(), packratLockExtractor(), pearCliExtractor(),
            pipInspectorExtractor(), pipInspectorManager(), pipenvExtractor(), podlockExtractor(), pythonExecutableFinder(), rebarExtractor(), sbtResolutionCacheExtractor(), yarnLockExtractor());
    }

    @Bean
    public BomToolSearchProvider bomToolSearchProvider() throws ParserConfigurationException {
        return new BomToolSearchProvider(bomToolFactory(), bomToolProfiler());
    }

    @Bean
    public SearchManager searchManager() throws ParserConfigurationException {
        return new SearchManager(reportManager(), bomToolSearchProvider(), phoneHomeManager(), detectConfiguration());
    }

    @Bean
    public PreparationSummaryReporter preparationSummaryReporter() {
        return new PreparationSummaryReporter();
    }

    @Bean
    public ExtractionSummaryReporter extractionSummaryReporter() {
        return new ExtractionSummaryReporter();
    }

    @Bean
    public ReportManager reportManager() {
        return new ReportManager(bomToolProfiler(), phoneHomeManager(), diagnosticManager(), preparationSummaryReporter(), extractionSummaryReporter(), searchSummaryReporter());
    }

    public ExtractionManager extractionManager() {
        return new ExtractionManager(reportManager());
    }

    @Bean
    public DetectCodeLocationManager detectCodeLocationManager() {
        return new DetectCodeLocationManager(codeLocationNameManager(), detectConfiguration());
    }

    @Bean
    public BdioManager bdioManager() {
        return new BdioManager(detectInfo(), simpleBdioFactory(), integrationEscapeUtil(), codeLocationNameManager(), detectConfiguration());
    }

    @Bean
    public BomToolNameVersionDecider bomToolNameVersionDecider() {
        return new BomToolNameVersionDecider();
    }

    @Bean
    public DetectProjectManager detectProjectManager() throws ParserConfigurationException {
        return new DetectProjectManager(searchManager(), extractionManager(), detectCodeLocationManager(), bdioManager(), bomToolNameVersionDecider(), detectConfiguration(), reportManager());
    }

    @Bean
    public CleanupZipExpander cleanupZipExpander() {
        return new CleanupZipExpander(new Slf4jIntLogger(logger));
    }

    @Bean
    public BlackDuckSignatureScanner blackDuckSignatureScanner() {
        return new BlackDuckSignatureScanner(detectFileManager(), detectFileFinder(), codeLocationNameManager(), detectConfiguration());
    }

    @Bean
    public DetectSummaryManager statusSummary() throws ParserConfigurationException {
        final List<StatusSummaryProvider<?>> statusSummaryProviders = new ArrayList<>();
        statusSummaryProviders.add(detectProjectManager());
        statusSummaryProviders.add(blackDuckSignatureScanner());

        return new DetectSummaryManager(statusSummaryProviders);
    }

    @Bean
    public PolicyChecker policyChecker() {
        return new PolicyChecker(detectConfiguration());
    }

    @Bean
    public BdioUploader bdioUploader() {
        return new BdioUploader(detectConfiguration(), detectFileManager());
    }

    @Bean
    public BlackDuckBinaryScanner blackDuckBinaryScanner() {
        return new BlackDuckBinaryScanner(codeLocationNameService());
    }

    @Bean
    public HubManager hubManager() {
        return new HubManager(bdioUploader(), codeLocationNameManager(), detectConfiguration(), hubServiceManager(), blackDuckSignatureScanner(), policyChecker(), blackDuckBinaryScanner());
    }

    @Bean
    public StandardExecutableFinder standardExecutableFinder() {
        return new StandardExecutableFinder(executableManager(), detectConfiguration());
    }

    @Bean
    public DependenciesListFileManager clangDependenciesListFileParser() {
        return new DependenciesListFileManager(executableRunner());
    }

    @Bean
    public CodeLocationAssembler codeLocationAssembler() {
        return new CodeLocationAssembler(externalIdFactory());
    }

    @Bean
    public ClangExtractor clangExtractor() {
        return new ClangExtractor(executableRunner(), gson(), detectFileFinder(), detectFileManager(), clangDependenciesListFileParser(), codeLocationAssembler());
    }

    public List<ClangLinuxPackageManager> clangLinuxPackageManagers() {
        final List<ClangLinuxPackageManager> clangLinuxPackageManagers = new ArrayList<>();
        clangLinuxPackageManagers.add(new ApkPackageManager());
        clangLinuxPackageManagers.add(new DpkgPackageManager());
        clangLinuxPackageManagers.add(new RpmPackageManager());
        return clangLinuxPackageManagers;
    }

    @Bean
    public PodlockParser podlockParser() {
        return new PodlockParser(externalIdFactory());
    }

    @Bean
    public PodlockExtractor podlockExtractor() {
        return new PodlockExtractor(podlockParser(), externalIdFactory());
    }

    @Bean
    public CondaListParser condaListParser() {
        return new CondaListParser(gson(), externalIdFactory());
    }

    @Bean
    public CondaCliExtractor condaCliExtractor() {
        return new CondaCliExtractor(condaListParser(), externalIdFactory(), executableRunner(), detectConfiguration());
    }

    @Bean
    public CpanListParser cpanListParser() {
        return new CpanListParser(externalIdFactory());
    }

    @Bean
    public CpanCliExtractor cpanCliExtractor() {
        return new CpanCliExtractor(cpanListParser(), externalIdFactory(), executableRunner());
    }

    @Bean
    public PackratPackager packratPackager() {
        return new PackratPackager(externalIdFactory());
    }

    @Bean
    public PackratLockExtractor packratLockExtractor() {
        return new PackratLockExtractor(packratPackager(), externalIdFactory(), detectFileFinder());
    }

    @Bean
    public DockerExtractor dockerExtractor() {
        return new DockerExtractor(detectFileFinder(), detectFileManager(), dockerProperties(), executableRunner(), bdioTransformer(), externalIdFactory(), gson(), blackDuckSignatureScanner());
    }

    @Bean
    public DetectConfigurationUtility detectConfigurationUtility() {
        return new DetectConfigurationUtility(detectConfiguration());
    }

    @Bean
    public DockerInspectorManager dockerInspectorManager() throws ParserConfigurationException {
        return new DockerInspectorManager(detectFileManager(), executableManager(), executableRunner(), detectConfiguration(), detectConfigurationUtility(), mavenMetadataService());
    }

    @Bean
    public DockerProperties dockerProperties() {
        return new DockerProperties(detectConfiguration(), detectPropertySource());
    }

    @Bean
    public GoDepExtractor goDepExtractor() {
        return new GoDepExtractor(depPackager(), externalIdFactory());
    }

    @Bean
    public GoInspectorManager goInspectorManager() {
        return new GoInspectorManager(detectFileManager(), executableManager(), executableRunner(), detectConfiguration());
    }

    @Bean
    public GoVndrExtractor goVndrExtractor() {
        return new GoVndrExtractor(externalIdFactory());
    }

    @Bean
    public DepPackager depPackager() {
        return new DepPackager(executableRunner(), externalIdFactory(), detectConfiguration());
    }

    @Bean
    public GradleReportParser gradleReportParser() {
        return new GradleReportParser(externalIdFactory());
    }

    @Bean
    public GradleExecutableFinder gradleExecutableFinder() {
        return new GradleExecutableFinder(executableManager(), detectConfiguration());
    }

    @Bean
    public GradleInspectorExtractor gradleInspectorExtractor() {
        return new GradleInspectorExtractor(executableRunner(), detectFileFinder(), detectFileManager(), gradleReportParser(), detectConfiguration());
    }

    @Bean
    public MavenMetadataService mavenMetadataService() throws ParserConfigurationException {
        return new MavenMetadataService(xmlDocumentBuilder(), detectConfigurationUtility());
    }

    @Bean
    public GradleInspectorManager gradleInspectorManager() throws ParserConfigurationException {
        return new GradleInspectorManager(detectFileManager(), configuration(), detectConfiguration(), mavenMetadataService());
    }

    @Bean
    public Rebar3TreeParser rebar3TreeParser() {
        return new Rebar3TreeParser(externalIdFactory());
    }

    @Bean
    public RebarExtractor rebarExtractor() {
        return new RebarExtractor(executableRunner(), rebar3TreeParser());
    }

    @Bean
    public MavenCodeLocationPackager mavenCodeLocationPackager() {
        return new MavenCodeLocationPackager(externalIdFactory());
    }

    @Bean
    public MavenCliExtractor mavenCliExtractor() {
        return new MavenCliExtractor(executableRunner(), mavenCodeLocationPackager(), detectConfiguration());
    }

    @Bean
    public MavenExecutableFinder mavenExecutableFinder() {
        return new MavenExecutableFinder(executableManager(), detectConfiguration());
    }

    @Bean
    public NpmCliDependencyFinder npmCliDependencyFinder() {
        return new NpmCliDependencyFinder(externalIdFactory());
    }

    @Bean
    public NpmLockfilePackager npmLockfilePackager() {
        return new NpmLockfilePackager(gson(), externalIdFactory());
    }

    @Bean
    public NpmCliExtractor npmCliExtractor() {
        return new NpmCliExtractor(executableRunner(), npmCliDependencyFinder(), detectConfiguration());
    }

    @Bean
    public NpmLockfileExtractor npmLockfileExtractor() {
        return new NpmLockfileExtractor(npmLockfilePackager(), detectConfiguration());
    }

    @Bean
    public NpmExecutableFinder npmExecutableFinder() {
        return new NpmExecutableFinder(executableManager(), executableRunner(), detectConfiguration());
    }

    @Bean
    public NugetInspectorPackager nugetInspectorPackager() {
        return new NugetInspectorPackager(gson(), externalIdFactory());
    }

    @Bean
    public NugetInspectorExtractor nugetInspectorExtractor() {
        return new NugetInspectorExtractor(detectFileManager(), nugetInspectorPackager(), executableRunner(), detectFileFinder(), detectConfiguration());
    }

    @Bean
    public NugetInspectorInstaller nugetInspectorInstaller() {
        return new NugetInspectorInstaller(detectFileManager(), detectConfiguration(), executableRunner());
    }

    @Bean
    public NugetInspectorManager nugetInspectorManager() throws ParserConfigurationException {
        return new NugetInspectorManager(detectFileManager(), executableManager(), executableRunner(), detectConfiguration());
    }

    @Bean
    public PackagistParser packagistParser() {
        return new PackagistParser(externalIdFactory(), detectConfiguration());
    }

    @Bean
    public ComposerLockExtractor composerLockExtractor() {
        return new ComposerLockExtractor(packagistParser());
    }

    @Bean
    public PearDependencyFinder pearDependencyFinder() {
        return new PearDependencyFinder(externalIdFactory(), detectConfiguration());
    }

    @Bean
    public PearCliExtractor pearCliExtractor() {
        return new PearCliExtractor(detectFileFinder(), externalIdFactory(), pearDependencyFinder(), executableRunner());
    }

    @Bean
    public PipenvGraphParser pipenvGraphParser() {
        return new PipenvGraphParser(externalIdFactory());
    }

    @Bean
    public PipenvExtractor pipenvExtractor() {
        return new PipenvExtractor(executableRunner(), pipenvGraphParser(), detectConfiguration());
    }

    @Bean
    public PipInspectorTreeParser pipInspectorTreeParser() {
        return new PipInspectorTreeParser(externalIdFactory());
    }

    @Bean
    public PipInspectorExtractor pipInspectorExtractor() {
        return new PipInspectorExtractor(executableRunner(), pipInspectorTreeParser(), detectConfiguration());
    }

    @Bean
    public PipInspectorManager pipInspectorManager() {
        return new PipInspectorManager(detectFileManager());
    }

    @Bean
    public PythonExecutableFinder pythonExecutableFinder() {
        return new PythonExecutableFinder(executableManager(), detectConfiguration());
    }

    @Bean
    public GemlockExtractor gemlockExtractor() {
        return new GemlockExtractor(externalIdFactory());
    }

    @Bean
    public SbtResolutionCacheExtractor sbtResolutionCacheExtractor() {
        return new SbtResolutionCacheExtractor(detectFileFinder(), externalIdFactory(), detectConfiguration());
    }

    @Bean
    public YarnListParser yarnListParser() {
        return new YarnListParser(externalIdFactory(), yarnLockParser());
    }

    @Bean
    public YarnLockParser yarnLockParser() {
        return new YarnLockParser();
    }

    @Bean
    public YarnLockExtractor yarnLockExtractor() {
        return new YarnLockExtractor(externalIdFactory(), yarnListParser(), executableRunner(), detectConfiguration());
    }

}
