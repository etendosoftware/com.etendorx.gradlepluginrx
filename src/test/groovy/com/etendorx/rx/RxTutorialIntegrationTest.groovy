package com.etendorx.rx

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

import static org.junit.jupiter.api.Assertions.*

/**
 * Integration test that follows the RX tutorial from doc.md
 * This test:
 * 1. Clones etendo_base from main branch
 * 2. Applies the RX plugin
 * 3. Follows all tutorial steps
 * 4. Verifies the tutorial module builds successfully
 */
class RxTutorialIntegrationTest {

    @TempDir
    Path tempDir

    Path projectDir
    Path buildSrcDir

    @BeforeEach
    void setup() {
        projectDir = tempDir.resolve("test-project")
        
        // Clone etendo_base
        println "Cloning etendo_base..."
        def process = new ProcessBuilder("git", "clone", "--depth", "1", "-b", "main", "https://github.com/etendosoftware/etendo_base.git", projectDir.toString())
            .redirectErrorStream(true)
            .start()
        process.waitFor()
        if (process.exitValue() != 0) {
            throw new RuntimeException("Failed to clone etendo_base: " + process.inputStream.text)
        }

        buildSrcDir = projectDir.resolve("buildSrc")
        // No need to create buildSrcDir as it might exist or will be populated by copyBuildSrcPlugin
    }

    @Test
    void testRxTutorialWorkflow() {
        println ">>> STARTING RX TUTORIAL INTEGRATION TEST <<<"

        // Step 1: Configure base project (gradle.properties, etc.)
        println "Step 1: Configure base project..."
        setupBaseProject()

        // Step 2: Copy current buildSrc plugin to the test project
        println "Step 2: Copy current buildSrc plugin to the test project..."
        copyBuildSrcPlugin()

        // Step 3: Run rx.init task to create src-rx structure and configure root project
        println "Step 3: Run rx.init task..."
        // We need to apply the plugin in build.gradle first if it's not there, 
        // but copyBuildSrcPlugin does that by overwriting buildSrc. 
        // Wait, the root build.gradle needs the plugin applied.
        applyPluginToRootProject()
        
        runTask("rx.init")

        // Step 4: Create tutorial module using rx.new.module task with simplified parameter
        println "Step 4: Create tutorial module using rx.new.module task..."
        runTask("rx.new.module", "-Pmodule=com.etendorx.tutorial")

        // Step 5: Verify src-rx structure was created
        println "Step 5: Verify src-rx structure was created..."
        verifySrcRxStructure()

        // Step 6: Verify tutorial module structure was created
        println "Step 6: Verify tutorial module structure was created..."
        verifyTutorialModuleStructure()

        // Step 7: Compile the tutorial module
        println "Step 7: Compile the tutorial module..."
        def compileResult = runTask(":com.etendorx.tutorial:compileJava")
        assertTrue(compileResult.output.contains("BUILD SUCCESSFUL") || 
                   compileResult.output.contains("UP-TO-DATE"),
                   "Tutorial module should compile successfully")

        println "✓ RX Tutorial integration test passed successfully!"
        println ">>> FINISHED RX TUTORIAL INTEGRATION TEST <<<"
    }

    private void setupBaseProject() {
        // Create config directory if it doesn't exist
        Files.createDirectories(projectDir.resolve("config"))
        
        // Create dummy Openbravo.properties
        projectDir.resolve("config/Openbravo.properties").text = "test=true"

        // Copy gradle.properties from buildSrc to the test project root
        def currentBuildSrc = new File(System.getProperty("user.dir"))
        def sourceGradleProps = new File(currentBuildSrc, "gradle.properties")
        def targetGradleProps = projectDir.resolve("gradle.properties")
        
        if (sourceGradleProps.exists()) {
            Files.copy(sourceGradleProps.toPath(), targetGradleProps, StandardCopyOption.REPLACE_EXISTING)
            
            // Append test configuration
            targetGradleProps.toFile().append("""
                
                # Disable daemon and parallel execution in test environment
                org.gradle.daemon=false
                org.gradle.parallel=false
                org.gradle.caching=false
            """.stripIndent())
        } else {
            // Fallback if not found
            targetGradleProps.text = """
                githubUser=test
                githubToken=test
                
                # Disable daemon and parallel execution in test environment
                org.gradle.daemon=false
                org.gradle.parallel=false
                org.gradle.caching=false
            """.stripIndent()
        }
    }

