package com.etendorx.codegen

import groovy.io.FileType
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class CodeGenCleanContainer {

    final static String CLEAN_TASK = "clean.generated.code"

    Project mainProject
    TaskProvider cleanTask

    final static List DIRECTORIES_TO_CLEAN = [
            [dir: "modules", folder: "src-gen"],
            [dir: "modules_gen", folder: "src"],
            [dir: "modules_core", folder: "src-gen"]
    ]

    CodeGenCleanContainer(Project mainProject) {
        this.mainProject = mainProject
    }

    void load() {
        this.generateCleanTask()
    }

    void generateCleanTask() {
        this.cleanTask = this.mainProject.tasks.register(CLEAN_TASK) {
            doLast {
                DIRECTORIES_TO_CLEAN.forEach({
                    File dir = project.file(it.dir)
                    if (dir && dir.exists() && dir.isDirectory()) {
                        cleanDirectory(project, dir, it.folder)
                    }
                })
            }
        }
    }

    static void cleanDirectory(Project project, File modulesLocation, String dirToClean) {
        modulesLocation.traverse(type: FileType.DIRECTORIES, maxDepth: 0) {
            File dir = new File(it, dirToClean)
            if (dir.exists() && dir.isDirectory()) {
                project.logger.info("Cleaning directory: ${dir.getAbsolutePath()}")
                project.delete(dir.listFiles())
            }
        }
    }

}
