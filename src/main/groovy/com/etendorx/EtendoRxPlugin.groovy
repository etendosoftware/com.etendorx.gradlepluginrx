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
    static final String PLUGIN_VERSION = '2.0.0'
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

        CodeGenLoader.load(project)
        RxLoader.load(project)
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
