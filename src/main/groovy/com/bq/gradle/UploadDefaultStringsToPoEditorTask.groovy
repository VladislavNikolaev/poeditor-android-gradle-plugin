package com.bq.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Task that:
 * 1. downloads all strings files (every available lang) from PoEditor given a api_token and project_id.
 * 2. extract "tablet" strings to another own XML (strings with the suffix "_tablet")
 * 3. creates and saves two strings.xml files to values-<lang> and values-<lang>-sw600dp (tablet specific strings)
 *
 * Created by imartinez on 11/1/16.
 */
class UploadDefaultStringsToPoEditorTask extends DefaultTask {

    String PO_EDITOR_API_V2_UPLOAD_URL = 'https://api.poeditor.com/v2/projects/upload'

    @TaskAction
    def uploadDefaultStringsToPoEditor() {
        def apiToken = project.extensions.poEditorPlugin.api_token
        def projectId = project.extensions.poEditorPlugin.project_id
        def resDirPath = project.extensions.poEditorPlugin.res_dir_path
        def defaultLang = project.extensions.poEditorPlugin.default_lang
        uploadDefaultStringsToPoEditor(apiToken, projectId, resDirPath, defaultLang)
    }

    def uploadDefaultStringsToPoEditor(String apiToken,
                                       String projectId,
                                       String resDirPath,
                                       String defaultLang) {
        def filePath = "${resDirPath}\\values\\strings.xml"
        def file = new File(filePath)
        if(!file.exists()){
            throw new FileNotFoundException(filePath)
        }
        def date = new Date().format("dd.MM.yy", TimeZone.getDefault())
        def requestResult = ['curl', '-X', 'POST',
                             '-F', "api_token=${apiToken}",
                             '-F', "id=${projectId}",
                             '-F', "language=${defaultLang}",
                             '-F', 'updating=\"terms_translations\"',
                             '-F', "file=@\"${filePath}\"",
                             '-F', "fuzzy_trigger=\"1\"",
                             '-F', "overwrite=\"1\"",
                             '-F', "tags=\"{" +
                                     "\"obsolete\":\"obsolete_{$date}\"," +
                                     " \"new\": \"new_{$date}\"," +
                                     "}\"",
                             PO_EDITOR_API_V2_UPLOAD_URL].execute()
        println "uploadDefaultStringsToPoEditor requestResult: ${requestResult.text}"
    }

}
