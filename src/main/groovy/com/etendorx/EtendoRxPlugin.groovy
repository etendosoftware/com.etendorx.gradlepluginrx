package com.etendorx


import com.etendorx.rx.RxLoader
import com.etendorx.codegen.CodeGenLoader
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.plugins.PublishingPlugin

class EtendoRxPlugin implements Plugin<Project> {

    static final String PLUGIN_VERSION = "1.0.0"
    static final String EXTENSION_NAME = "etendorx"

    @Override
    void apply(Project project) {

        System.out.println("**********************************************")
        System.out.println("* ETENDO RX PLUGIN VERSION: ${PLUGIN_VERSION}")
        System.out.println("**********************************************")

        def extension = project.extensions.create(EXTENSION_NAME, EtendoRxPluginExtension)
        project.getPluginManager().apply(JavaBasePlugin.class)
        project.getPluginManager().apply(PublishingPlugin.class)
        project.getPluginManager().apply(MavenPublishPlugin.class)
        project.getPluginManager().apply(WarPlugin.class)

        CodeGenLoader.load(project)
        RxLoader.load(project)

    }

}
