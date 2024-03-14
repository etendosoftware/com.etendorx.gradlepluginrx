import com.etendorx.rx.launch.RxLaunch
import com.etendorx.rx.services.auth.AuthService
import com.etendorx.rx.services.base.BaseService
import com.etendorx.rx.services.configserver.ConfigServer
import spock.lang.Specification
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project

class RxLaunchTest extends Specification {

    def "loadServicesToExclude splits comma separated services"() {
        given:
        Project project = ProjectBuilder.builder().build()
        project.ext.rx_exclude = "service1,service2"
        RxLaunch rxLaunch = new RxLaunch(project)

        when:
        rxLaunch.loadServicesToExclude()

        then:
        rxLaunch.servicesToExclude == ["service1", "service2"]
    }

    def "configureServicesToRun excludes specified services"() {
        given:
        Project project = ProjectBuilder.builder().build()
        project.ext.rx_exclude = "configServer"
        RxLaunch rxLaunch = new RxLaunch(project)

        when:
        List<BaseService> servicesToRun = rxLaunch.configureServicesToRun()

        then:
        !servicesToRun.find { it.serviceName == "configServer" }
    }

}
