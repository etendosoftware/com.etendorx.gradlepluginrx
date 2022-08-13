package com.etendorx.rx.services.base

import org.gradle.api.Project

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
    List<String> subprojectsPath = new ArrayList<>()

    AbstractBaseService(Project mainProject) {
        super(mainProject)
    }

    void subprojectsPath(List<String> subprojectPath) {
        this.subprojectsPath.addAll(subprojectPath)
    }

    @Override
    String toString() {
        return """
        |* Service name: ${this.serviceName}
        |* Port: ${this.port}${super.toString()}""".stripMargin()
    }
}
