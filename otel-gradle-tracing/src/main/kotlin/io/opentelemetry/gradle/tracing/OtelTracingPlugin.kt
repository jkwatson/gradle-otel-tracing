package io.opentelemetry.gradle.tracing

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtraPropertiesExtension

class OtelTracingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val openTelemetry: OpenTelemetrySdk = initializeOpenTelemetry(project)

        val tracer: Tracer = openTelemetry.getTracer("opentelemetry-build")
        val rootProjectSpan: Span = tracer.spanBuilder(project.displayName).startSpan()
        project.gradle.addBuildListener(object : BuildListener {
            override fun settingsEvaluated(settings: Settings) {}

            override fun projectsLoaded(gradle: Gradle) {}

            override fun projectsEvaluated(gradle: Gradle) {}

            override fun buildFinished(result: BuildResult) {
                if (result.failure != null) {
                    rootProjectSpan.setStatus(StatusCode.ERROR)
                    rootProjectSpan.recordException(result.failure!!)
                }
                rootProjectSpan.end()
                openTelemetry.sdkTracerProvider.forceFlush()
            }
        })
        val spansByTask: MutableMap<Task, Span> = mutableMapOf()
        project.gradle.taskGraph.beforeTask {
            val projectSpan: Span
            val taskProject = it.project
            if (taskProject.extra.has("projectSpan")) {
                projectSpan = taskProject.extra["projectSpan"] as Span
            } else {
                projectSpan = tracer.spanBuilder(taskProject.displayName)
                    .setParent(Context.current().with(rootProjectSpan))
                    .startSpan()
                taskProject.extra["projectSpan"] = projectSpan
                //todo: would be better to end this at the end of the last task...but how?
                projectSpan.end()
            }
            val span = tracer.spanBuilder(taskProject.displayName + " : " + it.name)
                .setParent(Context.current().with(projectSpan))
                .startSpan()
            spansByTask[it] = span
        }
        project.gradle.taskGraph.afterTask {
            val span = spansByTask[it]
            if (span != null) {
                val failure = it.state.failure
                if (failure != null) {
                    span.setStatus(StatusCode.ERROR)
                    span.recordException(failure)
                    span.setAttribute("error.message", failure.message ?: "None")
                    if (failure.cause != null) {
                        span.setAttribute("error.cause", failure.cause?.message ?: "Unknown")
                    }
                }
                span.end()
                spansByTask.remove(it)
            }
        }

    }

    private fun initializeOpenTelemetry(project: Project): OpenTelemetrySdk {
        System.setProperty("otel.service.name", project.name + " Build")
        val sdk = OpenTelemetrySdkAutoConfiguration.initialize()
        //this is a super-hack to undo what the auto-configure module does by default, otherwise the global pollutes
        //the gradle daemon's JVM state.
        GlobalOpenTelemetry.resetForTest()
        return sdk
    }
}
private val ExtensionAware.extra: ExtraPropertiesExtension
    get() = extensions.extraProperties
