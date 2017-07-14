package com.blackducksoftware.integration.hub.detect.bomtool.prerequisite;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.detect.type.ExecutableType;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableManager;

public class ExecutableExistsPrerequisite extends Prerequisite {
    private final ExecutableManager executableManager;
    private final ExecutableType executableType;
    private final String executablePathOverride;

    public ExecutableExistsPrerequisite(final ExecutableManager executableManager, final ExecutableType executableType, final String executablePathOverride) {
        this.executableManager = executableManager;
        this.executableType = executableType;
        this.executablePathOverride = executablePathOverride;
        this.setExecutablePrerequisite(true);
    }

    @Override
    public boolean isMet() {
        final String executablePath = executableManager.getPathOfExecutable(executableType, executablePathOverride);
        return StringUtils.isNotEmpty(executablePath);
    }

    @Override
    public String failureMessage() {
        return String.format("Could not find executable for %s", executableType.toString());
    }

}
