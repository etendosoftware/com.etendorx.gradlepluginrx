package com.etendorx.codegen

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.gradle.GradleUtils
import com.etendorx.rx.services.base.AbstractExecutableJar
import com.etendorx.rx.services.base.ExecutableUtils
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider

class CodeGenContainer extends AbstractExecutableJar {

    static final String DEFAULT_PROJECT_PATH = ":com.etendorx.generate_entities"
    static final String DEFAULT_GROUP = "com.etendorx"
    static final String DEFAULT_ARTIFACT = "generate-entities"
    static final String DEFAULT_VERSION = "latest.integration"
    static final String DEFAULT_CONFIG = "entities"

    static final String ENTITIES_TASK = "generate.entities"
    static final String TEST_ENTITIES_TASK = "generate.entities.test"
    static final String MAIN_CLASS = "com.etendorx.gen.GenerateEntitiesApplication"

    static final String GENERATE_PROPERTY = "generate"
    static final String EXCLUDED_MODULES_PROPERTY = "excludedModules"
    static final String INCLUDED_MODULES_PROPERTY = "includedModules"

    Map propertiesMap = [
            "${GENERATE_PROPERTY}"        : "-g",
            "${EXCLUDED_MODULES_PROPERTY}": "-e",
            "${INCLUDED_MODULES_PROPERTY}": "-i"
    ]

    static final Action<AbstractExecutableJar> DEFAULT_ACTION = { AbstractExecutableJar executable ->
        executable.subprojectPath = DEFAULT_PROJECT_PATH
        executable.dependencyGroup = DEFAULT_GROUP
        executable.dependencyArtifact = DEFAULT_ARTIFACT
        executable.dependencyVersion = DEFAULT_VERSION
        executable.subProject = executable.mainProject.findProject(executable.subprojectPath)
        executable.configurationContainer = executable.mainProject.configurations.create(DEFAULT_CONFIG)
    }

    TaskProvider entitiesTask

    CodeGenContainer(Project mainProject) {
        super(mainProject)
        this.buildTaskName = "build"
        ExecutableUtils.configureExecutable(this.mainProject, this, DEFAULT_ACTION)
    }

    @Override
    void configureExtensionAction() {
        GradleUtils.runAction(this, this.mainProject.extensions.findByType(EtendoRxPluginExtension).codeGenAction)
        this.subProject = this.mainProject.findProject(this.subprojectPath)
    }

    void load() {
        this.registerEntitiesTask()
        this.registerTestEntitiesTask()
    }

    @Override
    void loadClasspathFiles() {
        super.loadClasspathFiles()
        if (validExecutableProject()) {
            this.fileCollectionClasspath.from(this.subProject.sourceSets.main.runtimeClasspath)
        }
    }

    List<String> loadCommandLineParameters() {
        def commands = []
        propertiesMap.each {
            def property = mainProject.findProperty(it.key)
            if (property != null) {
                commands += ["${it.value}", property as String]
            }
        }
        return commands
    }

    void generateEntitiesTaskBuilder(taskName , List<String> extraParameters) {
        this.mainProject.tasks.register("configure${taskName}") {
            doLast {
                JavaExec task = this.mainProject.tasks.findByName(taskName) as JavaExec
                def commandLineParameters = loadCommandLineParameters()
                if(extraParameters != null) {
                    commandLineParameters.addAll(extraParameters)
                }
                project.logger.info("*** Command line parameters: ${commandLineParameters}")
                if (task) {
                    this.loadClasspathFiles()
                    task.mainClass = MAIN_CLASS
                    task.classpath = this.fileCollectionClasspath
                    task.setEnvironment(this.environment)
                    task.args += commandLineParameters
                }
            }
        }

        this.entitiesTask = this.mainProject.tasks.register(taskName, JavaExec) {
            dependsOn({
                def tasks = [this.mainProject.tasks.findByName("configure${taskName}")]
                this.configureExtensionAction()

                def buildTasks = this.loadBuildTasks()
                if (buildTasks.isPresent()) {
                    tasks.addAll(buildTasks.get())
                }
                return tasks
            })
            debugOptions {
                enabled = project.findProperty("debug") ? true : false
                port = (project.findProperty("port") ?: "5005") as Integer
                server = true
                suspend = true
            }
        }
    }

    void registerEntitiesTask() {
        generateEntitiesTaskBuilder(ENTITIES_TASK, null)
    }

    void registerTestEntitiesTask() {
        generateEntitiesTaskBuilder(TEST_ENTITIES_TASK, ["--test"])
    }

}
