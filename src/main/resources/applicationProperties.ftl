# This file is generated from the src/main/resources/applicationProperties.ftl file.
# Any necessary changes should be made there and then this file should be generated from the template.

<#list applicationProperties as applicationProperty>
    <#if applicationProperty.defaultValue??>
${applicationProperty.key}=${applicationProperty.defaultValue}
    <#else>
${applicationProperty.key}=
    </#if>
</#list>
