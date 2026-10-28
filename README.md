# Swift Packages for Gradle

The Swift Package Manager Plugin provides tasks to build swift packages, generate Java bridgings and assembling final products. 

## Description         

The projects consists of two plugins: the `spm-gradle` for the integration into standard Java/Kotlin Gradle builds and the `spm-gradle-android` when building for the Android platform. 
       

## Getting Started

### Dependencies

-  [Swift Toolchain](https://www.swift.org)
-  [Swift Toolchain for Android](https://www.swift-android.com)

### Usage

#### Applying plugins

To apply one of the plugins, add the plugin's id into the plugins block in the build file `build.gradle.kts`:

`spm-gradle`
```kotlin
plugins { 
    id("io.scade.gradle.plugins.swiftpm") version "1.0.3"
}
```

`spm-gradle-android`
```kotlin
plugins {
    id("io.scade.gradle.plugins.android.swiftpm") version "1.0.3"
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
    
    // OPTIONAL PROPERTIES
    
    // Java version (8, 9, 11, ...) used for the generated code compatibility
    // Example: set compatibility with Java 8
    javaVersion = 8 // (default: Java 11)
    
    // Platform configuration including custom toolchain path
    // Example: a custom path to the Android toolchain    
    platforms = listOf(
        TargetPlatform.Android(
            // List of build architectures
            archs = listOf("<armeabi-v7a | arm64-v8a | x86 | x86_64>"),
            // Path to a custom toolchain location
            toolchain = file("<PATH TO THE TOOLCHAIN LOCATION>"))
    )
    
    // Configuration for the Scade Build Tool (scd)
    
    // Custom binary location     
    scd = file("<SCD BINARY LOCATION>")
    // Auto-update for the scd build tool
    scdAutoUpdate = true
    // List of custom options passed to the scd build tool
    scdOptions = listOf("<OPTIONS>")
}
```

**Note:** both plugins only support dynamic library products

### Selecting Xcode

Use `xcode` to build a project with a specific Xcode installation without
changing the system-wide Xcode selection:

```kotlin
swiftpm {
    xcode = file("/Applications/Xcode_26.6.app")
}
```

This can help when a newer Xcode includes a Swift version that your Android
toolchain does not yet support. Android builds also use the installed Swift
toolchain on your Mac, so selecting a compatible Xcode can resolve the mismatch
while other projects continue using the newer version.

For a machine-specific selection, set the path in the Android project's ignored
`local.properties` file instead:

```properties
swift.xcode=/Applications/Xcode_26.6.app
```

An explicit `swiftpm.xcode` value takes precedence over `swift.xcode`. If neither
is set, the default Xcode selection is used. After changing the setting, sync
Gradle in Android Studio and build normally.

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

This project is licensed under the Apache 2.0 License - see the LICENSE file for details