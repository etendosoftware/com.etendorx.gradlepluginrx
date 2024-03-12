package com.etendorx.rx.services.configserver

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.gradle.GradleUtils
import com.etendorx.rx.services.base.BaseService
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class ConfigServer extends BaseService {

    static final String DEFAULT_PROJECT_PATH = ':com.etendorx.configserver'
    static final String DEFAULT_NAME = 'config'
    static final String DEFAULT_PORT = '8888'
    static final String DEFAULT_GROUP = 'com.etendorx'
    static final String DEFAULT_ARTIFACT = 'configserver'
    static final String DEFAULT_VERSION = 'latest.integration'
    static final String DEFAULT_CONFIG = 'configserver'

    static final Action<BaseService> DEFAULT_ACTION = { BaseService service ->
        EtendoRxPluginExtension extension = service.mainProject.extensions.findByType(EtendoRxPluginExtension)

        service.subprojectPath = DEFAULT_PROJECT_PATH
        service.serviceName = DEFAULT_NAME
        service.port = DEFAULT_PORT
        service.dependencyGroup = DEFAULT_GROUP
        service.dependencyArtifact = DEFAULT_ARTIFACT
        var version = extension.version
        if (version == null) {
            version = DEFAULT_VERSION
        }
        service.dependencyVersion = version
        service.subProject = service.mainProject.findProject(service.subprojectPath)
        service.configurationContainer = service.mainProject.configurations.create(DEFAULT_CONFIG)
        service.environment = [
                'SPRING_PROFILES_ACTIVE'                           : 'native',
                'SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCHLOCATIONS': "file://${service.mainProject.projectDir.absolutePath}/rxconfig",
        ]
    }

    TaskProvider runServiceTask

    ConfigServer(Project mainProject) {
        super(mainProject, DEFAULT_ACTION)
    }

    void load() {
        loadJavaExecAction()
    }

    @Override
    void configureExtensionAction() {
        GradleUtils.runAction(this, mainProject.extensions.findByType(EtendoRxPluginExtension).configServerAction)
        this.subProject = this.mainProject.findProject(this.subprojectPath)
    }
}
