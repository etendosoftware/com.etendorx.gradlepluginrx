package com.etendorx.codegen

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider

import javax.annotation.Nullable

class CodeGenContainer {

    static final String ENTITIES_CONFIGURATION = "entities"
    static final String ENTITIES_PROJECT = "com.etendorx.generate_entities"
    static final String ENTITIES_TASK = "generate.entities"
    static final String MAIN_CLASS = "com.etendorx.gen.GenerateEntitiesApplication"

    Project mainProject
    Configuration entitiesConfiguration
    @Nullable
    Project entitiesProject
    TaskProvider entitiesTask

    String entityDependencyGroup = "com.etendorx"
    String entityDependencyArtifact = "generate-entities"
    String entityDependencyVersion = "latest.integration"

    CodeGenContainer(Project mainProject) {
        this.mainProject = mainProject
    }

    void load() {
        this.entitiesProject = this.mainProject.findProject(ENTITIES_PROJECT)
        this.entitiesConfiguration = this.mainProject.configurations.create(ENTITIES_CONFIGURATION)
        // Add the 'generate-entities' dependency to the configuration
        this.mainProject.dependencies.add(this.entitiesConfiguration.name, this.getEntitiesDependencyName())
        this.configureDependencySubstitution()
        this.registerEntitiesTask()
    }

    String getEntitiesDependencyName() {
        return "${this.entityDependencyGroup}:${this.entityDependencyArtifact}:${this.entityDependencyVersion}"
    }

    void configureDependencySubstitution() {
        this.entitiesConfiguration.resolutionStrategy.dependencySubstitution.all({dependency ->
            if (dependency.requested instanceof ModuleComponentSelector
                    && (dependency.requested as ModuleComponentSelector).module == "generate-entities"){
                if (this.entitiesProject != null) {
                    dependency.useTarget(this.entitiesProject)
                }
            }
        })
    }

    void registerEntitiesTask() {
        this.entitiesTask = this.mainProject.tasks.register(ENTITIES_TASK, JavaExec) {
            classpath = this.entitiesConfiguration
            mainClass = MAIN_CLASS
            debugOptions {
                enabled = project.findProperty("debug") ? true : false
                port = (project.findProperty("port") ?: "5005") as Integer
                server = true
                suspend = true
            }
        }
    }

}
