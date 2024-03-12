package com.etendorx.rx.services.base

import com.etendorx.gradle.GradleUtils
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project

/**
 * Utility class for configuring executable JARs
 */
@CompileStatic
class ExecutableUtils {

    /**
     * Configures an executable JAR with a default action
     *
     * @param mainProject The main project
     * @param executableJar The executable JAR to configure
     * @param defaultAction The default action to apply
     */
    static void configureExecutable(Project mainProject, AbstractExecutableJar executableJar, Action defaultAction) {
        // Configure the object with the default action
        GradleUtils.runAction(executableJar, defaultAction)

        // Show default values
        mainProject.logger.info("***** DEFAULT VALUES ***** ${executableJar.toString()}")
    }

}
