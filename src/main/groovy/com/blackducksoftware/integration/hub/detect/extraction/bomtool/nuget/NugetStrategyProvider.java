package com.blackducksoftware.integration.hub.detect.extraction.bomtool.nuget;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.detect.extraction.requirement.NugetInspectorRequirement;
import com.blackducksoftware.integration.hub.detect.extraction.strategy.Strategy;
import com.blackducksoftware.integration.hub.detect.extraction.strategy.StrategyProvider;
import com.blackducksoftware.integration.hub.detect.model.BomToolType;

@Component
public class NugetStrategyProvider extends StrategyProvider {

    static final String[] SOLUTION_PATTERN = new String[] { "*.sln" };
    //populated from "open project" in visual studio 2017
    static final String[] SUPPORTED_PROJECT_PATTERNS = new String[] {
            //C#
            "*.csproj",
            //F#
            "*.fsproj",
            //VB
            "*.vbproj",
            //Azure Stream Analytics
            "*.asaproj",
            //Docker Compose
            "*.dcproj",
            //Shared Projects
            "*.shproj",
            //Cloud Computing
            "*.ccproj",
            //Fabric Application
            "*.sfproj",
            //Node.js
            "*.njsproj",
            //VC++
            "*.vcxproj",
            //VC++
            "*.vcproj",
            //.NET Core
            "*.xproj",
            //Python
            "*.pyproj",
            //Hive
            "*.hiveproj",
            //Pig
            "*.pigproj",
            //JavaScript
            "*.jsproj",
            //U-SQL
            "*.usqlproj",
            //Deployment
            "*.deployproj",
            //Common Project System Files
            "*.msbuildproj",
            //SQL
            "*.sqlproj",
            //SQL Project Files
            "*.dbproj",
            //RStudio
            "*.rproj"
    };


    @SuppressWarnings("rawtypes")
    @Override
    public List<Strategy> createStrategies() {

        final Strategy solutionStrategy = newStrategyBuilder(NugetInspectorContext.class, NugetInspectorExtractor.class)
                .needsBomTool(BomToolType.NUGET).noop()
                .needsCurrentDirectory((context, file) -> context.directory = file)
                .needsFiles(SOLUTION_PATTERN).noop()
                .demands(new NugetInspectorRequirement(), (context, file) -> context.inspectorExe = file)
                .build();

        final Strategy projectStrategy = newStrategyBuilder(NugetInspectorContext.class, NugetInspectorExtractor.class)
                .needsBomTool(BomToolType.NUGET).noop()
                .needsCurrentDirectory((context, file) -> context.directory = file)
                .needsFiles(SUPPORTED_PROJECT_PATTERNS).noop()
                .demands(new NugetInspectorRequirement(), (context, file) -> context.inspectorExe = file)
                .yieldsTo(solutionStrategy)
                .build();

        return Arrays.asList(solutionStrategy, projectStrategy);

    }

}