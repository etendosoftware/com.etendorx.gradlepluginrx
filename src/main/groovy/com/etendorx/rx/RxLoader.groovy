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
        // Check if Etendo Classic plugin is present (indicates etendo_base environment)
        def isEtendoBase = project.rootProject.plugins.hasPlugin('com.etendoerp.gradleplugin')
        
        // RxTemplateTasks provides rx.init and rx.new.module - always load for all projects
        // These tasks are needed in root to create the initial :rx structure
        RxTemplateTasks.load(project)
        
        // Only load RxConfigSetup and RxLaunch for subprojects when in etendo_base
        // to avoid task conflicts with existing tasks in the root project (setup, rx, etc.)
        // In etendo_rx (pure RX project), load for all projects including root
        if (!isEtendoBase || project != project.rootProject) {
            RxConfigSetup.load(project)
            new RxLaunch(project)
        }
    }

}
