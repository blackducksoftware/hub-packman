import java.nio.charset.StandardCharsets

import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

import com.blackducksoftware.integration.gradle.DependencyGatherer

initscript {
<#if airGapLibsPath??>
    println 'Running air gapped from ${airGapLibsPath}'
<#else>
    println 'Running in online mode with url: ${repositoryUrl}'
</#if>
    repositories {
<#if airGapLibsPath??>
        flatDir {
            dirs '${airGapLibsPath}'
        }
<#else>
        mavenLocal()
        maven {
            name 'UserDefinedRepository'
            url '${repositoryUrl}'
        }
</#if>
    }

    dependencies {
<#if airGapLibsPath??>
        new File('${airGapLibsPath}').eachFile {
            String fileName = it.name.find('.*\\.jar')?.replace('.jar', '')
            if (fileName) {
                classpath name: fileName 
            }
        }
<#else>
        classpath 'com.blackducksoftware.integration:integration-gradle-inspector:${gradleInspectorVersion}'
</#if>
    }
}

addListener(
    new TaskExecutionListener() {
        boolean executed = false;
        void beforeExecute(Task task) { }
        void afterExecute(Task task, TaskState state) {
            if (executed) {
                return
            } else {
                executed = true
            }

            String outputDirectoryPath = System.getProperty('GRADLEEXTRACTIONDIR')
            File outputDirectory = new File(outputDirectoryPath)
            outputDirectory.mkdirs()

            def dependencyGatherer = new DependencyGatherer()
            def rootProject = task.project
            dependencyGatherer.createAllDependencyGraphFiles(rootProject, '${excludedProjectNames}', '${includedProjectNames}', '${excludedConfigurationNames}', '${includedConfigurationNames}', outputDirectory)
        }
    }
)
