package com.blackducksoftware.integration.hub.detect.bomtool.prerequisite;

public abstract class Prerequisite {
    private boolean executablePrerequisite;

    public abstract boolean isMet();

    public abstract String failureMessage();

    public void setExecutablePrerequisite(final boolean executablePrerequisite) {
        this.executablePrerequisite = executablePrerequisite;
    }

    public boolean isExecutablePrerequisite() {
        return executablePrerequisite;
    }

}
