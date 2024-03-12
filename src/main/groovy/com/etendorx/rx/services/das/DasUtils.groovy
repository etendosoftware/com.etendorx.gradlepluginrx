package com.etendorx.rx.services.das

import com.etendorx.gradle.GradleUtils
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

class DasUtils {

    static final String CONFIGURATION_CONTAINER = 'bundle'

    static Optional<TaskProvider<? extends Task>> createCustomFatJarTask(Project mainProject, Project subProject,
                                                                         String jarName, String taskName = 'customFatJar') {
        if (subProject) {
            def task = GradleUtils.getTaskByName(mainProject, subProject, taskName)
            if (task != null) {
                return Optional.of(task)
            }

            task = subProject.tasks.register(taskName, Jar) {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                archiveBaseName = jarName

                def configurationContainer = subProject.configurations.findByName(CONFIGURATION_CONTAINER)

                if (configurationContainer != null) {
                    from {
                        configurationContainer.collect {
                            it.isDirectory() ? it : subProject.zipTree(it)
                        }
                    }
                }
                with subProject.jar
            }
            return Optional.of(task)
        }
        return Optional.empty()
    }

    static Collection<File> collectFilesFromTasks(List<Task> tasks) {
        Set files = []
        tasks.each {
            files.addAll(it.outputs.files.getFiles())
        }
        return files
    }

    static String filesToLoaderPath(Collection<File> files) {
        return files*.absolutePath.join(',')
    }

    static List<String> addDynamicDependencies(Project mainProject) {
        List<String> dynamicDependencies = [] as LinkedList

        def directories = [mainProject.getRootProject().projectDir.absolutePath]
        directories.each { dir ->
            String fileToFind = 'build.gradle'
            List<File> subDirs = findSubDirectoriesWithFile(new File(dir), fileToFind)
            subDirs.each { File subDir ->
                if (includeInDasDependencies(new File(subDir, fileToFind))) {
                    dynamicDependencies.add(':' + subDir.name)
                }
            }
        }
        return dynamicDependencies
    }

    static List<File> findSubDirectoriesWithFile(File startDir, String fileName) {
        def subDirs = []
        startDir.eachDirRecurse { File dir ->
            if (new File(dir, fileName).isFile()) {
                subDirs << dir
            }
        }
        return subDirs
    }

    static boolean includeInDasDependencies(File propertiesFile) {
        Properties properties = new Properties()
        propertiesFile.withInputStream { properties.load(it) }
        return properties.get('includeInDasDependencies')?.toBoolean() ?: false
    }
}
