package com.etendorx.rx.launch

import com.etendorx.EtendoRxPluginExtension
import com.etendorx.rx.services.asyncprocess.AsyncProcessService
import com.etendorx.rx.services.auth.AuthService
import com.etendorx.rx.services.base.BaseService
import com.etendorx.rx.services.configserver.ConfigServer
import com.etendorx.rx.services.das.DasService
import com.etendorx.rx.services.edge.EdgeService
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory

import java.time.LocalDate
import java.util.stream.Collectors

class RxLaunch {

    static final String LAUNCH_TASK = "rx"
    static final String EXCLUDE_SERVICES_PROPERTY = "rx_exclude"

    Project mainProject
    TaskProvider launchServicesTask

    ConfigServer configServer
    AuthService authService
    DasService dasService
    EdgeService edgeService
    AsyncProcessService asyncProcessService

    List<BaseService> services = new ArrayList<>()
    List<String> servicesToExclude = new ArrayList<>()

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
            out.withStyle(StyledTextOutput.Style.Info).println("${LocalDate.now()} - Running services status.")
            for (BaseService service : services) {
                try {
                    def message = new URL("http://localhost:" + service.port + "/actuator/health").text
                    if (service.serviceName == configServer.serviceName) {
                        // Workaround: config server doesn't work well with actuator functionality
                        message = "{\"status\": \"UP\"}"
                    }
                    out.withStyle(StyledTextOutput.Style.Success).println("[" + service.serviceName + ": " + message + "]")
                } catch (ignored) {
                    out.withStyle(StyledTextOutput.Style.Failure).println("[" + service.serviceName + ": Cannot connect]")
                }
            }
        } while (true)
    }

    void startMultipleServices(List<BaseService> services) {
        services.each {
            startService(it)
        }
    }

    void startService(BaseService service) {
        Thread.start {
            out.withStyle(StyledTextOutput.Style.Info).println("Starting service: ${service.serviceName}")
            this.mainProject.javaexec(service.javaExecAction)
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

}
