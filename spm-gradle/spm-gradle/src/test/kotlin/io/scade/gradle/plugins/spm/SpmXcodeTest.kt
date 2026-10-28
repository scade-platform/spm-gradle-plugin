package io.scade.gradle.plugins.spm

import io.scade.gradle.plugins.spm.tasks.ResolveScdToolTask
import io.scade.gradle.plugins.spm.tasks.SpmGradlePluginTask
import io.scade.gradle.plugins.spm.tasks.configureXcode
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SpmXcodeTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun selectedXcodeReachesChildProcessAndPreservesOtherVariables() {
        assumeTrue(File("/bin/sh").canExecute())
        val project = ProjectBuilder.builder().build()
        val selected = temporaryFolder.root.resolve("Xcode With Spaces.app").absolutePath
        val output = ByteArrayOutputStream()
        project.exec {
            it.environment("SPM_TEST_KEEP", "preserved")
            it.configureXcode(selected)
            it.commandLine("/bin/sh", "-c", """printf '%s\n' "${'$'}SCADE_XCODE" "${'$'}DEVELOPER_DIR" "${'$'}SPM_TEST_KEEP" """)
            it.standardOutput = output
        }
        assertEquals(listOf(selected, "$selected/Contents/Developer", "preserved"), output.toString().trimEnd().lines())
    }

    @Test
    fun unsetXcodeLeavesProcessEnvironmentUntouched() {
        assumeTrue(File("/bin/sh").canExecute())
        val project = ProjectBuilder.builder().build()
        val output = ByteArrayOutputStream()
        project.exec {
            it.environment("SCADE_XCODE", "existing-xcode")
            it.environment("DEVELOPER_DIR", "existing-developer-dir")
            it.configureXcode(null)
            it.commandLine("/bin/sh", "-c", """printf '%s\n' "${'$'}SCADE_XCODE" "${'$'}DEVELOPER_DIR" """)
            it.standardOutput = output
        }
        assertEquals(listOf("existing-xcode", "existing-developer-dir"), output.toString().trimEnd().lines())
    }

    @Test
    fun localPropertiesDefaultAndDslOverrideReachEveryTask() {
        val directory = temporaryFolder.newFolder("project")
        val localXcode = temporaryFolder.newFolder("Local Xcode.app")
        directory.resolve("local.properties").writeText("swift.xcode=${localXcode.absolutePath}\n")
        val project = ProjectBuilder.builder().withProjectDir(directory).build()
        project.pluginManager.apply("java")
        project.pluginManager.apply(SpmGradlePlugin::class.java)
        val extension = project.extensions.getByType(SpmGradlePluginExtension::class.java)
        val tasks = project.tasks.withType(SpmGradlePluginTask::class.java).toList()
        val resolver = project.tasks.getByName("resolveScdTool") as ResolveScdToolTask
        assertEquals(2, tasks.size)
        tasks.forEach { assertEquals(localXcode.absolutePath, it.xcodePath.get()) }
        assertEquals(localXcode.absolutePath, resolver.xcodePath.get())

        val explicitXcode = temporaryFolder.newFolder("Explicit Xcode.app")
        extension.xcode.fileValue(explicitXcode)
        tasks.forEach { assertEquals(explicitXcode.absolutePath, it.xcodePath.get()) }
        assertEquals(explicitXcode.absolutePath, resolver.xcodePath.get())
    }

    @Test
    fun missingLocalPropertyKeepsTaskSelectionUnset() {
        val directory = temporaryFolder.newFolder("unconfigured")
        directory.resolve("local.properties").writeText("sdk.dir=/unrelated/android-sdk\n")
        val project = ProjectBuilder.builder().withProjectDir(directory).build()
        project.pluginManager.apply("java")
        project.pluginManager.apply(SpmGradlePlugin::class.java)
        assertFalse(project.extensions.getByType(SpmGradlePluginExtension::class.java).xcode.isPresent)
        project.tasks.withType(SpmGradlePluginTask::class.java).forEach { assertFalse(it.xcodePath.isPresent) }
        assertFalse((project.tasks.getByName("resolveScdTool") as ResolveScdToolTask).xcodePath.isPresent)
    }
}
