package com.bq.gradle

import groovy.json.JsonSlurper
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

    String POEDITOR_API_V2_UPLOAD_URL = 'https://api.poeditor.com/v2/projects/upload'
    String POEDITOR_API_URL = 'https://poeditor.com/api/'

    @TaskAction
    def importPoEditorStrings() {

        // Check if needed extension and parameters are set
        def apiToken = ""
        def projectId = ""
        def defaultLang = ""
        def resDirPath = ""
        def generateTabletRes = false

        try {
            apiToken = project.extensions.poEditorPlugin.api_token
            projectId = project.extensions.poEditorPlugin.project_id
            defaultLang = project.extensions.poEditorPlugin.default_lang
            resDirPath = project.extensions.poEditorPlugin.res_dir_path
            generateTabletRes = project.extensions.poEditorPlugin.generate_tablet_res

            if (apiToken.length() == 0)
                throw new Exception('Invalid params: api_token is ""')
            if (projectId.length() == 0)
                throw new Exception('Invalid params: project_id is ""')
            if (defaultLang.length() == 0)
                throw new Exception('Invalid params: default_lang is ""')
            if (resDirPath.length() == 0)
                throw new Exception('Invalid params: res_dir_path is ""')

        } catch (Exception e) {
            throw new IllegalStateException(
                    "You shoud define in your build.gradle: \n\n" +
                            "poEditorPlugin.api_token = <your_api_token>\n" +
                            "poEditorPlugin.project_id = <your_project_id>\n" +
                            "poEditorPlugin.default_lang = <your_default_lang> \n" +
                            "poEditorPlugin.res_dir_path = <your_res_dir_path> \n\n "
                            + e.getMessage()
            )
        }

        uploadDefaultStringsToPoEditor(apiToken, projectId, resDirPath, defaultLang)

        // Retrieve available languages from PoEditor
        def jsonSlurper = new JsonSlurper()
        def langs = ['curl', '-X', 'POST', '-d', "api_token=${apiToken}", '-d', 'action=list_languages', '-d', "id=${projectId}", POEDITOR_API_URL].execute()
        def langsJson = jsonSlurper.parseText(langs.text)

        // Check if the response was 200
        if (langsJson.response.code != "200") {
            throw new IllegalStateException(
                    "An error occurred while trying to export from PoEditor API: \n\n" +
                            langsJson.toString()
            )
        }

        // Iterate over every available language
        langsJson.list.code.each {
            // Retrieve translation file URL for the given language
            println "Retrieving translation file URL for language code: ${it}"
            // TODO curl may not be installed in the host SO. Add a safe check and, if curl is not available, stop the process and print an error message
            def translationFileInfo = ['curl', '-X', 'POST', '-d', "api_token=${apiToken}", '-d', 'action=export', '-d', "id=${projectId}", '-d', 'type=android_strings', '-d', "language=${it}", 'https://poeditor.com/api/'].execute()
            def translationFileInfoJson = jsonSlurper.parseText(translationFileInfo.text)
            def translationFileUrl = translationFileInfoJson.item
            // Download translation File in "Android Strings" XML format
            println "Downloading file from Url: ${translationFileUrl}"
            def translationFile = ['curl', '-X', 'GET', translationFileUrl].execute()

            // Post process the downloaded XML:
            def translationFileText = translationFile.text

            // Extract tablet strings to a separate strings XML
            def translationFileRecords = new XmlParser().parseText(translationFileText)
            removeEmptyNodes(translationFileRecords)
            def tabletNodes = translationFileRecords.children().findAll {
                it.@name.endsWith('_tablet')
            }
            String tabletXmlString = """
                    <resources>
                     <!-- Tablet strings -->
                    </resources>"""
            def tabletRecords = new XmlParser().parseText(tabletXmlString)
            tabletNodes.each {
                translationFileRecords.remove(it)
                it.@name = it.@name.replace("_tablet", "")
                tabletRecords.append(it)
            }

            // Build final strings XMLs ready to be written to files
            StringWriter sw = new StringWriter()
            XmlNodePrinter np = new XmlNodePrinter(new PrintWriter(sw))
            np.print(translationFileRecords)
            def curatedStringsXmlText = sw.toString()

            StringWriter tabletSw = new StringWriter()
            XmlNodePrinter tabletNp = new XmlNodePrinter(new PrintWriter(tabletSw))
            tabletNp.print(tabletRecords)
            def curatedTabletStringsXmlText = tabletSw.toString()

            // If language folders doesn't exist, create it (both for smartphones and tablets)
            // TODO investigate if we can infer the res folder path instead of passing it using poEditorPlugin.res_dir_path

            def valuesModifier = createValuesModifierFromLangCode(it)
            def valuesFolder = valuesModifier != defaultLang ? "values-${valuesModifier}" : "values"
            File stringsFolder = new File("${resDirPath}/${valuesFolder}")
            if (!stringsFolder.exists()) {
                println 'Creating strings folder for new language'
                def folderCreated = stringsFolder.mkdir()
                println "Folder created: ${folderCreated}"
            }
            def tabletValuesFolder = valuesModifier != defaultLang ? "values-${valuesModifier}-sw600dp" : "values-sw600dp"
            File tabletStringsFolder = new File("${resDirPath}/${tabletValuesFolder}")
            if (!tabletStringsFolder.exists()) {
                println 'Creating tablet strings folder for new language'
                def tabletFolderCreated = tabletStringsFolder.mkdir()
                println "Folder created: ${tabletFolderCreated}"
            }

            // TODO delete existing strings.xml files

            // Write downloaded and post-processed XML to files
            println "Writing strings.xml file"
            new File(stringsFolder, 'strings.xml').withWriter { w ->
                w << curatedStringsXmlText
            }

            if (!generateTabletRes) {
                return
            }
            println "Writing tablet strings.xml file"
            new File(tabletStringsFolder, 'strings.xml').withWriter { w ->
                w << curatedTabletStringsXmlText
            }
        }
    }

    String removeEmptyNodes(Node rootNode) {
        def emptyNodes = rootNode.children().findAll {
            return it.name() == "string" && it.value().size() == 0
        }

        emptyNodes.each {
            rootNode.remove(it)
        }

        return null
    }

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
                             POEDITOR_API_V2_UPLOAD_URL].execute()
        println requestResult
    }

    /**
     * Creates values file modifier taking into account specializations (i.e values-es-rMX for Mexican)
     * @param langCode
     * @return proper values file modifier (i.e. es-rMX)
     */
    String createValuesModifierFromLangCode(String langCode) {
        if (!langCode.contains("-")) {
            return langCode
        } else {
            String[] langParts = langCode.split("-")
            return langParts[0] + "-" + "r" + langParts[1].toUpperCase()
        }
    }

}
