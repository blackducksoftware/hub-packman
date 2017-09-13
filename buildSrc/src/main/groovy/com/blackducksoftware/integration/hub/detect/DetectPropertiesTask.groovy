package com.blackducksoftware.integration.hub.detect

import java.nio.charset.StandardCharsets

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.google.gson.Gson

import freemarker.template.Configuration;
import freemarker.template.Template;

class DetectPropertiesTask extends DefaultTask {
    @TaskAction
    def generate() {
        File projectDir = getProject().getProjectDir()

        DetectPropertiesJsonParser detectPropertiesJsonParser = new DetectPropertiesJsonParser()
        Gson gson = new Gson()
        String jsonFilePath = filePath([projectDir.canonicalPath, 'src', 'main', 'resources', 'application_properties.json'])
        String jsonProperties = new File(jsonFilePath).text
        detectPropertiesJsonParser.parseJson(gson, jsonProperties)

        String resourcesPath = filePath([projectDir.canonicalPath, 'src', 'main', 'resources'])
        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_26)
        configuration.setDirectoryForTemplateLoading(new File(resourcesPath))
        configuration.setDefaultEncoding('UTF-8')
        configuration.setLogTemplateExceptions(true)

        final Map model = new HashMap()
        model.put('groups', detectPropertiesJsonParser.groups)
        model.put('applicationProperties', detectPropertiesJsonParser.applicationProperties)
        model.put('detectProperties', detectPropertiesJsonParser.detectProperties)

        final Template applicationPropertiesTemplate = configuration.getTemplate('applicationProperties.ftl')
        final Template detectPropertiesTemplate = configuration.getTemplate('detectProperties.ftl')
        final Template detectConfigurationTemplate = configuration.getTemplate('detectConfiguration.ftl')

        updateApplicationProperties(projectDir, applicationPropertiesTemplate, model)
        updateDetectProperties(projectDir, detectPropertiesTemplate, model)
        updateDetectConfiguration(projectDir, detectConfigurationTemplate, model)
    }

    private void updateApplicationProperties(File projectDir, Template template, Map model) {
        final String applicationPropertiesPath = filePath([projectDir.canonicalPath, 'src', 'main', 'resources', 'application.properties'])
        new File(applicationPropertiesPath).withWriter('UTF-8') {
            template.process(model, it)
        }
    }

    private void updateDetectProperties(File projectDir, Template template, Map model) {
        final String detectPropertiesPath = filePath([projectDir.canonicalPath, 'src', 'main', 'groovy', 'com', 'blackducksoftware', 'integration', 'hub', 'detect', 'DetectProperties.groovy'])
        new File(detectPropertiesPath).withWriter('UTF-8') {
            template.process(model, it)
        }
    }

    private void updateDetectConfiguration(File projectDir, Template template, Map model) {
        final String generateStartMarker = '    //AUTO-GENERATE PROPERTIES START MARKER'
        final String generateEndMarker = '    //AUTO-GENERATE PROPERTIES END MARKER'
        
        final String detectPropertiesPath = filePath([projectDir.canonicalPath, 'src', 'main', 'groovy', 'com', 'blackducksoftware', 'integration', 'hub', 'detect', 'DetectConfiguration.groovy'])
        final File detectConfigurationFile = new File(detectPropertiesPath)

        final Writer writer = new StringWriter()
        template.process(model, writer)
        final String generatedText = writer.toString()

        final String detectConfigurationText = detectConfigurationFile.getText(StandardCharsets.UTF_8.toString())
        if (!detectConfigurationText.contains(generateStartMarker)) {
            throw new Exception("Missing auto-generate start marker. Not generating DetectConfiguration.groovy")
        }
        if (!detectConfigurationText.contains(generateEndMarker)) {
            throw new Exception("Missing auto-generate end marker. Not generating DetectConfiguration.groovy")
        }
        
        final int startIndex = detectConfigurationText.indexOf(generateStartMarker) + generateStartMarker.length()
        final int endIndex = detectConfigurationText.indexOf(generateEndMarker)

        String newDetectConfigurationText = detectConfigurationText.substring(0, startIndex)
        newDetectConfigurationText += generatedText
        newDetectConfigurationText += detectConfigurationText.substring(endIndex, detectConfigurationText.length())

        detectConfigurationFile.delete()
        detectConfigurationFile << newDetectConfigurationText
    }

    private String filePath(List<String> pathPieces) {
        pathPieces.join(File.separator)
    }
}