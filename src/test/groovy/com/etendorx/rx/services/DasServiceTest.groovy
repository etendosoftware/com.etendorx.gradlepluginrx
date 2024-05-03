package com.etendorx.rx.services

import com.etendorx.rx.services.das.DasService
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

class DasServiceTest extends Specification {

    @Ignore
    def "loadDynamicTasks adds tasks for each subproject"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        DasService dasService = new DasService(project)
        dasService.subprojectsPath = [":nonexistent"]
        mainProject.findProject(_ as String) >> subProject

        when:
        dasService.loadDynamicTasks()

        then:
        dasService.dynamicTasks.size() == DasService.DYNAMIC_SUBPROJECTS.size()
    }

    @Ignore
    def "loadBuildTasks includes dynamic and super tasks"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        DasService dasService = new DasService(project)
        dasService.loadDynamicTasks()

        when:
        def tasksOptional = dasService.loadBuildTasks()

        then:
        tasksOptional.isPresent()
        tasksOptional.get().size() == dasService.dynamicTasks.size() + 1 // +1 for the super task
    }

    @Ignore
    def "loadJavaExecAction sets correct environment and classpath"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        DasService dasService = new DasService(project)
        dasService.loadDynamicTasks()

        when:
        dasService.loadJavaExecAction()

        then:
        dasService.javaExecAction != null
    }

    def "loadDynamicTasks handles null subproject"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        DasService dasService = new DasService(project)
        dasService.subprojectsPath = [":nonexistent"]

        when:
        dasService.loadDynamicTasks()

        then:
        dasService.dynamicTasks.isEmpty()
    }
}