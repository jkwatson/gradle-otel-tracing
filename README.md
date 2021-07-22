## Gradle OpenTelemetry Build Tracing Plugin
This plugin will trace your gradle build using OpenTelemetry APIs. 

## Usage 

### kotlin buildscript (`build.gradle.kts`/`settings.gradle.kts`):

In your `settings.gradle.kts` file, configure your plugins like this:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url="https://jitpack.io")
    }
    plugins {
        # ...
        id("com.github.jkwatson.gradle-otel-tracing") version "0.0.2"
    }
}

```

And, in your `build.gradle.kts` apply the plugin like this:

```kotlin
plugins {
    # ...
    id("com.github.jkwatson.gradle-otel-tracing")
}
```

### groovy buildscript (`build.gradle`/`settings.gradle`):

In your `settings.gradle` file, configure your plugins like this:

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = "https://jitpack.io"
        }
    }
    plugins {
        # ...
        id "com.github.jkwatson.gradle-otel-tracing" version "0.0.2"
    }
}
```

And, in your `build.gradle` apply the plugin like this:

```groovy
plugins {
    # ...
    id "com.github.jkwatson.gradle-otel-tracing"
}
```

## Configuration
Configuration of the OpenTelemetry SDK is done via the OpenTelemetry Java autoconfiguration module. 

Documentation can be found here: https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure

This plugin will use the name of your project as the OpenTelemetry `service.name` Resource attribute.