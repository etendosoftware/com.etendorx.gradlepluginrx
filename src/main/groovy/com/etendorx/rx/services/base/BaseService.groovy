package com.etendorx.rx.services.base

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.process.JavaExecSpec

class BaseService extends AbstractBaseService {
    static final String BOOT_JAR_TASK = "bootJar"

    Action<? super JavaExecSpec> javaExecAction

    BaseService(Project mainProject) {
        super(mainProject)
    }

    BaseService(Project mainProject, Action defaultAction) {
        this(mainProject)
        this.buildTaskName = BOOT_JAR_TASK
        ExecutableUtils.configureExecutable(this.mainProject, this, defaultAction)
    }

    void loadJavaExecAction() {
        this.loadClasspathFiles()

        // Trick to obtain the files from the 'ConfigurableFileCollection'
        // Before the javaExecAcion is executed from a thread.
        // This solves the problem of a configuration not resolved from a thread not managed by Gradle.
        def classpathFiles = this.fileCollectionClasspath.getFiles()

        this.javaExecAction = { JavaExecSpec spec ->
            spec.environment this.getEnvironment()
            spec.classpath classpathFiles
        }
    }
}
