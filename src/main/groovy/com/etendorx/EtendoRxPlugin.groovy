package com.etendorx

import com.etendorx.codegen.CodeGenLoader
import com.etendorx.rx.RxLoader
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.plugins.PublishingPlugin

/**
 * The EtendoRxPlugin class implements the Plugin interface for Gradle projects.
 */
class EtendoRxPlugin implements Plugin<Project> {

    /**
     * The version of the plugin.
     */
    static final String PLUGIN_VERSION = '2.1.0'
    static final String EXTENSION_NAME = 'etendorx'
    static final String LINE = '**********************************************'

    @Override
    /**
     * Applies the plugin to the given project.
     *
     * @param project The project to apply the plugin to.
     */
    void apply(Project project) {
        logInfo(project)
        project.extensions.create(EXTENSION_NAME, EtendoRxPluginExtension)
        project.getPluginManager().apply(JavaBasePlugin)
        project.getPluginManager().apply(PublishingPlugin)
        project.getPluginManager().apply(MavenPublishPlugin)
        project.getPluginManager().apply(WarPlugin)

        // Add RX repositories dynamically to all projects
        configureRepositories(project)

        // Check if Etendo Classic plugin is present (indicates etendo_base environment)
        def isEtendoBase = project.rootProject.plugins.hasPlugin('com.etendoerp.gradleplugin')

        // Only load CodeGenLoader for subprojects when in etendo_base (e.g., :rx)
        // to avoid task conflicts with the Etendo Classic plugin's generate.entities task
        // In etendo_rx (pure RX project), load for all projects including root
        if (!isEtendoBase || project != project.rootProject) {
            CodeGenLoader.load(project)
        }

        // RxLoader handles detection internally and loads appropriately based on environment
        RxLoader.load(project)
    }

    /**
     * Configures RX repositories for the project and all subprojects.
     */
    private static void configureRepositories(Project project) {
        // Only configure from root project to avoid duplicates
        if (project == project.rootProject) {
            project.allprojects { p ->
                p.repositories {
                    mavenCentral()
                    maven {
                        url = "https://maven.pkg.github.com/etendosoftware/etendo_rx"
                        credentials {
                            username = p.findProperty("githubUser") ?: System.getenv("GITHUB_USER")
                            password = p.findProperty("githubToken") ?: System.getenv("GITHUB_TOKEN")
                        }
                    }
                }
            }
        }
    }

    /**
     * Logs information about the plugin.
     */
    private static void logInfo(Project project) {
        project.logger.info(LINE)
        project.logger.info('* ETENDO RX PLUGIN VERSION: ' + PLUGIN_VERSION)
        project.logger.info(LINE)
    }

}
