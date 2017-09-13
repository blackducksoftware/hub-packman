<#macro printIfExists textContent=""><#if textContent?has_content>${textContent}</#if></#macro>

<#list detectProperties as detectProperty>
    <#if detectProperty.type??>
        <#if detectProperty.type == "Boolean">
            <#assign javaPrimitiveType="boolean">
            <#assign javaCommonPrefix="BooleanUtils.toBoolean(">
            <#assign javaCommonSuffix=")">
        <#elseif detectProperty.type == "Integer">
            <#assign javaPrimitiveType="int">
            <#assign javaCommonPrefix="convertInt(">
            <#assign javaCommonSuffix=")">
        <#elseif detectProperty.type == "Long">
            <#assign javaPrimitiveType="long">
            <#assign javaCommonPrefix="convertLong(">
            <#assign javaCommonSuffix=")">
        <#elseif detectProperty.type == "String[]">
            <#assign javaCommonPrefix="">
            <#assign javaCommonSuffix="">
            <#assign javaPrimitiveType="String[]">
        <#else>
            <#assign javaPrimitiveType="String">
            <#assign javaCommonPrefix="">
            <#assign javaCommonSuffix="?.trim()">
        </#if>
    public ${javaPrimitiveType} ${detectProperty.javaMethodName}() {
        return <@printIfExists detectProperty.javaPrefix/><@printIfExists javaCommonPrefix/>detectProperties.${detectProperty.javaName}<@printIfExists javaCommonSuffix/><@printIfExists detectProperty.javaSuffix/>
    }
    </#if>
</#list>
