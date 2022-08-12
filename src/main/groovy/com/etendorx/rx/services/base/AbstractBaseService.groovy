package com.etendorx.rx.services.base

abstract class AbstractBaseService {

    /**
     * The project path notation of the service
     */
    String projectServicePath

    /**
     * The name of the service
     */
    String serviceName

    /**
     * The port of the service to start
     */
    String port

    /**
     * The group of the dependency to be resolved
     */
    String dependencyGroup

    /**
     * The artifact name of the dependency to ve resolved
     */
    String dependencyArtifact

    /**
     * The version of the dependency to be resolved
     */
    String dependencyVersion

    Map<String, ?> environment = new HashMap<>()

    /**
     * List of dynamic subproject paths to add to the DAS classpath service
     */
    List<String> subprojectsPath = new ArrayList<>()

    void subprojectsPath(List<String> subprojectPath) {
        this.subprojectsPath.addAll(subprojectPath)
    }

    void environment(Map<String, ?> environmentVariables) {
        this.environment.putAll(environmentVariables);
    }
}
