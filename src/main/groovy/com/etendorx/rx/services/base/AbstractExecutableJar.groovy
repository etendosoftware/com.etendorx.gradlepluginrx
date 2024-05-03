package com.etendorx.rx.services.base

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection

import javax.annotation.Nullable

/**
 * Abstract class representing an executable JAR
 */
abstract class AbstractExecutableJar {

    Project mainProject

    /**
     * The subproject path notation
     */
    String subprojectPath

    /**
     * The subproject object {@link Project}
     */
    @Nullable
    Project subProject

    /**
     * The group of the dependency to be resolved
     */
    String dependencyGroup

    /**
     * The artifact name of the dependency to ve resolved
     */
    String dependencyArtifact

    /**
     * The version of the dependency to be resolved
     */
    String dependencyVersion

    ConfigurableFileCollection fileCollectionClasspath

    Configuration configurationContainer

    /**
     * The name of the task contained in the subproject which
     * builds the files used for the executable classpath
     */
    String buildTaskName

    Map<String, ?> environment = new HashMap<>()

    /**
     * Constructor for AbstractExecutableJar
     * @param mainProject The main project
     */
    AbstractExecutableJar(Project mainProject) {
        this.mainProject = mainProject
        this.fileCollectionClasspath = this.mainProject.objects.fileCollection()
    }

    /**
     * Set environment variables
     * @param environmentVariables The environment variables to set
     */
    void environment(Map<String, ?> environmentVariables) {
        this.environment.putAll(environmentVariables)
    }

    /**
     * Get the name of the dependency
     * @return The dependency name
     */
    String getDependencyName() {
        return "${this.dependencyGroup}:${this.dependencyArtifact}:${this.dependencyVersion}"
    }

    /**
     * Load classpath files based on project type
     */
    void loadClasspathFiles() {
        if (validExecutableProject()) {
            this.fileCollectionClasspath.from(loadDefaultBuildTask().get())
        } else {
            this.mainProject.dependencies.add(this.configurationContainer.name, getDependencyName())
            this.fileCollectionClasspath.from(this.configurationContainer)
        }
    }

    /**
     * Check if the project is a valid executable project
     * @return True if valid, false otherwise
     */
    boolean validExecutableProject() {
        return loadDefaultBuildTask().isPresent()
    }

    /**
     * Load build tasks for the subproject
     * @return List of build tasks
     */
    Optional<List<Task>> loadBuildTasks() {
        if (this.subProject != null) {
            List<Task> subprojectJarTasks = []
            Optional<Task> bootJar = loadDefaultBuildTask()
            if (bootJar.isPresent()) {
                subprojectJarTasks.add(bootJar.get())
            }
            return Optional.of(subprojectJarTasks)
        }
        return Optional.empty()
    }

    /**
     * Load the default build task for the subproject
     * @return The default build task
     */
    Optional<Task> loadDefaultBuildTask() {
        if (this.subProject != null && buildTaskName) {
            Task buildTask = this.subProject.tasks.findByName(buildTaskName)
            if (buildTask != null) {
                return Optional.of(buildTask)
            } else {
                this.mainProject.logger.info("The ${this.subProject} is missing the '${buildTaskName}' task.")
            }
        }
        return Optional.empty()
    }

    /**
     * Configure extension action for the main project
     */
    abstract void configureExtensionAction()

    /**
     * Get a string representation of the object
     * @return String representation
     */
    String toString() {
        return """
        |* Dependency group: ${this.dependencyGroup}
        |* Dependency artifact: ${this.dependencyArtifact}
        |* Dependency version: ${this.dependencyVersion}
        |* Environment variables:
        |* ${this.environment.toString()}""".stripMargin()
    }

}