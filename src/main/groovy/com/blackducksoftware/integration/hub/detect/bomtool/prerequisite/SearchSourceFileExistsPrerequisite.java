package com.blackducksoftware.integration.hub.detect.bomtool.prerequisite;

import com.blackducksoftware.integration.hub.detect.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager;

public class SearchSourceFileExistsPrerequisite extends SourceFileExistsPrerequisite {

    public SearchSourceFileExistsPrerequisite(final DetectFileManager detectFileManager, final DetectConfiguration detectConfiguration, final String filePath) {
        super(detectFileManager, detectConfiguration, filePath);
    }

    @Override
    public boolean isMet() {
        return getDetectFileManager().containsAllFilesToDepth(getDetectConfiguration().getSourcePath(), getDetectConfiguration().getSearchDepth(),
                getFilePath());
    }

}
