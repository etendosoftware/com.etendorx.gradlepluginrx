package com.etendorx.rx


import com.etendorx.rx.launch.RxLaunch
import org.gradle.api.Project

class RxLoader {

    static void load(Project project) {
        RxConfigSetup.load(project)
        new RxLaunch(project)
    }

}
