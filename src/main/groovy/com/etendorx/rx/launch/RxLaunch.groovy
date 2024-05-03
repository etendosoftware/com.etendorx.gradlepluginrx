package com.etendorx.rx.launch

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.rx.services.asyncprocess.AsyncProcessService
import com.etendorx.rx.services.auth.AuthService
import com.etendorx.rx.services.base.BaseService
import com.etendorx.rx.services.configserver.ConfigServer
import com.etendorx.rx.services.das.DasService
import com.etendorx.rx.services.edge.EdgeService
import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.process.internal.ExecException

import java.time.LocalDateTime
import java.util.stream.Collectors

class RxLaunch {

    static final String LAUNCH_TASK = "rx"
    static final String EXCLUDE_SERVICES_PROPERTY = "rx_exclude"

    static final int STATUS_STARTING_THRESHOLD = -20
    static final int STATUS_STOPPED = -999
    static final int STATUS_RUNNING = 1
    static final int STATUS_STARTING = 0

    Project mainProject
    TaskProvider launchServicesTask

    ConfigServer configServer
    AuthService authService
    DasService dasService
    EdgeService edgeService
    AsyncProcessService asyncProcessService

    List<BaseService> services = new ArrayList<>()
    List<String> servicesToExclude = new ArrayList<>()
    Map<String, Integer> servicesStatus = new HashMap<>()

    StyledTextOutput out

    RxLaunch(Project mainProject) {
        this.mainProject = mainProject
        services.add(configServer = new ConfigServer(this.mainProject))
        services.add(authService = new AuthService(this.mainProject))
        services.add(dasService = new DasService(this.mainProject))
        services.add(edgeService = new EdgeService(this.mainProject))
        services.add(asyncProcessService = new AsyncProcessService(this.mainProject))

        this.out = this.mainProject.services.get(StyledTextOutputFactory).create("STATUS")
        loadServicesToExclude()
        generateLaunchTask()
    }

    void loadServicesToExclude() {
        this.servicesToExclude.clear()
        String excludeServices = this.mainProject.findProperty(EXCLUDE_SERVICES_PROPERTY)
        if (excludeServices) {
            this.servicesToExclude.addAll(excludeServices.split(","))
        }
    }

    List<BaseService> configureServicesToRun() {
        def etendoRxPluginExtension = this.mainProject.extensions.findByType(EtendoRxPluginExtension)
        if (etendoRxPluginExtension != null) {
            this.servicesToExclude.addAll(etendoRxPluginExtension.excludedServices)
        }

        def servicesToRun = services.stream().filter({
            !(it.serviceName in this.servicesToExclude)
        }).collect(Collectors.toList()).each({
            it.configureExtensionAction()
        })

        return servicesToRun
    }

    void generateLaunchTask() {
        List<BaseService> servicesToRun = []

        this.launchServicesTask = this.mainProject.tasks.register(LAUNCH_TASK) {
            dependsOn({
                // Filter services to run
                servicesToRun = configureServicesToRun()

                // In case of the service is in sources
                // Execute the necessary jar tasks to generate the JAR files
                def dependsOnServices = []
                servicesToRun.each {
                    Optional<List<Task>> jarTasks = it.loadBuildTasks()
                    if (jarTasks.isPresent()) {
                        dependsOnServices.addAll(jarTasks.get())
                    }
                }
                return dependsOnServices
            })

            doLast {
                // Load the classpath files
                // The files are obtained from the 'bootJar' task output if the service is in sources
                // or downloading the JAR dependency
                servicesToRun.each {
                    it.loadJavaExecAction()
                }

                // Start config server
                ConfigServer configServer = servicesToRun.find {
                    it.serviceName = configServer.serviceName
                } as ConfigServer

                if (configServer) {
                    startConfigServer(configServer)
                }

                // Start services

                // Filter the config server service
                def servicesList = servicesToRun.stream().filter({
                    it.serviceName != configServer.serviceName
                }).collect(Collectors.toList())

                out.withStyle(StyledTextOutput.Style.Info).println("Starting services: ${servicesList*.serviceName}")
                startMultipleServices(servicesList)

                servicesHealCheck(servicesToRun)
            }
        }
    }

    void servicesHealCheck(List<BaseService> services) {
        do {
            Thread.sleep(10000)
            // Clear screen and send cursor to home
            out.println('\u001b[2J\u001b[H')
            out.withStyle(StyledTextOutput.Style.Info).println("${LocalDateTime.now()} - Running services status.")
            for (BaseService service : services) {
                try {
                    out.withStyle(StyledTextOutput.Style.Success).println("${styleServiceName(service.serviceName)}: " + fetchServiceStatus(service))
                    servicesStatus.put(service.serviceName, STATUS_RUNNING)
                } catch (ignored) {
                    handleServiceError(service, out)
                }
            }
        } while (true)
    }

    String fetchServiceStatus(BaseService service) {
        if (service.serviceName == configServer.serviceName) {
            return " UP" // Workaround for config server
        } else {
            def message = new URL("http://localhost:${service.port}/actuator/health").text
            try {
                return " ${new JsonSlurper().parseText(message).status}"
            } catch (Exception e) {
                // If the service is not up, the health endpoint will return a 404
                return "⚠ DOWN"
            }
        }
    }

    void handleServiceError(BaseService service, StyledTextOutput out) {
        int status = servicesStatus.getOrDefault(service.serviceName, STATUS_STARTING)
        if (status == STATUS_RUNNING || status == STATUS_STOPPED) {
            out.withStyle(StyledTextOutput.Style.Failure).println("${styleServiceName(service.serviceName)}: ⚠ Stopped")
            status = STATUS_STOPPED
        } else if (status <= STATUS_STARTING_THRESHOLD) {
            out.withStyle(StyledTextOutput.Style.Failure).println("${styleServiceName(service.serviceName)}: ⚠ Cannot connect")
        } else {
            out.withStyle(StyledTextOutput.Style.Info).println("${styleServiceName(service.serviceName)}: 🔄 Starting")
            status--
        }
        servicesStatus.put(service.serviceName, status)
    }

    void startMultipleServices(List<BaseService> services) {
        services.each {
            startService(it)
        }
    }

    void startService(BaseService service) {
        if (service != null && service.javaExecAction != null) {
            Thread.start {
                out.withStyle(StyledTextOutput.Style.Info).println("Starting service: [${service.serviceName}]")
                try {
                    this.mainProject.javaexec(service.javaExecAction)
                } catch (ExecException e) {
                    //
                    out.withStyle(StyledTextOutput.Style.Info).println("Warning! [" + service.serviceName + "] finished with non-zero exit value ")
                }
            }
        }
    }

    void startConfigServer(ConfigServer configServer) {
        // Start the service
        startService(configServer)

        def configServerIsUp = false
        do {
            try {
                Thread.sleep(5000)
                def check = new URL("http://localhost:" + configServer.port + "/application/default").text
                out.withStyle(StyledTextOutput.Style.Success).println("UP")
                configServerIsUp = true
            } catch (ignored) {
                out.withStyle(StyledTextOutput.Style.Failure).println("DOWN")
            }
        } while (!configServerIsUp)
        out.withStyle(StyledTextOutput.Style.Success).println("Config server is up.")
    }

    String styleServiceName(String serviceName) {
        return "${serviceName.padRight(10).toUpperCase()}";
    }

}
