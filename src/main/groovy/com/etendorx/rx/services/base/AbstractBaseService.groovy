package com.etendorx.rx.services.base

import groovy.transform.CompileStatic
import org.gradle.api.Project

/**
 * Abstract class representing a base service
 */
@CompileStatic
abstract class AbstractBaseService extends AbstractExecutableJar {

    /**
     * The name of the service
     */
    String serviceName

    /**
     * The port of the service to start
     */
    String port

    /**
     * List of dynamic subproject paths to add to the DAS classpath service
     */
    List<String> subprojectsPath = []

    /**
     * Constructor for AbstractBaseService
     * @param mainProject The main project
     */
    AbstractBaseService(Project mainProject) {
        super(mainProject)
    }

    /**
     * Add subproject paths to the service
     * @param subprojectPath List of subproject paths
     */
    void addSubprojectsPath(List<String> subprojectPath) {
        this.subprojectsPath.addAll(subprojectPath)
    }

    /**
     * Override toString method
     * @return String representation of the service
     */
    @Override
    String toString() {
        return """
        |* Service name: ${this.serviceName}
        |* Port: ${this.port}${super.toString()}""".stripMargin()
    }

}
