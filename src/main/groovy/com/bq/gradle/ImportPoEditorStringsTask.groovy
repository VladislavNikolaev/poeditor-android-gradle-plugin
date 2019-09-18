package com.bq.gradle

import com.bq.gradle.data.API
import com.bq.gradle.data.ExtensionModel
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.bq.gradle.ExportPoEditorStringsTask.throwing

/**
 * Task that:
 * 1. downloads all strings files (every available lang) from PoEditor given a api_token and project_id.
 * 2. extract "tablet" strings to another own XML (strings with the suffix "_tablet")
 * 3. creates and saves two strings.xml files to values-<lang> and values-<lang>-sw600dp (tablet specific strings)
 *
 * Created by imartinez on 11/1/16.
 */
class ImportPoEditorStringsTask extends DefaultTask {

    @SuppressWarnings("GroovyAssignabilityCheck")
    @TaskAction
    void importPoEditorStrings() {
        def model = ExtensionModel.define(project)
        def api = new API(model)
        println 'Started importing strings from the POEditor'
        api.list_languages({ json, error ->
            throwing error
            json.list.code.each { lang_code ->
                if (!model.excludedLanguageCodes.contains(lang_code)) {
                    api.translation_file_info(lang_code, { info, err ->
                        throwing err
                        api.download_translation_file(info.item, { file, e ->
                            throwing e
                            FileRecords.import_strings(model, file, lang_code)
                            println "IMPORTING SUCCESS!"
                        })
                    })
                }
            }
        })
    }

}
