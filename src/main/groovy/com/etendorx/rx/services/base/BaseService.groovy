package com.etendorx.rx.services.base

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskProvider
import org.gradle.process.JavaExecSpec

class BaseService extends AbstractBaseService {

    static final String BOOT_JAR_TASK = "bootJar"

    Project mainProject
    Project subProjectService
    Configuration configurationContainer

    Action<? super JavaExecSpec> javaExecAction
    Collection<File> classpathFiles

    TaskProvider bootJarTask

    BaseService(Project mainProject) {
        this.mainProject = mainProject
    }

    BaseService(Project mainProject, Action defaultAction, Action extensionAction) {
        this.mainProject = mainProject
        runActions(defaultAction, extensionAction)
        this.subProjectService = this.mainProject.findProject(this.projectServicePath)
    }

    void runActions(Action<AbstractBaseService>... actions) {
        actions.each {
            runAction(it)
        }
    }

    void runAction(Action<AbstractBaseService> action) {
        if (action) {
            action.execute(this)
        }
    }

    String getDependencyName() {
        return "${this.dependencyGroup}:${this.dependencyArtifact}:${this.dependencyVersion}"
    }

    void loadClasspathFiles() {
        if (validProjectService()) {
            this.classpathFiles = loadBootJarTask().get().outputs.files.getFiles()
        } else {
            this.mainProject.dependencies.add(this.configurationContainer.name, getDependencyName())
            this.classpathFiles = this.configurationContainer.files
        }
    }

    boolean validProjectService() {
        return loadBootJarTask().isPresent()
    }

    Optional<List<Task>> loadJarTasks() {
        if (this.subProjectService != null) {
            List<Task> subprojectJarTasks = []
            Optional<Task> bootJar = loadBootJarTask()
            if (bootJar.isPresent()) {
                subprojectJarTasks.add(bootJar.get())
            }
            return Optional.of(subprojectJarTasks)
        }
        return Optional.empty()
    }

    Optional<Task> loadBootJarTask() {
        if (this.subProjectService != null) {
            def bootJarTask = this.subProjectService.tasks.findByName(BOOT_JAR_TASK)
            if (bootJarTask) {
                return Optional.of(bootJarTask)
            } else {
                this.mainProject.logger.info("The ${this.subProjectService} is missing the '${BOOT_JAR_TASK}' task.")
            }
        }
        return Optional.empty()
    }

    void loadJavaExecAction() {
        this.loadClasspathFiles()
        this.javaExecAction = { JavaExecSpec spec ->
            spec.environment this.getEnvironment()
            spec.classpath this.classpathFiles
        }
    }
}
