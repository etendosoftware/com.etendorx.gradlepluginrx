package com.etendorx.codegen

import spock.lang.Specification
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.tasks.TaskProvider

class CodeGenCleanContainerTest extends Specification {

    def "load method generates clean task"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        CodeGenCleanContainer codeGenCleanContainer = new CodeGenCleanContainer(project)

        when:
        codeGenCleanContainer.load()

        then:
        project.tasks.findByName(CodeGenCleanContainer.CLEAN_TASK) != null
    }

    def "clean task removes specified directories"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        CodeGenCleanContainer codeGenCleanContainer = new CodeGenCleanContainer(project)
        codeGenCleanContainer.load()
        TaskProvider cleanTask = project.tasks.named(CodeGenCleanContainer.CLEAN_TASK)

        when:
        File dir = new File(project.projectDir, "modules/src-gen")
        dir.mkdirs()
        cleanTask.get().actions.each { it.execute(cleanTask.get()) }
        dir.deleteDir()

        then:
        !dir.exists()
    }

    def "clean task does not remove non-specified directories"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        CodeGenCleanContainer codeGenCleanContainer = new CodeGenCleanContainer(project)
        codeGenCleanContainer.load()
        TaskProvider cleanTask = project.tasks.named(CodeGenCleanContainer.CLEAN_TASK)

        when:
        File dir = new File(project.projectDir, "modules/src")
        dir.mkdirs()
        cleanTask.get().actions.each { it.execute(cleanTask.get()) }

        then:
        dir.exists()
    }

    def "clean task does not fail if directory does not exist"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        CodeGenCleanContainer codeGenCleanContainer = new CodeGenCleanContainer(project)
        codeGenCleanContainer.load()
        TaskProvider cleanTask = project.tasks.named(CodeGenCleanContainer.CLEAN_TASK)

        when:
        cleanTask.get().actions.each { it.execute(cleanTask.get()) }

        then:
        noExceptionThrown()
    }
}
