package com.blackducksoftware.integration.hub.detect.properties;

public class DetectPropertyData {
    private String propertyKey;
    private String detectPropertyName;
    private String detectConfigurationName;
    private String description;
    private String defaultValue;
    private String propertyType;
    private String group;
    private String javaCodePrefix;
    private String javaCodeSuffix;
    private int propertyOrder;
    private int configurationOrder;

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(final String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getDetectPropertyName() {
        return detectPropertyName;
    }

    public void setDetectPropertyName(final String detectPropertyName) {
        this.detectPropertyName = detectPropertyName;
    }

    public String getDetectConfigurationName() {
        return detectConfigurationName;
    }

    public void setDetectConfigurationName(final String detectConfigurationName) {
        this.detectConfigurationName = detectConfigurationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(final String propertyType) {
        this.propertyType = propertyType;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public String getJavaCodePrefix() {
        return javaCodePrefix;
    }

    public void setJavaCodePrefix(final String javaCodePrefix) {
        this.javaCodePrefix = javaCodePrefix;
    }

    public String getJavaCodeSuffix() {
        return javaCodeSuffix;
    }

    public void setJavaCodeSuffix(final String javaCodeSuffix) {
        this.javaCodeSuffix = javaCodeSuffix;
    }

    public int getPropertyOrder() {
        return propertyOrder;
    }

    public void setPropertyOrder(final int propertyOrder) {
        this.propertyOrder = propertyOrder;
    }

    public int getConfigurationOrder() {
        return configurationOrder;
    }

    public void setConfigurationOrder(final int configurationOrder) {
        this.configurationOrder = configurationOrder;
    }

}
