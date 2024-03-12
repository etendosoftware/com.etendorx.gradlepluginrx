package com.etendorx.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskProvider

/**
 * Utility class for Gradle operations
 */
class GradleUtils {

    /**
     * Get a task by name from a subproject
     * @param mainProject the main project
     * @param subProject the subproject
     * @param taskName the name of the task
     * @param logLevel the log level
     * @return the TaskProvider for the task
     */
    static TaskProvider<Task> getTaskByName(Project mainProject, Project subProject, String taskName, LogLevel logLevel = LogLevel.DEBUG) {
        TaskProvider<Task> task = null
        try {
            task = subProject.tasks.named(taskName)
        } catch (UnknownTaskException e) {
            mainProject.logger.log(logLevel, "* The task '${taskName}' from '${subProject}' could not be obtained")
            mainProject.logger.log(logLevel, "* MESSAGE: ${e.getMessage()}")
        }
        return task
    }

    /**
     * Run a list of actions on an object
     * @param obj the object
     * @param actions the list of actions
     */
    static <T> void runActions(T obj, Action<T>... actions) {
        actions.each { action ->
            runAction(obj, action)
        }
    }

    /**
     * Run a single action on an object
     * @param obj the object
     * @param action the action to run
     */
    static <T> void runAction(T obj, Action<T> action) {
        if (action) {
            action.execute(obj)
        }
    }

}
