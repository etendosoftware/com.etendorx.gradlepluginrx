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
    static final String MAIN_CLASS = "com.etendorx.gen.GenerateEntitiesApplication"

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
    }

    @Override
    void loadClasspathFiles() {
        super.loadClasspathFiles()
        if (validExecutableProject()) {
            this.fileCollectionClasspath.from(this.subProject.sourceSets.main.runtimeClasspath)
        }
    }

    void registerEntitiesTask() {
        this.mainProject.tasks.register("configure${ENTITIES_TASK}") {
            doLast {
                JavaExec task = this.mainProject.tasks.findByName(ENTITIES_TASK) as JavaExec
                if (task) {
                    this.loadClasspathFiles()
                    task.mainClass = MAIN_CLASS
                    task.classpath = this.fileCollectionClasspath
                    task.setEnvironment(this.environment)
                }
            }
        }

        this.entitiesTask = this.mainProject.tasks.register(ENTITIES_TASK, JavaExec) {
            dependsOn({
                def tasks = [this.mainProject.tasks.findByName("configure${ENTITIES_TASK}")]
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

}
