package com.etendorx.rx

import com.etendorx.rx.launch.RxLaunch
import groovy.transform.CompileStatic
import org.gradle.api.Project

@CompileStatic
class RxLoader {

    static void load(Project project) {
        RxConfigSetup.load(project)
        new RxLaunch(project)
    }

}
