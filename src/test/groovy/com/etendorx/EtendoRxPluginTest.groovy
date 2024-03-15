package com.etendorx

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import spock.lang.Specification
import org.gradle.testfixtures.ProjectBuilder
import com.etendorx.codegen.CodeGenLoader
import com.etendorx.rx.RxLoader

class EtendoRxPluginTest extends Specification {

    def "plugin adds extensions to project"() {
        setup:
        Project project = ProjectBuilder.builder().build()

        when:
        project.pluginManager.apply(EtendoRxPlugin)

        then:
        project.extensions.findByName(EtendoRxPlugin.EXTENSION_NAME) != null
    }

    def "plugin applies necessary plugins to project"() {
        setup:
        Project project = ProjectBuilder.builder().build()

        when:
        project.pluginManager.apply(EtendoRxPlugin)

        then:
        assert project.pluginManager.hasPlugin('java-base')
        assert project.pluginManager.hasPlugin('publishing')
        assert project.pluginManager.hasPlugin('maven-publish')
        assert project.pluginManager.hasPlugin('war')
    }

    def "plugin loads CodeGenLoader and RxLoader"() {
        setup:
        Project project = ProjectBuilder.builder().build()
        def codeGenLoader = Mock(CodeGenLoader)
        def rxLoader = Mock(RxLoader)

        when:
        project.pluginManager.apply(EtendoRxPlugin)

        then:
        0 * codeGenLoader.load(project)
        0 * rxLoader.load(project)
    }

    def "plugin logs information about version"() {
        setup:
        Project project = ProjectBuilder.builder().build()

        when:
        project.pluginManager.apply(EtendoRxPlugin)

        then:
        project.logger.lifecycle('ETENDO RX PLUGIN VERSION: ' + EtendoRxPlugin.PLUGIN_VERSION)
    }
}
