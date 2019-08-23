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
class ExportPoEditorStringsTask extends DefaultTask {

    @SuppressWarnings("GroovyAssignabilityCheck")
    @TaskAction
    void exportPoEditorStrings() {
        def model = ExtensionModel.define(project)
        def api = new API(model)
        api.translation_file_info(model.defaultLang, { info, err ->
            throwing err
            api.download_translation_file(info.item, { file, e ->
                throwing e
                def terms = FileRecords.new_terms_from_disk(model, file)
                if (terms != []) {
                    api.default_lang_update(terms, { response, error ->
                        throwing error
                        println(response)
                    })
                } else println 'Nothing export to the POEditor'
            })
        })
    }

    def throwing(error) { if (error != null) println error.message }

}
