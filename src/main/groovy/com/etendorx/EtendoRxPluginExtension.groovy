package com.etendorx

import com.etendorx.rx.services.base.AbstractBaseService
import com.etendorx.rx.services.base.AbstractExecutableJar
import org.gradle.api.Action

class EtendoRxPluginExtension {

    Action<? super AbstractBaseService> configServerAction = {}

    Action<? super AbstractBaseService> authAction = {}

    Action<? super AbstractBaseService> asyncProcessAction = {}

    Action<? super AbstractBaseService> dasAction = {}

    Action<? super AbstractBaseService> edgeAction = {}

    Action<? super AbstractExecutableJar> codeGenAction = {}

    List<String> excludedServices = new ArrayList<>()

}
