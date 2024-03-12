package com.etendorx.rx.services.base

import groovy.transform.CompileStatic
import org.gradle.api.Project

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

    AbstractBaseService(Project mainProject) {
        super(mainProject)
    }

    void addSubprojectsPath(List<String> subprojectPath) {
        this.subprojectsPath.addAll(subprojectPath)
    }

    @Override
    String toString() {
        return """
        |* Service name: ${this.serviceName}
        |* Port: ${this.port}${super.toString()}""".stripMargin()
    }

}
