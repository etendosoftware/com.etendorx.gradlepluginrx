package com.etendorx.codegen

import groovy.io.FileType
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

@CompileStatic
class CodeGenCleanContainer {

    final static String CLEAN_TASK = 'clean.generated.code'
    final static String SRC_GEN = 'src-gen'
    final static String SRC = 'src'

    final static List DIRECTORIES_TO_CLEAN = [
            [dir: 'modules', folder: SRC_GEN],
            [dir: 'modules_gen', folder: SRC],
            [dir: 'modules_core', folder: SRC_GEN],
    ]

    Project mainProject
    TaskProvider cleanTask

    CodeGenCleanContainer(Project mainProject) {
        this.mainProject = mainProject
    }

    void load() {
        this.generateCleanTask()
    }

    void generateCleanTask() {
        this.cleanTask = this.mainProject.tasks.register(CLEAN_TASK) {
            doLast {
                DIRECTORIES_TO_CLEAN.each { dirInfo ->
                    File dir = project.file(dirInfo.dir)
                    if (dir && dir.exists() && dir.isDirectory()) {
                        cleanDirectory(project, dir, dirInfo.folder)
                    }
                }
            }
        }
    }

    @CompileStatic
    static void cleanDirectory(Project project, File modulesLocation, String dirToClean) {
        modulesLocation.traverse(type: FileType.DIRECTORIES, maxDepth: 0) { file ->
            File dir = new File(file, dirToClean)
            if (dir.exists() && dir.isDirectory()) {
                project.logger.info("Cleaning directory: ${dir.getAbsolutePath()}")
                project.delete(dir.listFiles())
            }
        }
    }

}
