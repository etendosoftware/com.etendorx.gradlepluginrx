package com.etendorx.rx.services.edge

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.rx.services.base.BaseService
import org.gradle.api.Action
import org.gradle.api.Project

class EdgeService extends BaseService {

    static final String DEFAULT_PROJECT_PATH = ":com.etendorx.edge"
    static final String DEFAULT_NAME = "edge"
    static final String DEFAULT_PORT = "8096"
    static final String DEFAULT_GROUP = "com.etendorx"
    static final String DEFAULT_ARTIFACT = "edge"
    static final String DEFAULT_VERSION = "latest.integration"
    static final String DEFAULT_CONFIG = "edge"

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

    EdgeService(Project mainProject) {
        super(mainProject,
                DEFAULT_ACTION,
                mainProject.extensions.findByType(EtendoRxPluginExtension).edgeAction
        )
        this.configurationContainer = this.mainProject.configurations.create(DEFAULT_CONFIG)
    }

}
