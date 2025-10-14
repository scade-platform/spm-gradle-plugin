package io.scade.gradle.plugins.android.spm

import io.scade.gradle.plugins.spm.TargetPlatform
import io.scade.gradle.plugins.spm.tasks.AssembleSwiftPackageTask

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal


abstract class AssembleAndroidSwiftPackageTask: AssembleSwiftPackageTask() {
    @Internal
    val sdkPath: DirectoryProperty = project.objects.directoryProperty()

    @Internal
    val ndkPath: DirectoryProperty = project.objects.directoryProperty()

    @Internal
    val adbPath: RegularFileProperty = project.objects.fileProperty()

    override fun platformArgs(): List<String> {
        val args = mutableListOf<String>()
        var buildArchs = listOf<String>()

        val androidPlatform = platforms.get().find { it is TargetPlatform.Android }
        androidPlatform?.let {
            buildArchs = it.archs
            it.toolchain?.let { path ->
                args += listOf("--android-swift-toolchain", path.absolutePath)
            }
        }

        if (assembleDebug.get()) {
            val abi = getConnectedDeviceAbi()
            if (abi != null) {
                project.logger.lifecycle("Detected connected device ABI: $abi")
                if (abi in SpmGradleAndroidPlugin.supportedBuildArchs) {
                    buildArchs = listOf(abi)
                } else {
                    project.logger.lifecycle("Connected device ABI unsupported. Building for default Android platforms.")
                }
            } else {
                project.logger.lifecycle("⚠️ No connected devices found. Assembling for default Android platforms.")
            }
        }

        args += buildArchs.flatMap {
            listOf("--platform", "android-$it")
        }

        try {
            sdkPath.orNull?.let {
                val path = it.asFile.absolutePath
                project.logger.lifecycle("Building for Android SDK at: $path")
                args += listOf("--android-sdk", path)
            }
        } catch (_: Exception) {
            project.logger.lifecycle("An SDK path not set in AGP, trying to autodetect")
        }

        try {
            ndkPath.orNull?.let {
                val path = it.asFile.absolutePath
                project.logger.lifecycle("Building for Android NDK at: $path")
                args += listOf("--android-ndk", path)
            }
        } catch (_ : Exception) {
            project.logger.lifecycle("An NDK path not set in AGP, trying to autodetect")
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