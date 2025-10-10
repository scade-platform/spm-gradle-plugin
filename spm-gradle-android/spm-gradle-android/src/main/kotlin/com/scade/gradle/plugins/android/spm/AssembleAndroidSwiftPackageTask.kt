package com.scade.gradle.plugins.android.spm

import io.scade.gradle.plugins.spm.TargetPlatform
import io.scade.gradle.plugins.spm.tasks.AssembleSwiftPackageTask

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal


abstract class AssembleAndroidSwiftPackageTask: AssembleSwiftPackageTask() {
    @Internal
    val adbPath: RegularFileProperty = project.objects.fileProperty()

    private val defaultPlatformArgs = listOf(
        "--platform", "android-x86_64",
        "--platform", "android-arm64-v8a")

    override fun platformArgs(): List<String> {
        val abi = getConnectedDeviceAbi()

        val args = (if (abi != null && assembleDebug.get()) {
            project.logger.lifecycle("Detected connected device ABI: $abi")

            when (abi) {
                "arm64-v8a" -> listOf("--platform", "android-arm64-v8a")
                "x86_64" -> listOf("--platform", "android-x86_64")
                else -> {
                    project.logger.lifecycle("Connected device ABI unsupported. Building for default Android platforms.")
                    defaultPlatformArgs
                }
            }
        } else {
            if (assembleDebug.get()) {
                project.logger.lifecycle("⚠️ No connected devices found. Assembling for default Android platforms.")
            }
            defaultPlatformArgs

        }).toMutableList()

        platforms.get().forEach {
            if (it is TargetPlatform.Android) {
                it.toolchain?.let { path ->
                    args += listOf("--android-swift-toolchain", path.absolutePath)
                }
            }
        }

        return args
    }

    private fun getConnectedDeviceAbi(): String? {
        AndroidDebugBridge.initIfNeeded(false)
        val adb = AndroidDebugBridge.createBridge(adbPath.orNull?.asFile?.absolutePath, false)

        var attempts = 0
        while (!adb.hasInitialDeviceList() && attempts < 50) {
            Thread.sleep(100)
            attempts++
        }

        val devices = adb.devices
        if (devices.isEmpty()) {
            return null
        }

        val device: IDevice = devices[0]
        val abis = device.abis

        return abis.firstOrNull()
    }

}