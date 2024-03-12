package com.etendorx.rx.services.base

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.process.JavaExecSpec

import java.time.LocalDate

/**
 * This class represents the BaseService for handling base service operations.
 */
class BaseService extends AbstractBaseService {

    static final String BOOT_JAR_TASK = 'bootJar'

    Action<? super JavaExecSpec> javaExecAction

    /**
     * Constructs a BaseService with the main project.
     * @param mainProject The main project
     */
    BaseService(Project mainProject) {
        super(mainProject)
    }

    /**
     * Configures the extension action.
     */
    @Override
    void configureExtensionAction() {
    }

    /**
     * Constructs a BaseService with the main project and default action.
     * @param mainProject The main project
     * @param defaultAction The default action
     */
    BaseService(Project mainProject, Action defaultAction) {
        this(mainProject)
        this.buildTaskName = BOOT_JAR_TASK
        ExecutableUtils.configureExecutable(this.mainProject, this, defaultAction)
        // Configure logs
        configureLogs()
    }

    /**
     * Configures the logs for the service.
     */
    void configureLogs() {
        File defaultLogDir = new File(this.mainProject.projectDir, 'logs')

        if (!defaultLogDir.exists() || !defaultLogDir.isDirectory()) {
            defaultLogDir = new File('/var/log')
        }

        this.environment([
                'CONSOLE_LOG_PATTERN': '',
                'LOGGING_FILE_NAME'  : "${defaultLogDir.absolutePath}${File.separator}${this.serviceName}-${LocalDate.now().toString()}.log"
        ])
    }

    /**
     * Loads the JavaExec action for the service.
     */
    void loadJavaExecAction() {
        this.loadClasspathFiles()

        // Trick to obtain the files from the 'ConfigurableFileCollection'
        // Before the javaExecAction is executed from a thread.
        // This solves the problem of a configuration not resolved from a thread not managed by Gradle.
        def classpathFiles = this.fileCollectionClasspath.getFiles()

        this.javaExecAction = { JavaExecSpec spec ->
            spec.environment this.getEnvironment()
            spec.classpath classpathFiles
            spec.jvmArgs = ['-XX:+UseSerialGC']
        }
    }
}