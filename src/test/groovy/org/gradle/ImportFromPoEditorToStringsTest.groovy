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
        def invalidAPICredentialsExtension = new PoEditorPluginExtension()
        invalidAPICredentialsExtension.project_id = "invalid_project_id"
        invalidAPICredentialsExtension.api_token = "invalid_api_oken"
        invalidAPICredentialsExtension.default_lang = "fake_lang"
        invalidAPICredentialsExtension.res_dir_path = "fake_path"

        project.extensions.add("poEditorPlugin", invalidAPICredentialsExtension)

        // Test this throws IllegalStateException
        ((ImportPoEditorStringsTask) task).importPoEditorStrings()
    }
}
