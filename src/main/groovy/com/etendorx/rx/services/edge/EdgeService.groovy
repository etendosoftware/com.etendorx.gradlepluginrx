package com.etendorx.rx.services.edge

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.gradle.GradleUtils
import com.etendorx.rx.services.base.BaseService
import org.gradle.api.Action
import org.gradle.api.Project

/**
 * Represents the EdgeService class that extends BaseService.
 */
class EdgeService extends BaseService {

    /**
     * Default project path for the Edge service.
     */
    static final String DEFAULT_PROJECT_PATH = ':com.etendorx.edge'
    static final String DEFAULT_NAME = 'edge'
    static final String DEFAULT_PORT = '8096'
    static final String DEFAULT_GROUP = 'com.etendorx'
    static final String DEFAULT_ARTIFACT = 'edge'
    static final String DEFAULT_VERSION = 'latest.integration'
    static final String DEFAULT_CONFIG = 'edge'

    /**
     * Default action for the EdgeService.
     */
    static final Action<BaseService> DEFAULT_ACTION = { BaseService service ->
        EtendoRxPluginExtension extension = service.mainProject.extensions.findByType(EtendoRxPluginExtension)

        service.subprojectPath = DEFAULT_PROJECT_PATH
        service.serviceName = DEFAULT_NAME
        service.port = DEFAULT_PORT
        service.dependencyGroup = DEFAULT_GROUP
        service.dependencyArtifact = DEFAULT_ARTIFACT
        String version = extension.version ?: DEFAULT_VERSION
        service.dependencyVersion = version
        service.subProject = service.mainProject.findProject(service.subprojectPath)
        service.configurationContainer = service.mainProject.configurations.create(DEFAULT_CONFIG)
        service.environment = [
                'SPRING_PROFILES_ACTIVE': 'dev'
        ]
    }

    /**
     * Constructor for EdgeService.
     * @param mainProject The main project.
     */
    EdgeService(Project mainProject) {
        super(mainProject, DEFAULT_ACTION)
    }

    /**
     * Configures the extension action for EdgeService.
     */
    @Override
    void configureExtensionAction() {
        GradleUtils.runAction(this, mainProject.extensions.findByType(EtendoRxPluginExtension).edgeAction)
        this.subProject = this.mainProject.findProject(this.subprojectPath)
    }
}