package com.blackducksoftware.integration.hub.detect.bomtool.prerequisite;

import com.blackducksoftware.integration.hub.detect.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager;

public class SourceFileExistsPrerequisite extends Prerequisite {
    private final DetectFileManager detectFileManager;

    private final DetectConfiguration detectConfiguration;

    private final String filePath;

    public SourceFileExistsPrerequisite(final DetectFileManager detectFileManager, final DetectConfiguration detectConfiguration, final String filePath) {
        this.detectFileManager = detectFileManager;
        this.detectConfiguration = detectConfiguration;
        this.filePath = filePath;
    }

    @Override
    public boolean isMet() {
        return detectFileManager.containsAllFiles(detectConfiguration.getSourcePath(), filePath);
    }

    @Override
    public String failureMessage() {
        return String.format("Could not find the source file %s in %s.", filePath, detectConfiguration.getSourcePath());
    }

    public DetectFileManager getDetectFileManager() {
        return detectFileManager;
    }

    public DetectConfiguration getDetectConfiguration() {
        return detectConfiguration;
    }

    public String getFilePath() {
        return filePath;
    }
}
