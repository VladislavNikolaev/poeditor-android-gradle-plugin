package org.gradle

import com.bq.gradle.ImportPoEditorStringsTask
import com.bq.gradle.PoEditorPluginExtension
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

class ImportFromPoEditorToStringsTest {
    @Test
    void main() {
        def project = ProjectBuilder.builder().build()
        def task = project.task('importPoEditorStrings', type: ImportPoEditorStringsTask)
        assertTrue(task instanceof ImportPoEditorStringsTask)

        // Set empty extension
        def parameters = new PoEditorPluginExtension()

        //TODO: Add your parameters for test

        project.extensions.add('poEditorPlugin', parameters)

        // Test this throws IllegalStateException
        ((ImportPoEditorStringsTask) task).importPoEditorStrings()
    }
}
