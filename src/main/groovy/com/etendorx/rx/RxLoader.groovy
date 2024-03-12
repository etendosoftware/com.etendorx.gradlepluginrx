package com.etendorx.rx

import com.etendorx.rx.launch.RxLaunch
import groovy.transform.CompileStatic
import org.gradle.api.Project

/**
 * A class responsible for loading Rx configuration and launching Rx.
 */
@CompileStatic
class RxLoader {

    /**
     * Loads Rx configuration and initializes RxLaunch.
     *
     * @param project The Gradle project
     */
    static void load(Project project) {
        RxConfigSetup.load(project)
        new RxLaunch(project)
    }

}