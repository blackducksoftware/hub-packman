package com.blackducksoftware.integration.hub.detect.bomtool.prerequisite;

import org.apache.commons.lang3.StringUtils;

public class PropertyValueSetPrerequisite extends Prerequisite {
    private final String propertyKey;
    private final String propertyValue;

    public PropertyValueSetPrerequisite(final String propertyKey, final String propertyValue) {
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    @Override
    public boolean isMet() {
        return StringUtils.isNotBlank(propertyValue);
    }

    @Override
    public String failureMessage() {
        return String.format("%s is not set.", propertyKey);
    }

}
