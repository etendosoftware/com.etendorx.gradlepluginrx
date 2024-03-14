package com.etendorx.codegen

import org.gradle.api.tasks.JavaExec
import spock.lang.Ignore
import spock.lang.Specification
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class CodeGenContainerTest extends Specification  {
    def "load method registers entities and test entities tasks"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        CodeGenContainer codeGenContainer = new CodeGenContainer(project)

        when:
        codeGenContainer.load()

        then:
        project.tasks.findByName(CodeGenContainer.ENTITIES_TASK) != null
        project.tasks.findByName(CodeGenContainer.TEST_ENTITIES_TASK) != null
    }

    def "loadCommandLineParameters returns correct parameters"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        CodeGenContainer codeGenContainer = new CodeGenContainer(project)
        project.ext.generate = "true"
        project.ext.excludedModules = "module1,module2"
        project.ext.includedModules = "module3,module4"

        when:
        List<String> parameters = codeGenContainer.loadCommandLineParameters()

        then:
        parameters == ["-g", "true", "-e", "module1,module2", "-i", "module3,module4"]
    }

    @Ignore
    def "generateEntitiesTaskBuilder creates correct task"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        CodeGenContainer codeGenContainer = new CodeGenContainer(project)
        project.ext.generate = "true"
        project.ext.excludedModules = "module1,module2"
        project.ext.includedModules = "module3,module4"

        when:
        codeGenContainer.generateEntitiesTaskBuilder("testTask", ["--test"])

        then:
        JavaExec task = project.tasks.findByName("testTask") as JavaExec
        task != null
        task.mainClass == CodeGenContainer.MAIN_CLASS
        task.args.containsAll(["-g", "true", "-e", "module1,module2", "-i", "module3,module4", "--test"])
    }

    @Ignore
    def "registerEntitiesTask creates correct task"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        CodeGenContainer codeGenContainer = new CodeGenContainer(project)

        when:
        codeGenContainer.registerEntitiesTask()

        then:
        JavaExec task = project.tasks.findByName(CodeGenContainer.ENTITIES_TASK) as JavaExec
        task != null
        task.mainClass == CodeGenContainer.MAIN_CLASS
    }

    @Ignore
    def "registerTestEntitiesTask creates correct task"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        CodeGenContainer codeGenContainer = new CodeGenContainer(project)

        when:
        codeGenContainer.registerTestEntitiesTask()

        then:
        JavaExec task = project.tasks.findByName(CodeGenContainer.TEST_ENTITIES_TASK) as JavaExec
        task != null
        task.mainClass == CodeGenContainer.MAIN_CLASS
        task.args.contains("--test")
    }
}
