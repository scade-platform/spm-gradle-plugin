package com.scade.gradle.plugins.android.spm

//import com.android.ddmlib.IDevice
//import com.android.build.gradle.internal.devices.ConnectedDeviceProvider

import io.scade.gradle.plugins.spm.TargetPlatform
import io.scade.gradle.plugins.spm.tasks.AssembleSwiftPackageTask

abstract class AssembleAndroidSwiftPackageTask: AssembleSwiftPackageTask() {
    override fun platformArgs(): List<String> {
        val args = mutableListOf("--platform", "android-x86_64", "--platform", "android-arm64-v8a")

        platforms.get().forEach {
            if (it is TargetPlatform.Android) {
                it.toolchain?.let { path ->
                    args += listOf("--android-swift-toolchain", path.absolutePath)
                }
            }
        }

        return args
    }
}