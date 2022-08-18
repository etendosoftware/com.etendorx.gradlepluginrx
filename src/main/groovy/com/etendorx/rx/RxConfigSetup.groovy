package com.etendorx.rx

import org.gradle.api.Project
import org.gradle.api.tasks.Copy

class RxConfigSetup {

    static void load(Project project) {
        project.tasks.register("setup", Copy) {
            from('rxconfig') {
                include '*.yaml.template'
            }
            into('rxconfig')
            rename { fileName ->
                fileName.replace('.yaml.template', '.yaml')
            }
        }
    }
}
