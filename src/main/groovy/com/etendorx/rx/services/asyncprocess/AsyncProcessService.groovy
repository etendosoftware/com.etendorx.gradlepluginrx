package com.etendorx.rx.services.asyncprocess

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.gradle.GradleUtils
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

    AsyncProcessService(Project mainProject) {
        super(mainProject,
                DEFAULT_ACTION,
        )
    }

    @Override
    void configureExtensionAction() {
        def extension = mainProject.extensions.findByType(EtendoRxPluginExtension)
        if(extension != null) {
            GradleUtils.runAction(this, extension.asyncProcessAction)
            this.subProject = this.mainProject.findProject(this.subprojectPath)
        }
    }
}
