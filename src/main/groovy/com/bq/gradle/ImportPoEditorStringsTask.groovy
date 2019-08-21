package com.bq.gradle

import com.bq.gradle.data.API
import com.bq.gradle.data.ExtensionModel
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
class ImportPoEditorStringsTask extends DefaultTask {

    @SuppressWarnings("GroovyAssignabilityCheck")
    @TaskAction
    void importPoEditorStrings() {
        def model = ExtensionModel.define(project)
        def api = new API(model)
        api.list_languages({ json ->
            json.list.code.each { lang_code ->
                if (model.contains(lang_code)) {
                    return
                }
                api.translationFileInfo({ info ->
                    api.download_translation_file(info.item, {
                        FileRecords.import_strings(model, it, lang_code)
                    })
                }, { throw it })
            }
        }, { throw it })
    }

}
