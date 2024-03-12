package com.etendorx.rx.services.base

import com.etendorx.gradle.GradleUtils
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project

@CompileStatic
class ExecutableUtils {

    static void configureExecutable(Project mainProject, AbstractExecutableJar executableJar, Action defaultAction) {
        // Configure the object with the default action
        GradleUtils.runAction(executableJar, defaultAction)

        // Show default values
        mainProject.logger.info("***** DEFAULT VALUES ***** ${executableJar.toString()}")
    }

}
