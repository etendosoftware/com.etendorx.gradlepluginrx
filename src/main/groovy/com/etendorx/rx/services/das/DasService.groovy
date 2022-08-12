package com.etendorx.rx.services.das

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.rx.services.base.BaseService
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.process.JavaExecSpec

class DasService extends BaseService {

    static final String DEFAULT_PROJECT_PATH = ":com.etendorx.das"
    static final String DEFAULT_NAME = "das"
    static final String DEFAULT_PORT = "8092"
    static final String DEFAULT_GROUP = "com.etendorx"
    static final String DEFAULT_ARTIFACT = "das"
    static final String DEFAULT_VERSION = "latest.integration"
    static final String DEFAULT_CONFIG = "das"

    static final List<String> DYNAMIC_SUBPROJECTS = [
            ":com.etendorx.entities",
            ":com.etendorx.grpc.common"
    ]

    static final Action<BaseService> DEFAULT_ACTION = { BaseService service ->
        service.projectServicePath = DEFAULT_PROJECT_PATH
        service.serviceName = DEFAULT_NAME
        service.port = DEFAULT_PORT
        service.dependencyGroup = DEFAULT_GROUP
        service.dependencyArtifact = DEFAULT_ARTIFACT
        service.dependencyVersion = DEFAULT_VERSION
        service.setEnvironment([
                'SPRING_PROFILES_ACTIVE' : 'dev'
        ])
        service.subprojectsPath = DYNAMIC_SUBPROJECTS
    }

    List<Task> dynamicTasks = new ArrayList<>()

    DasService(Project mainProject) {
        super(mainProject,
                DEFAULT_ACTION,
                mainProject.extensions.findByType(EtendoRxPluginExtension).dasAction
        )
        this.configurationContainer = this.mainProject.configurations.create(DEFAULT_CONFIG)
    }

    void loadDynamicTasks() {
        this.subprojectsPath.each {
            def subProject = this.mainProject.findProject(it)
            def taskOptional = DasUtils.createCustomFatJarTask(this.mainProject,
                    subProject, "${subProject.name}-fatjar")
            if (taskOptional.isPresent()) {
                this.dynamicTasks.add(taskOptional.get().get())
            }
        }
    }

    @Override
    Optional<List<Task>> loadJarTasks() {
        this.loadDynamicTasks()
        List<Task> jarTasks = []
        jarTasks.addAll(this.dynamicTasks)

        def tasksOptional = super.loadJarTasks()
        if (tasksOptional.isPresent()) {
            jarTasks.addAll(tasksOptional.get())
        }

        return Optional.of(jarTasks)
    }

    @Override
    void loadJavaExecAction() {
        super.loadClasspathFiles()

        // Collect the dynamic JAR files
        def files = DasUtils.collectFilesFromTasks(this.dynamicTasks)
        String loaderPath = DasUtils.filesToLoaderPath(files)

        // The DAS service requires a special configuration to load dynamic JAR files using 'loader.path'
        this.javaExecAction = { JavaExecSpec spec ->
            spec.environment this.getEnvironment()
            spec.classpath this.classpathFiles
            spec.mainClass = "org.springframework.boot.loader.PropertiesLauncher"
            spec.systemProperties = [
                    "loader.path" : loaderPath
            ]
        }
    }
}
