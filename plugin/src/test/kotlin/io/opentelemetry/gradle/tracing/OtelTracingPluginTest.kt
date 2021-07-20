/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package io.opentelemetry.gradle.tracing

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * A simple unit test for the 'io.opentelemetry.gradle.tracing.greeting' plugin.
 */
class OtelTracingPluginTest {
    @Test fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.opentelemetry.gradle.tracing.greeting")

        // Verify the result
        assertNotNull(project.tasks.findByName("greeting"))
    }
}
