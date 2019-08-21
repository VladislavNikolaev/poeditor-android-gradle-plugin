package com.bq.gradle

import com.bq.gradle.data.ExtentionModel

import javax.xml.soap.Node

@SuppressWarnings(["GroovyAssignabilityCheck", "GrMethodMayBeStatic"])
class FileRecords {
    private XmlParser _parser
    private def _file
    private Node _file_records
    private ExtentionModel _model
    private String _table_xml_String = """<resources>
                     <!-- Tablet strings -->
                    </resources>"""
    private def _tablet_records
    private def _tablet_notes

    FileRecords(model, file = null) {
        _model = model
        _file = file
        _parser = new XmlParser()
    }

    static def import_strings(model, file, lang_code) {
        FileRecords records = new FileRecords(model, file)
                .remove_empty_nodes()
                .replace_brand_name()
                .replace_tabel_strings_to_his_own_records()

        def _file = records.file_records()
        def _tablet_file = records.tablet_records()
        def _modifier = records.create_values_modifier_from(lang_code)
        def _folder_path = records.create_folder_path(_modifier)
        def _tablet_folder_path = records.create_folder_path(_modifier, true)

        println "Writing strings.xml file"
        records.write_file(_folder_path, _file)

        println "Writing tablet strings.xml file"
        records.write_file(_tablet_folder_path, _tablet_file)

    }

    def file_records(file = _file) {
        if (_file_records == null && _file != null) {
            _file_records = _parser.parse(file)
        }
        _file_records
    }

    def write_file(folder_path, records = file_records()) {
        new File(folder_path, 'strings.xml').withWriter { w ->
            w << stringify(records)
        }
    }

    def stringify(records = file_records()) {
        StringWriter sw = new StringWriter()
        XmlNodePrinter np = new XmlNodePrinter(new PrintWriter(sw))
        np.print(records)
        sw.toString()
    }

    def replace_brand_name() {
        if (_model.shouldReplaceBrandName) {
            apply_replacement_of_values()
        }
        this
    }

    def tablet_nodes(node = file_records()) {
        if (_tablet_notes == null) _tablet_notes = node.children().findAll {
            it.@name.endsWith('_tablet', '')
        }
        _tablet_notes
    }

    def tablet_records(tablet_xml_string = _table_xml_String) {
        if (_tablet_records == null) _tablet_records = _parser.parseText(tablet_xml_string)
        _tablet_records
    }

    def replace_tabel_strings_to_his_own_records(file_records = file_records()) {
        tablet_nodes().each {
            file_records.remove(it)
            it.@name = it.@name.replace('_tablet', '')
            tablet_records() << it
        }
        this
    }

    def create_folder_path(value_modifiler, is_tablet = false) {
        def folder_path
        if (is_tablet) folder_path = value_modifiler != _model.defaultLang ? "values-${value_modifiler}-sw600dp" : "values-sw600dp"
        else folder_path = value_modifiler != _model.defaultLang ? "values-${value_modifiler}" : "values"
        folder_path
    }

    def string_folder_file(folder_path) {
        create_dir_if_not_exists(
                new File("${_model.resDirPath}/${folder_path}"),
                folder_path
        )
    }

    def create_dir_if_not_exists(strings_folder_file, folder_path) {
        strings_folder_file = string_folder_file(folder_path)
        if (!strings_folder_file.exists()) {
            println "Creating ${folder_path} folder for new language"
            def folder_created = strings_folder_file.mkdir()
            println "Folder created: ${folder_created}"
        }
        strings_folder_file
    }

    String apply_replacement_of_values(Node rootNode = file_records()) {
        def brandNameOldUpperCase = _model.brandNameOld.toUpperCase()
        def brandNameNewUpperCase = _model.brandNameNew.toUpperCase()
        def brandNameOldCapitalized = _model.brandNameOld.toLowerCase().capitalize()
        def brandNameNewCapitalized = _model.brandNameNew.toLowerCase().capitalize()
        rootNode.children().each {
            if (_model.keysExcludedForReplacement.contains(it.attributes()["name"])) {
                return
            }
            def resultValue = it.value()[0].replaceAll(brandNameOld, brandNameNew)
                    .replaceAll(brandNameOldUpperCase, brandNameNewUpperCase)
                    .replaceAll(brandNameOldCapitalized, brandNameNewCapitalized)
                    .getChars()
            it.setValue(resultValue)
        }
    }

    def remove_empty_nodes(Node root_node = file_records()) {
        def emptyNodes = root_node.children().findAll {
            it.name() == "string" && it.value().size() == 0
        }
        emptyNodes.each {
            root_node.remove(it)
        }
        this
    }

    String create_values_modifier_from(String langCode) {
        if (!langCode.contains("-")) {
            return langCode
        } else {
            String[] langParts = langCode.split("-")
            return langParts[0] + "-" + "r" + langParts[1].toUpperCase()
        }
    }

}
