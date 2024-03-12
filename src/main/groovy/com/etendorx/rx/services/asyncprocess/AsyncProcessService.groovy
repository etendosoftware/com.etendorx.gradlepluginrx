package com.etendorx.rx.services.asyncprocess

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.gradle.GradleUtils
import com.etendorx.rx.services.base.BaseService
import org.gradle.api.Action
import org.gradle.api.Project

/**
 * This class represents the AsyncProcessService which handles asynchronous processes.
 */
class AsyncProcessService extends BaseService {

    /**
     * Default project path for the async process.
     */
    static final String DEFAULT_PROJECT_PATH = ':com.etendorx.asyncprocess'
    static final String DEFAULT_NAME = 'async'
    static final String DEFAULT_PORT = '8099'
    static final String DEFAULT_GROUP = 'com.etendorx'
    static final String DEFAULT_ARTIFACT = 'asyncprocess'
    static final String DEFAULT_VERSION = 'latest.integration'
    static final String DEFAULT_CONFIG = 'asyncprocess'

    /**
     * Default action for the AsyncProcessService.
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
     * Constructor for AsyncProcessService.
     * @param mainProject The main project where the service is being configured.
     */
    AsyncProcessService(Project mainProject) {
        super(mainProject,
                DEFAULT_ACTION,
        )
    }

    /**
     * Configures the extension action for the AsyncProcessService.
     */
    @Override
    void configureExtensionAction() {
        GradleUtils.runAction(this, mainProject.extensions.findByType(EtendoRxPluginExtension).asyncProcessAction)
        this.subProject = this.mainProject.findProject(this.subprojectPath)
    }
}