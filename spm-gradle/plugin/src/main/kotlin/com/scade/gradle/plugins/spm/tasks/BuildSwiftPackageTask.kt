package com.scade.gradle.plugins.spm.tasks

import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem;

abstract class BuildSwiftPackageTask() : SpmGradlePluginTask() {
    @TaskAction
    fun run() {
        if (OperatingSystem.current().isMacOsX) {
            scd("build", "--build-dir", buildDirPath, "--path", packageDir, "--platform", "macos")
        }
    }
}