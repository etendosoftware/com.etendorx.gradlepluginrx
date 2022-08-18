package com.etendorx.codegen

import org.gradle.api.Project

class CodeGenLoader {

    static void load (Project project) {
        new CodeGenCleanContainer(project).load()
        new CodeGenContainer(project).load()

        project.tasks.getByName(CodeGenContainer.ENTITIES_TASK).dependsOn({
            project.tasks.findByName(CodeGenCleanContainer.CLEAN_TASK)
        })
    }

}
