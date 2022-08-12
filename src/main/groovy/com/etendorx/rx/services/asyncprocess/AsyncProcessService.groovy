package com.etendorx.rx.services.asyncprocess

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.rx.services.base.BaseService
import org.gradle.api.Action
import org.gradle.api.Project

class AsyncProcessService extends BaseService {

    static final String DEFAULT_PROJECT_PATH = ":com.etendorx.asyncprocess"
    static final String DEFAULT_NAME = "async"
    static final String DEFAULT_PORT = "8099"
    static final String DEFAULT_GROUP = "com.etendorx"
    static final String DEFAULT_ARTIFACT = "asyncprocess"
    static final String DEFAULT_VERSION = "latest.integration"
    static final String DEFAULT_CONFIG = "asyncprocess"

    static final Action<BaseService> DEFAULT_ACTION = { BaseService service ->
        service.projectServicePath = DEFAULT_PROJECT_PATH
        service.serviceName = DEFAULT_NAME
        service.port = DEFAULT_PORT
        service.dependencyGroup = DEFAULT_GROUP
        service.dependencyArtifact = DEFAULT_ARTIFACT
        service.dependencyVersion = DEFAULT_VERSION
        service.setEnvironment([
                'SPRING_PROFILES_ACTIVE' : 'dev'
        ])
    }

    AsyncProcessService(Project mainProject) {
        super(mainProject,
                DEFAULT_ACTION,
                mainProject.extensions.findByType(EtendoRxPluginExtension).authAction
        )
        this.configurationContainer = this.mainProject.configurations.create(DEFAULT_CONFIG)
    }
}
