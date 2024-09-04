plugins {
    kotlin("jvm") version "2.0.10"
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.2.2"
}

group = "com.scade.gradle.plugins"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    website = "https://github.com/scade-platform/spm-gradle-plugin.git"
    vcsUrl = "https://github.com/scade-platform/spm-gradle-plugin.git"
    plugins {
        create("swiftpm") {
            id = "com.scade.gradle.plugins.swiftpm"
            displayName = "Gradle plugin for Swift packages"
            description = "Adds seamless interoberability between Swift and Java/Kotlin"
            tags = listOf("Swift", "SwiftPM", "SPM", "Java/Swift", "Kotlin/Swift")
            implementationClass = "com.scade.gradle.plugins.spm.SpmGradlePlugin"
        }
    }
}
