package com.etendorx.rx.services.edge

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.gradle.GradleUtils
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
        service.subprojectPath = DEFAULT_PROJECT_PATH
        service.serviceName = DEFAULT_NAME
        service.port = DEFAULT_PORT
        service.dependencyGroup = DEFAULT_GROUP
        service.dependencyArtifact = DEFAULT_ARTIFACT
        service.dependencyVersion = DEFAULT_VERSION
        service.subProject = service.mainProject.findProject(service.subprojectPath)
        service.configurationContainer = service.mainProject.configurations.create(DEFAULT_CONFIG)
        service.setEnvironment([
                'SPRING_PROFILES_ACTIVE': 'dev'
        ])
    }

    EdgeService(Project mainProject) {
        super(mainProject, DEFAULT_ACTION)
    }

    @Override
    void configureExtensionAction() {
        GradleUtils.runAction(this, mainProject.extensions.findByType(EtendoRxPluginExtension).edgeAction)
        this.subProject = this.mainProject.findProject(this.subprojectPath)
    }

}
