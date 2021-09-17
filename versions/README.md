# Latest dependency versions

## Usage

In setting.gladle.kts
```kotlin
enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("versions/android.versions.toml"))
    }
  }
}

include(":app")
```

In build.gradle.kts
```
buildscript {
  dependencies {
    // https://github.com/gradle/gradle/issues/16958
    val libs = project.extensions.getByType<VersionCatalogsExtension>()
      .named("libs") as org.gradle.accessors.dm.LibrariesForLibs
    classpath(libs.androidPlugin)
    classpath(libs.kotlinPlugin)
  }
}

```

In app/build.gradle.kts
```kotlin
dependencies {
  implementation(libs.androidxCoreKtx)
  // etc
}
```
