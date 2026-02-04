package com.etendorx.rx

import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Generates RX scaffolding and module templates from embedded resources.
 */
class RxTemplateTasks {

    static final String RX_INIT_TASK = 'rx.init'
    static final String RX_NEW_MODULE_TASK = 'rx.new.module'

    static void load(Project project) {
        // Only register these tasks on the root project to avoid duplicate execution
        if (project != project.rootProject) {
            return
        }

        project.tasks.register(RX_INIT_TASK) {
            group = 'rx'
            description = 'Creates src-rx structure from templates, copies Openbravo.properties, and configures root project'
            doLast {
                createRxStructure(project)
                createModulesGenStructure(project)
                configureRootProject(project)
            }
        }

        project.tasks.register(RX_NEW_MODULE_TASK) {
            group = 'rx'
            description = 'Creates a modules_rx template project (use -Pmodule=com.example.module)'
            doLast {
                createModuleTemplate(project)
            }
        }
    }

    private static void createRxStructure(Project project) {
        File rootDir = project.rootDir
        File srcRxDir = new File(rootDir, 'src-rx')
        File resourcesDir = new File(srcRxDir, 'src/main/resources')
        resourcesDir.mkdirs()

        writeTemplateIfMissing(project, 'etendorx/templates/src-rx/build.gradle', new File(srcRxDir, 'build.gradle'), [:])
        writeTemplateIfMissing(project, 'etendorx/templates/src-rx/settings.gradle', new File(srcRxDir, 'settings.gradle'), [:])
        writeTemplateIfMissing(project, 'etendorx/templates/src-rx/gradle.properties.template', new File(srcRxDir, 'gradle.properties.template'), [:])

        new File(srcRxDir, 'rxconfig').mkdirs()

        File obSource = new File(rootDir, 'config/Openbravo.properties')
        File obTarget = new File(resourcesDir, 'openbravo.properties')
        File gradlePropsTarget = new File(srcRxDir, 'gradle.properties')
        if (obSource.exists()) {
            obTarget.parentFile.mkdirs()
            obTarget.bytes = obSource.bytes
            project.logger.lifecycle("Copied Openbravo.properties to ${project.relativePath(obTarget)}")

            // Copy and append RX-specific properties
            String content = obSource.text
            content += """

# RX Configuration (added by rx.init)
rx.generateCode=true
rx.path=.
rx.computedColumns=true
rx.views=true
"""
            gradlePropsTarget.text = content
            project.logger.lifecycle("Created ${project.relativePath(gradlePropsTarget)} with RX properties")
        } else {
            project.logger.warn("Openbravo.properties not found at ${project.relativePath(obSource)}")
        }
    }

    private static void createModulesGenStructure(Project project) {
        File modulesGenDir = new File(project.rootDir, 'src-rx/modules_gen')

        // List of modules to create with their template files
        def modules = [
            'com.etendorx.entities': ['build.gradle'],
            'com.etendorx.clientrest': ['build.gradle'],
            'com.etendorx.entitiesModel': ['build.gradle'],
            'com.etendorx.grpc.common': ['build.gradle', 'grpc.gradle']
        ]

        modules.each { moduleName, files ->
            File moduleDir = new File(modulesGenDir, moduleName)
            moduleDir.mkdirs()

            files.each { fileName ->
                String templatePath = "etendorx/templates/modules_gen/${moduleName}/${fileName}"
                File destFile = new File(moduleDir, fileName)
                writeTemplateIfMissing(project, templatePath, destFile, [:])
            }

            project.logger.lifecycle("Created module structure: ${project.relativePath(moduleDir)}")
        }
    }

    private static void configureRootProject(Project project) {
        File rootSettings = new File(project.rootDir, 'settings.gradle')
        if (rootSettings.exists()) {
            String settingsContent = rootSettings.text
            if (!settingsContent.contains("include ':rx'")) {
                rootSettings.append("""
// RX Configuration
include ':rx'
project(':rx').projectDir = file('src-rx')

def rxDirs = [ "modules_rx", "src-rx/modules_gen" ]
rxDirs.each {
    def dir = new File(rootDir, it)
    if (dir.exists()) {
        def subDirs = dir.listFiles(new FileFilter() {
            boolean accept(File file) {
                if (!file.isDirectory()) {
                    return false
                }
                return new File(file, 'build.gradle').isFile()
            }
        })
        if (subDirs) {
            subDirs.each { File subDir ->
                include subDir.name
                project(":\${subDir.name}").projectDir = subDir
            }
        }
    }
}
""")
                project.logger.lifecycle("Updated settings.gradle with RX configuration")
            }
        }
    }

    private static void createModuleTemplate(Project project) {
        // Support both -Pmodule and -PmoduleName
        String moduleName = project.findProperty('module') ?: project.findProperty('moduleName')
        
        if (!moduleName) {
            throw new GradleException("Missing -Pmodule. Example: ./gradlew ${RX_NEW_MODULE_TASK} -Pmodule=com.etendorx.tutorial")
        }

        // Default javaPackage to moduleName if not provided
        String javaPackage = project.findProperty('javaPackage') ?: moduleName
        String className = buildAppClassName(javaPackage)

        File moduleDir = new File(project.rootDir, "modules_rx/${moduleName}")
        File javaDir = new File(moduleDir, "src/main/java/${javaPackage.replace('.', '/')}")
        File resourcesDir = new File(moduleDir, 'src/main/resources')
        javaDir.mkdirs()
        resourcesDir.mkdirs()

        Map<String, String> tokens = [
                '__JAVA_PACKAGE__'    : javaPackage,
                '__APP_CLASS__'       : className,
                '__MODULE_NAME__'     : moduleName,
        ]

        writeTemplateIfMissing(project, 'etendorx/templates/module/build.gradle', new File(moduleDir, 'build.gradle'), tokens)
        writeTemplateIfMissing(project, 'etendorx/templates/module/application.properties', new File(resourcesDir, 'application.properties'), tokens)
        writeTemplateIfMissing(project, 'etendorx/templates/module/Application.java', new File(javaDir, "${className}.java"), tokens)
        
        project.logger.lifecycle("RX Module created at ${project.relativePath(moduleDir)}")
        project.logger.lifecycle("Don't forget to refresh your Gradle project!")
    }

    private static String buildAppClassName(String javaPackage) {
        String lastSegment = javaPackage.tokenize('.').last()
        return lastSegment.substring(0, 1).toUpperCase() + lastSegment.substring(1) + 'Application'
    }

    private static void writeTemplateIfMissing(Project project, String resourcePath, File dest, Map<String, String> tokens) {
        if (dest.exists()) {
            project.logger.lifecycle("Skipping existing file: ${project.relativePath(dest)}")
            return
        }
        String template = readResource(project, resourcePath)
        String content = applyTokens(template, tokens)
        dest.parentFile.mkdirs()
        dest.text = content
        project.logger.lifecycle("Created ${project.relativePath(dest)}")
    }

    private static String readResource(Project project, String resourcePath) {
        InputStream stream = RxTemplateTasks.class.classLoader.getResourceAsStream(resourcePath)
        if (stream == null) {
            throw new GradleException("Template not found: ${resourcePath}")
        }
        stream.getText('UTF-8')
    }

    private static String applyTokens(String template, Map<String, String> tokens) {
        String result = template
        tokens.each { k, v -> result = result.replace(k, v) }
        result
    }
}
