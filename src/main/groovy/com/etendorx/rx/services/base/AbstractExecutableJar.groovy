package com.etendorx.rx.services.base

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection

import javax.annotation.Nullable

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

    AbstractExecutableJar(Project mainProject) {
        this.mainProject = mainProject
        this.fileCollectionClasspath = this.mainProject.objects.fileCollection()
    }

    void environment(Map<String, ?> environmentVariables) {
        this.environment.putAll(environmentVariables)
    }

    String getDependencyName() {
        return "${this.dependencyGroup}:${this.dependencyArtifact}:${this.dependencyVersion}"
    }

    void loadClasspathFiles() {
        if (validExecutableProject()) {
            this.fileCollectionClasspath.from(loadDefaultBuildTask().get())
        } else {
            this.mainProject.dependencies.add(this.configurationContainer.name, getDependencyName())
            this.fileCollectionClasspath.from(this.configurationContainer)
        }
    }

    boolean validExecutableProject() {
        return loadDefaultBuildTask().isPresent()
    }

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

    Optional<Task> loadDefaultBuildTask() {
        if (this.subProject != null && buildTaskName) {
            def buildTask = this.subProject.tasks.findByName(buildTaskName)
            if (buildTask) {
                return Optional.of(buildTask)
            } else {
                this.mainProject.logger.info("The ${this.subProject} is missing the '${buildTaskName}' task.")
            }
        }
        return Optional.empty()
    }

    /**
     * This method should be executed when the main project has been evaluated and the extensions are loaded
     */
    abstract void configureExtensionAction()

    String toString() {
        return """
        |* Dependency group: ${this.dependencyGroup}
        |* Dependency artifact: ${this.dependencyArtifact}
        |* Dependency version: ${this.dependencyVersion}
        |* Environment variables:
        |* ${this.environment.toString()}""".stripMargin()
    }

}
