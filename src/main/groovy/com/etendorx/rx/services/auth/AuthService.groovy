package com.etendorx.rx.services.auth

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.rx.services.base.BaseService
import org.gradle.api.Action
import org.gradle.api.Project

class AuthService extends BaseService {

    static final String DEFAULT_PROJECT_PATH = ":com.etendorx.auth"
    static final String DEFAULT_NAME = "auth"
    static final String DEFAULT_PORT = "8094"
    static final String DEFAULT_GROUP = "com.etendorx"
    static final String DEFAULT_ARTIFACT = "auth"
    static final String DEFAULT_VERSION = "latest.integration"
    static final String DEFAULT_CONFIG = "auth"

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

    AuthService(Project mainProject) {
        super(mainProject,
                DEFAULT_ACTION,
                mainProject.extensions.findByType(EtendoRxPluginExtension).authAction
        )
        this.configurationContainer = this.mainProject.configurations.create(DEFAULT_CONFIG)
    }
}
