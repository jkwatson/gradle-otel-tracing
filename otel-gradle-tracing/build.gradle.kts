import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `maven-publish`

    id("org.jetbrains.kotlin.jvm") version "1.4.31"
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}
project.group="com.github.jkwatson"
project.version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:1.4.1-alpha")
    implementation(platform("io.opentelemetry:opentelemetry-bom:1.4.1"))
    implementation("io.opentelemetry:opentelemetry-sdk-extension-resources")
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-sdk")
    implementation("io.opentelemetry:opentelemetry-exporter-logging")
    implementation("io.opentelemetry:opentelemetry-exporter-zipkin")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp-trace")
    implementation("io.grpc:grpc-netty-shaded:1.34.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
    // Define the plugin
    val tracing by plugins.creating {
        id = "com.github.jkwatson.gradle-tracing-plugin"
        implementationClass = "io.opentelemetry.gradle.tracing.OtelTracingPlugin"
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}