    private void applyPluginToRootProject() {
        def buildGradle = projectDir.resolve("build.gradle")
        def content = buildGradle.text
        
        // Keep Etendo Core plugin but remove testing plugin for test environment
        content = content.replaceAll(/id 'com.etendoerp.testing.gradleplugin' version '.*'/, "")

        if (!content.contains("com.etendorx.gradlepluginrx")) {
            // Inject plugin application
            if (content.contains("plugins {")) {
                content = content.replace("plugins {", "plugins {\n    id 'com.etendorx.gradlepluginrx'")
            } else {
                content = "plugins {\n    id 'com.etendorx.gradlepluginrx'\n}\n" + content
            }
        }
        buildGradle.text = content
    }

    private void copyBuildSrcPlugin() {
        // Get the current buildSrc location
        def currentBuildSrc = new File(System.getProperty("user.dir"))
        
        // Copy only the source groovy files (not the entire build infrastructure)
        def srcMainGroovy = new File(currentBuildSrc, "src/main/groovy")
        def targetSrcMainGroovy = buildSrcDir.resolve("src/main/groovy")
        
        if (srcMainGroovy.exists()) {
            srcMainGroovy.eachFileRecurse { file ->
                if (file.isFile() && file.name.endsWith(".groovy")) {
                    def relativePath = srcMainGroovy.toPath().relativize(file.toPath())
                    def targetFile = targetSrcMainGroovy.resolve(relativePath)
                    Files.createDirectories(targetFile.parent)
                    Files.copy(file.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
        
        // Copy resources if they exist
        def srcMainResources = new File(currentBuildSrc, "src/main/resources")
        def targetSrcMainResources = buildSrcDir.resolve("src/main/resources")
        
        if (srcMainResources.exists()) {
            srcMainResources.eachFileRecurse { file ->
                if (file.isFile()) {
                    def relativePath = srcMainResources.toPath().relativize(file.toPath())
                    def targetFile = targetSrcMainResources.resolve(relativePath)
                    Files.createDirectories(targetFile.parent)
                    Files.copy(file.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
        
        // Create a minimal build.gradle WITHOUT any test configuration
        def buildGradleFile = buildSrcDir.resolve("build.gradle")
        buildGradleFile.text = """
plugins {
    id 'java'
    id 'groovy'
    id 'java-gradle-plugin'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation gradleApi()
}

gradlePlugin {
    plugins {
        etendoPlugin {
            id = "com.etendorx.gradlepluginrx"
            implementationClass = "com.etendorx.EtendoRxPlugin"
        }
    }
}

// Explicitly disable all test tasks
tasks.withType(Test).configureEach {
    enabled = false
}
"""
        
        // Create minimal gradle.properties
        def gradlePropsFile = buildSrcDir.resolve("gradle.properties")
        gradlePropsFile.text = """
org.gradle.daemon=false
org.gradle.parallel=false
org.gradle.caching=false
"""
    }



    private void verifySrcRxStructure() {
        def srcRxDir = projectDir.resolve("src-rx")
        assertTrue(Files.exists(srcRxDir), "src-rx directory should exist")
        assertTrue(Files.exists(srcRxDir.resolve("build.gradle")), "src-rx/build.gradle should exist")
        assertTrue(Files.exists(srcRxDir.resolve("settings.gradle")), "src-rx/settings.gradle should exist")
        assertTrue(Files.exists(srcRxDir.resolve("gradle.properties.template")), "src-rx/gradle.properties.template should exist")
        assertTrue(Files.exists(srcRxDir.resolve("rxconfig")), "src-rx/rxconfig directory should exist")
        assertTrue(Files.exists(srcRxDir.resolve("src/main/resources/openbravo.properties")), 
                   "Openbravo.properties should be copied to src-rx/src/main/resources")
    }

    private void verifyTutorialModuleStructure() {
        def moduleDir = projectDir.resolve("modules_rx/com.etendorx.tutorial")
        assertTrue(Files.exists(moduleDir), "Tutorial module directory should exist")
        assertTrue(Files.exists(moduleDir.resolve("build.gradle")), "Tutorial module build.gradle should exist")
        assertTrue(Files.exists(moduleDir.resolve("src/main/java/com/etendorx/tutorial/TutorialApplication.java")), 
                   "TutorialApplication.java should exist")
        assertTrue(Files.exists(moduleDir.resolve("src/main/resources/application.properties")), 
                   "application.properties should exist")
    }



    private def runTask(String... tasks) {
        def taskList = tasks as List<String>
        // Add flags to prevent recursive test execution and ensure clean test environment
        taskList.addAll(["-x", "test", "--no-parallel", "--no-build-cache", "--stacktrace", "--info"])
        
        def result = GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments(taskList)
            .withPluginClasspath()
            .withGradleVersion("8.12.1")
            .forwardOutput()
            .build()
        
        return result
    }
}
