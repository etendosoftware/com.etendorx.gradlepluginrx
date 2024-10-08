package com.etendorx.rx.services.das

import com.etendorx.gradle.GradleUtils
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

/**
 * Utility class for handling custom tasks in Gradle projects
 */
class DasUtils {

    static final String CONFIGURATION_CONTAINER = 'bundle'

    /**
     * Creates a custom fat jar task for the given subProject
     * @param mainProject The main project
     * @param subProject The sub project
     * @param jarName The name of the jar
     * @param taskName The name of the task
     * @return Optional<TaskProvider<? extends Task>> The task provider
     */
    static Optional<TaskProvider<? extends Task>> createCustomFatJarTask(Project mainProject, Project subProject,
                                                                         String jarName, String taskName = 'customFatJar') {
        if (subProject) {
            TaskProvider<? extends Task> task = GradleUtils.getTaskByName(mainProject, subProject, taskName)
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

    /**
     * Collects files from the output of tasks
     * @param tasks The list of tasks
     * @return Collection<File> The collection of files
     */
    static Collection<File> collectFilesFromTasks(List<Task> tasks) {
        Set<File> files = []
        tasks.each { Task task ->
            files.addAll(task.outputs.files.files)
        }
        return files
    }

    /**
     * Converts a collection of files to a loader path string
     * @param files The collection of files
     * @return String The loader path string
     */
    static String filesToLoaderPath(Collection<File> files) {
        return files*.absolutePath.join(',')
    }

    /**
     * Adds dynamic dependencies based on build.gradle files in subdirectories
     * @param mainProject The main project
     * @return List<String> The list of dynamic dependencies
     */
    static List<String> addDynamicDependencies(Project mainProject) {
        List<String> dynamicDependencies = [] as LinkedList

        List<String> directories = [mainProject.getRootProject().projectDir.absolutePath]
        directories.each { String dir ->
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

    /**
     * Finds subdirectories containing a specific file
     * @param startDir The starting directory
     * @param fileName The name of the file
     * @return List<File> The list of subdirectories
     */
    static List<File> findSubDirectoriesWithFile(File startDir, String fileName) {
        List<File> subDirs = []
        startDir.eachDirRecurse { File dir ->
            if (new File(dir, fileName).isFile()) {
                subDirs << dir
            }
        }
        return subDirs
    }

    /**
     * Checks if a properties file includes a specific dependency
     * @param propertiesFile The properties file
     * @return boolean True if the dependency is included, false otherwise
     */
    static boolean includeInDasDependencies(File propertiesFile) {
        Properties properties = new Properties()
        propertiesFile.withInputStream { InputStream inputStream ->
            properties.load(inputStream)
        }
        return properties.get('includeInDasDependencies')?.toBoolean() ?: false
    }
}