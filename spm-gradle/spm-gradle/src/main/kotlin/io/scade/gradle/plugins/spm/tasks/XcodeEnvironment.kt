package io.scade.gradle.plugins.spm.tasks

import org.gradle.process.ExecSpec
import java.io.File

internal fun ExecSpec.configureXcode(xcodePath: String?) {
    if (xcodePath == null) return

    environment("SCADE_XCODE", xcodePath)
    environment("DEVELOPER_DIR", File(xcodePath, "Contents/Developer").absolutePath)
}
