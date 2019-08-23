package org.gradle

import com.bq.gradle.ExportPoEditorStringsTask
import com.bq.gradle.PoEditorPluginExtension
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

class ExportFromPoEditorToStringsTest {
    @Test
    void main() {
        def project = ProjectBuilder.builder().build()
        def task = project.task('importPoEditorStrings', type: ExportPoEditorStringsTask)
        assertTrue(task instanceof ExportPoEditorStringsTask)

        // Set empty extension
        def parameters = new PoEditorPluginExtension()

        //TODO: Add your parameters for test

        project.extensions.add('poEditorPlugin', parameters)

        // Test this throws IllegalStateException
        ((ExportPoEditorStringsTask) task).exportPoEditorStrings()
    }
}
