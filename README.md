# Swift Packages for Gradle

The Swift Package Manager Plugin provides tasks to build swift packages, generate Java bridgings and assembling final products. 

## Description         

The projects consists of two plugins: the `spm-gradle` for the integration into standard Java/Kotlin Gradle builds and the `spm-gradle-android` when building for the Android platform. 
       

## Getting Started

### Dependencies

-  Swift Toolchain ([https://www.swift.org](url))
-  Swift Toolchain for Android ([https://www.swift-android.com](url))

### Usage

#### Applying plugins

To apply one of the plugins, add the plugin's id into the plugins block in the build file `build.gradle.kts`:

`spm-gradle`
```kotlin
plugins { 
    id("io.scade.gradle.plugins.swiftpm") version "1.0.2"
}
```

`spm-gradle-android`
```kotlin
plugins {
    id("io.scade.gradle.plugins.android.swiftpm") version "1.0.2"
}
```

#### Plugins configuration

For both plugins add the following configuration section to the build file `build.gradle.kts`:

```kotlin
swiftpm { 
    // Path to the folder containing Package.swift
    path = file("<PACKAGE LOCATION>")
    // Name of the package's product 
    product = "<PRODUCT NAME>"
    
    // Optional properties
    
    // Java version (8, 9, 11, ...) used for the generated code compatibility
    // Example: set compatibility with Java 8
    javaVersion = 8 // (default: Java 11)
    
    // Platform configuration including custom toolchain path
    // Example: a custom path to the Android toolchain    
    platforms = listOf(
        TargetPlatform.Android(file("<PATH TO THE TOOLCHAIN LOCATION>"))
    )
}
```

**Note:** both plugins only support dynamic library products

## Development

### Local Installation

Both plugins can be published into the local Maven repository by executing

```shell
./gradlew publishAllToMavenLocal 
```

In order to use locally installed plugins in your applications, 
add the local Maven repository to the plugin management section in the Gradle settings file `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal() 
    }
}
```

## License

This project is licensed under the Apache 2.0 License - see the LICENSE.md file for details