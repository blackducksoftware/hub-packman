package com.blackducksoftware.integration.hub.detect

import java.lang.reflect.Type

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DetectPropertiesJsonParser {
    Set<Map<String, String>> groups = []
    List<Map<String, String>> detectProperties = []
    List<Map<String, String>> applicationProperties = []

    void parseJson(Gson gson, String json) {
        List<DetectPropertyData> detectPropertyDataList = gson.fromJson(json, new TypeToken<List<DetectPropertyData>>(){}.getType())

        detectPropertyDataList.each {
            if (it.group) {
                String groupText = it.group.toLowerCase().replaceAll('_', ' ')
                groups.add([javaName: it.group, textName: groupText])
            }

            def keyPieces = it.propertyKey.split('\\.')
            def javaName = keyPieces[1] + keyPieces[2..-1].collect {
                it[0].toUpperCase() + it[1..-1]
            }.join('')
            if (it.javaName) {
                javaName = it.javaName
            }
            def javaMethodName = "get${javaName.capitalize()}"

            def detectProperty = [key: it.propertyKey, javaName: javaName, javaMethodName: javaMethodName]
            if (it.description)
                detectProperty.put('description', it.description)
            if (it.defaultValue)
                detectProperty.put('defaultValue', it.defaultValue)
            if (it.propertyType)
                detectProperty.put('type', it.propertyType)
            if (it.group)
                detectProperty.put('group', it.group)

            detectProperties.add(detectProperty)

            def applicationProperty = [key: it.propertyKey]
            if (it.defaultValue)
                applicationProperty.put('defaultValue', it.defaultValue)

            applicationProperties.add(applicationProperty)
        }
    }
}
