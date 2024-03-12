package com.etendorx.codegen

import groovy.io.FileType
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/**
 * A container class for cleaning generated code directories.
 */
@CompileStatic
class CodeGenCleanContainer {

    /**
     * The task name for cleaning generated code.
     */
    final static String CLEAN_TASK = 'clean.generated.code'
    final static String SRC_GEN = 'src-gen'
    final static String SRC = 'src'

    /**
     * Directories to be cleaned.
     */
    final static List DIRECTORIES_TO_CLEAN = [
            [dir: 'modules', folder: SRC_GEN],
            [dir: 'modules_gen', folder: SRC],
            [dir: 'modules_core', folder: SRC_GEN],
    ]

    Project mainProject
    TaskProvider cleanTask

    /**
     * Constructor for CodeGenCleanContainer.
     * @param mainProject The main project.
     */
    CodeGenCleanContainer(Project mainProject) {
        this.mainProject = mainProject
    }

    /**
     * Load method to generate clean task.
     */
    void load() {
        this.generateCleanTask()
    }

    /**
     * Method to generate the clean task.
     */
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

    /**
     * Static method to clean a directory.
     * @param project The project.
     * @param modulesLocation The location of modules.
     * @param dirToClean The directory to clean.
     */
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