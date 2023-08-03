package com.etendorx.rx.services.base

import com.etendorx.gradle.GradleUtils
import org.gradle.api.Action
import org.gradle.api.Project

class ExecutableUtils {

    static void configureExecutable(Project mainProject, AbstractExecutableJar executableJar, Action defaultAction) {
        // Configure the object with the default action
        GradleUtils.runAction(executableJar, defaultAction)

        // Show default values
        mainProject.logger.info("***** DEFAULT VALUES ***** ${executableJar.toString()}")
    }

}
