plugins {
    kotlin("jvm") version "2.0.10"
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.2.2"
}

group = "io.scade.gradle.plugins"

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
            id = "io.scade.gradle.plugins.swiftpm"
            implementationClass = "io.scade.gradle.plugins.spm.SpmGradlePlugin"
            displayName = "Swift Packages for Gradle"
            description = "Adds seamless interoberability between Swift and Java/Kotlin"
            tags = listOf("Swift", "SwiftPM", "SPM", "Java/Swift", "Kotlin/Swift")
        }
    }
}