package org.gradle

import com.bq.gradle.ImportPoEditorStringsTask
import com.bq.gradle.PoEditorPluginExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import javax.sql.rowset.spi.XmlReader

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * ImportPoEditorStrings task test.
 *
 * Created by imartinez on 11/1/16.
 */
class ImportPoEditorStringsTaskTest {
    @Test
    public void canAddTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('importPoEditorStrings', type: ImportPoEditorStringsTask)
        assertTrue(task instanceof ImportPoEditorStringsTask)
    }

    @Test(expected=IllegalStateException.class)
    public void testExecutedWithoutNeededExtensionThrowsException() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('importPoEditorStrings', type: ImportPoEditorStringsTask)
        assertTrue(task instanceof ImportPoEditorStringsTask)

        // No extension is set

        // Test this throws IllegalStateException
        ((ImportPoEditorStringsTask)task).importPoEditorStrings()
    }

    @Test(expected=IllegalStateException.class)
    public void testExecutedWithEmptyExtensionThrowsException() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('importPoEditorStrings', type: ImportPoEditorStringsTask)
        assertTrue(task instanceof ImportPoEditorStringsTask)

        // Set empty extension
        def emptyExtension = new PoEditorPluginExtension()
        project.extensions.add("poEditorPlugin", emptyExtension)

        // Test this throws IllegalStateException
        ((ImportPoEditorStringsTask)task).importPoEditorStrings()
    }

    @Test(expected=IllegalStateException.class)
    public void testExecutedWithInvalidAPICredentialsThrowsException() {
        Project project = ProjectBuilder.builder().build()
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
        ((ImportPoEditorStringsTask)task).importPoEditorStrings()
    }

    @Test
    public void testCreateValuesModifierFromLangCodeWithNormalLangCode() throws Exception {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('importPoEditorStrings', type: ImportPoEditorStringsTask)

        assertEquals('es',
                ((ImportPoEditorStringsTask)task).createValuesModifierFromLangCode('es'))
    }

    @Test
    public void testCreateValuesModifierFromLangCodeWithSpecializedLangCode() throws Exception {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('importPoEditorStrings', type: ImportPoEditorStringsTask)

        assertEquals('es-rMX',
                ((ImportPoEditorStringsTask)task).createValuesModifierFromLangCode('es-mx'))
    }
}