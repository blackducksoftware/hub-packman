package com.blackducksoftware.integration.hub.detect.extraction.bomtool.go.strategy;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.detect.extraction.StandardExecutableFinder;
import com.blackducksoftware.integration.hub.detect.extraction.StandardExecutableFinder.StandardExecutableType;
import com.blackducksoftware.integration.hub.detect.extraction.bomtool.go.GoDepContext;
import com.blackducksoftware.integration.hub.detect.extraction.bomtool.go.GoDepExtractor;
import com.blackducksoftware.integration.hub.detect.extraction.bomtool.go.GoInspectorManager;
import com.blackducksoftware.integration.hub.detect.extraction.requirement.evaluation.StrategyEnvironment;
import com.blackducksoftware.integration.hub.detect.extraction.result.ExecutableNotFoundStrategyResult;
import com.blackducksoftware.integration.hub.detect.extraction.result.FileNotFoundStrategyResult;
import com.blackducksoftware.integration.hub.detect.extraction.result.InspectorNotFoundStrategyResult;
import com.blackducksoftware.integration.hub.detect.extraction.result.PassedStrategyResult;
import com.blackducksoftware.integration.hub.detect.extraction.result.StrategyResult;
import com.blackducksoftware.integration.hub.detect.extraction.strategy.Strategy;
import com.blackducksoftware.integration.hub.detect.model.BomToolType;
import com.blackducksoftware.integration.hub.detect.util.DetectFileFinder;

@Component
public class GoCliStrategy extends Strategy<GoDepContext, GoDepExtractor> {
    public static final String GOFILE_FILENAME_PATTERN = "*.go";

    @Autowired
    public DetectFileFinder fileFinder;

    @Autowired
    public GoInspectorManager goInspectorManager;

    @Autowired
    public StandardExecutableFinder standardExecutableFinder;


    public GoCliStrategy() {
        super("Go Cli", BomToolType.GO_DEP, GoDepContext.class, GoDepExtractor.class);
    }

    @Override
    public StrategyResult applicable(final StrategyEnvironment environment, final GoDepContext context) {
        final List<File> found = fileFinder.findFiles(environment.getDirectory(), GOFILE_FILENAME_PATTERN);
        if (found == null || found.size() == 0) {
            return new FileNotFoundStrategyResult(GOFILE_FILENAME_PATTERN);
        }

        return new PassedStrategyResult();
    }

    @Override
    public StrategyResult extractable(final StrategyEnvironment environment, final GoDepContext context){
        context.goExe = standardExecutableFinder.getExecutable(StandardExecutableType.GO);
        if (context.goExe == null) {
            return new ExecutableNotFoundStrategyResult("go");
        }

        context.goDepInspector = goInspectorManager.evaluate(environment);
        if (context.goDepInspector == null) {
            return new InspectorNotFoundStrategyResult("go");
        }

        return new PassedStrategyResult();
    }

}