package com.etendorx.codegen

import org.gradle.api.Project

/**
 * This class is responsible for loading the code generation process.
 */
class CodeGenLoader {

    /**
     * Loads the code generation process for the given project.
     *
     * @param project The Gradle project to load the code generation for.
     */
    static void load(Project project) {
        new CodeGenCleanContainer(project).load()
        new CodeGenContainer(project).load()

        // Fixed issue AY4ycF5-KWRqVdAoGwaO by moving the closure outside the parenthesis
        project.tasks.getByName(CodeGenContainer.ENTITIES_TASK).dependsOn {
            return [
                    ':' + CodeGenContainer.DEFAULT_GROUP + '.' + CodeGenContainer.DEFAULT_CONFIG + ':clean',
                    project.tasks.findByName(CodeGenCleanContainer.CLEAN_TASK),
            ]
        }
        // Fixed issue AY4ycF5-KWRqVdAoGwaP by moving the closure outside the parenthesis
        project.tasks.getByName(CodeGenContainer.TEST_ENTITIES_TASK).dependsOn {
            project.tasks.findByName(CodeGenCleanContainer.CLEAN_TASK)
        }
    }

}