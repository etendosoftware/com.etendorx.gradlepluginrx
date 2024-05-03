package com.etendorx

import com.etendorx.rx.services.base.AbstractBaseService
import com.etendorx.rx.services.base.AbstractExecutableJar
import org.gradle.api.Action

/**
 * This class represents the configuration extension for the EtendoRx plugin.
 */
class EtendoRxPluginExtension {

    /**
     * Action to configure the server.
     */
    Action<? super AbstractBaseService> configServerAction = {}

    /**
     * Action for authentication.
     */
    Action<? super AbstractBaseService> authAction = {}

    /**
     * Action for asynchronous processing.
     */
    Action<? super AbstractBaseService> asyncProcessAction = {}

    /**
     * Action for data access services.
     */
    Action<? super AbstractBaseService> dasAction = {}

    /**
     * Action for edge services.
     */
    Action<? super AbstractBaseService> edgeAction = {}

    /**
     * Action for code generation.
     */
    Action<? super AbstractExecutableJar> codeGenAction = {}

    /**
     * List of excluded services.
     */
    List<String> excludedServices = [] // Fixed ArrayList instantiation

    /**
     * The version of the plugin.
     */
    String version = 'latest.integration' // Fixed string quotation

    /**
     * Set the version of the plugin.
     *
     * @param version The version to set.
     */
    void setVersion(String version) {
        this.version = version
    }

}
