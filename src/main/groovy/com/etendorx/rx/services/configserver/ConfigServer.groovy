package com.etendorx.rx.services.configserver

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.rx.services.base.BaseService
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class ConfigServer extends BaseService {

    static final String DEFAULT_PROJECT_PATH = ":com.etendorx.configserver"
    static final String DEFAULT_NAME = "config"
    static final String DEFAULT_PORT = "8888"
    static final String DEFAULT_GROUP = "com.etendorx"
    static final String DEFAULT_ARTIFACT = "configserver"
    static final String DEFAULT_VERSION = "latest.integration"
    static final String DEFAULT_CONFIG = "configserver"

    static final Action<BaseService> DEFAULT_ACTION = { BaseService service ->
        service.projectServicePath = DEFAULT_PROJECT_PATH
        service.serviceName = DEFAULT_NAME
        service.port = DEFAULT_PORT
        service.dependencyGroup = DEFAULT_GROUP
        service.dependencyArtifact = DEFAULT_ARTIFACT
        service.dependencyVersion = DEFAULT_VERSION
        service.setEnvironment([
                'SPRING_PROFILES_ACTIVE': "native",
                "SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCHLOCATIONS": "file://${service.mainProject.projectDir.absolutePath}/rxconfig"
        ])
    }

    TaskProvider runServiceTask

    ConfigServer(Project mainProject) {
        super(mainProject,
                DEFAULT_ACTION,
                mainProject.extensions.findByType(EtendoRxPluginExtension).configServerAction
        )
        this.configurationContainer = this.mainProject.configurations.create(DEFAULT_CONFIG)
    }

    void load() {
        loadJavaExecAction()
    }

}
