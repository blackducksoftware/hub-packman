package com.blackducksoftware.integration.hub.detect.bomtool.prerequisite;

public class BooleanPrerequisite extends Prerequisite {
    private final boolean condition;

    private final String failureMessage;

    public BooleanPrerequisite(final boolean condition, final String failureMessage) {
        this.condition = condition;
        this.failureMessage = failureMessage;
    }

    @Override
    public boolean isMet() {
        return condition;
    }

    @Override
    public String failureMessage() {
        return failureMessage;
    }

}
