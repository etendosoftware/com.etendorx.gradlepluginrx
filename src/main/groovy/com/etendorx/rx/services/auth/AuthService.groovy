package com.etendorx.rx.services.auth

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.gradle.GradleUtils
import com.etendorx.rx.services.base.BaseService
import org.gradle.api.Action
import org.gradle.api.Project

/**
 * Service class for handling authentication related functionality.
 */
class AuthService extends BaseService {

    /**
     * Default project path for the authentication service.
     */
    static final String DEFAULT_PROJECT_PATH = ':com.etendorx.auth'
    static final String DEFAULT_NAME = 'auth'
    static final String DEFAULT_PORT = '8094'
    static final String DEFAULT_GROUP = 'com.etendorx'
    static final String DEFAULT_ARTIFACT = 'auth'
    static final String DEFAULT_VERSION = 'latest.integration'
    static final String DEFAULT_CONFIG = 'auth'

    /**
     * Default action for the authentication service.
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
     * Constructor for AuthService.
     * @param mainProject The main project instance.
     */
    AuthService(Project mainProject) {
        super(mainProject, DEFAULT_ACTION)
    }

    /**
     * Configures the extension action for the authentication service.
     */
    @Override
    void configureExtensionAction() {
        GradleUtils.runAction(this, mainProject.extensions.findByType(EtendoRxPluginExtension).authAction)
        this.subProject = this.mainProject.findProject(this.subprojectPath)
    }
}