
plugins {
    kotlin("jvm") version "2.0.10"
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.2.2"
}

group = "io.scade.gradle.plugins.android"
version = "1.0.1"

repositories {
    mavenLocal()
    mavenCentral()
    google()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
    implementation("com.android.tools.build:gradle:8.6.0")

    implementation("io.scade.gradle.plugins:spm-gradle:1.0.1")
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    website = "https://github.com/scade-platform/spm-gradle-plugin.git"
    vcsUrl = "https://github.com/scade-platform/spm-gradle-plugin.git"
    plugins {
        create("swiftpm") {
            id = "io.scade.gradle.plugins.android.swiftpm"
            implementationClass = "com.scade.gradle.plugins.android.spm.SpmGradleAndroidPlugin"
            displayName = "Swift Packages for Android-Gradle"
            description = "Adds seamless interoberability between Swift and Java/Kotlin into Android applications"
            tags = listOf("Swift", "SwiftPM", "SPM", "Java/Swift", "Kotlin/Swift", "Android", "SwiftForAndroid")
        }
    }
}