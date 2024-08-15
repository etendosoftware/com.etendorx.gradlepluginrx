package com.etendorx.codegen

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.gradle.GradleUtils
import com.etendorx.rx.services.base.AbstractExecutableJar
import com.etendorx.rx.services.base.ExecutableUtils
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider

/**
 * Represents a container for code generation tasks.
 */
class CodeGenContainer extends AbstractExecutableJar {

    /**
     * Default project path for code generation.
     */
    static final String DEFAULT_PROJECT_PATH = ':com.etendorx.generate_entities'
    static final String DEFAULT_GROUP = 'com.etendorx'
    static final String DEFAULT_ARTIFACT = 'generate-entities'
    static final String DEFAULT_VERSION = 'latest.integration'
    static final String DEFAULT_CONFIG = 'entities'

    // Constants for tasks and classes
    static final String ENTITIES_TASK = 'generate.entities'
    static final String TEST_ENTITIES_TASK = 'generate.entities.test'
    static final String MAIN_CLASS = 'com.etendorx.gen.GenerateEntitiesApplication'
    static final String MAIN_CLASS_JAR = 'org.springframework.boot.loader.JarLauncher'

    // Properties for command line parameters
    static final String GENERATE_PROPERTY = 'generate'
    static final String EXCLUDED_MODULES_PROPERTY = 'excludedModules'
    static final String INCLUDED_MODULES_PROPERTY = 'includedModules'

    static String version;

    // Map of properties for command line arguments
    Map propertiesMap = [
            GENERATE_PROPERTY        : '-g',
            EXCLUDED_MODULES_PROPERTY: '-e',
            INCLUDED_MODULES_PROPERTY: '-i'
    ]

    // Default action for executable
    static final Action<AbstractExecutableJar> DEFAULT_ACTION = { AbstractExecutableJar executable ->
        executable.subprojectPath = DEFAULT_PROJECT_PATH
        executable.dependencyGroup = DEFAULT_GROUP
        executable.dependencyArtifact = DEFAULT_ARTIFACT
        version = executable.mainProject.findProperty('rx.version')
        if (version == null) {
            version = DEFAULT_VERSION
        }
        executable.dependencyVersion = version
        executable.subProject = executable.mainProject.findProject(executable.subprojectPath)
        executable.configurationContainer = executable.mainProject.configurations.create(DEFAULT_CONFIG)
    }

    TaskProvider entitiesTask

    /**
     * Constructor for CodeGenContainer.
     * @param mainProject The main project.
     */
    CodeGenContainer(Project mainProject) {
        super(mainProject)
        this.buildTaskName = 'build'
        // Moved configuration to a separate method to avoid exposing a half-baked object
        configureExecutable()
    }

    /**
     * Configures the executable.
     */
    void configureExecutable() {
        ExecutableUtils.configureExecutable(this.mainProject, this, DEFAULT_ACTION)
    }

    @Override
    void configureExtensionAction() {
        GradleUtils.runAction(this, this.mainProject.extensions.findByType(EtendoRxPluginExtension).codeGenAction)
        this.subProject = this.mainProject.findProject(this.subprojectPath)
    }

    /**
     * Loads the entities.
     */
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

    /**
     * Loads command line parameters.
     * @return List of command line parameters.
     */
    List<String> loadCommandLineParameters() {
        List<String> commands = []
        propertiesMap.each { Map.Entry<String, String> entry ->
            String property = mainProject.findProperty(entry.key) as String
            if (property != null) {
                commands += [entry.value, property]
            }
        }
        return commands
    }

    /**
     * Builds the entities task.
     * @param taskName The name of the task.
     * @param extraParameters Extra parameters for the task.
     */
    void generateEntitiesTaskBuilder(String taskName, List<String> extraParameters) {
        this.mainProject.tasks.register("configure${taskName}") {
            doLast {
                JavaExec task = this.mainProject.tasks.findByName(taskName) as JavaExec
                List<String> commandLineParameters = loadCommandLineParameters()
                if (extraParameters != null) {
                    commandLineParameters.addAll(extraParameters)
                }
                project.logger.info("*** Command line parameters: ${commandLineParameters}")
                if (task) {
                    this.loadClasspathFiles()
                    if (version.startsWith('2.')) {
                        task.mainClass = MAIN_CLASS_JAR
                    } else {
                        task.mainClass = MAIN_CLASS
                    }
                    task.classpath = this.fileCollectionClasspath
                    task.environment = this.environment
                    task.args += commandLineParameters
                }
            }
        }

        this.entitiesTask = this.mainProject.tasks.register(taskName, JavaExec) {
            dependsOn {
                List tasks = [this.mainProject.tasks.findByName("configure${taskName}")]
                this.configureExtensionAction()

                Optional<List> buildTasks = this.loadBuildTasks()
                if (buildTasks.isPresent()) {
                    tasks.addAll(buildTasks.get())
                }
                tasks
            }
            debugOptions {
                enabled = project.findProperty('debug') ? true : false
                port = (project.findProperty('port') ?: '5005') as Integer
                server = true
                suspend = true
            }
        }
    }

    /**
     * Registers the entities task.
     */
    void registerEntitiesTask() {
        generateEntitiesTaskBuilder(ENTITIES_TASK, null)
    }

    /**
     * Registers the test entities task.
     */
    void registerTestEntitiesTask() {
        generateEntitiesTaskBuilder(TEST_ENTITIES_TASK, ['--test'])
    }
}
