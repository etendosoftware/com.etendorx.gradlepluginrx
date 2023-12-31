package com.etendorx.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskProvider

class GradleUtils {

    static TaskProvider<Task> getTaskByName(Project mainProject, Project subProject, String taskName, LogLevel logLevel = LogLevel.DEBUG) {
        TaskProvider<Task> task = null
        try {
            task = subProject.tasks.named(taskName)
        } catch (Exception e) {
            mainProject.logger.log(logLevel, "* The task '${taskName}' from '${subProject}' could not be obtained")
            mainProject.logger.log(logLevel, "* MESSAGE: ${e.getMessage()}")
        }
        return task
    }

    static <T> void runActions(T obj, Action<T>... actions) {
        actions.each {
            runAction(obj, it)
        }
    }

    static <T> void runAction(T obj, Action<T> action) {
        if (action) {
            action.execute(obj)
        }
    }

}
