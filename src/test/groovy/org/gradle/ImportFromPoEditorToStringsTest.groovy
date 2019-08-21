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
        parameters.project_id = "140991"
        parameters.api_token = "ae0097676e6c8d6cc825247c43f12c58"
        parameters.default_lang = "en"
        parameters.res_dir_path = "/Users/paulhostev/Desktop"

        project.extensions.add("poEditorPlugin", parameters)

        // Test this throws IllegalStateException
        ((ImportPoEditorStringsTask) task).importPoEditorStrings()
    }
}
