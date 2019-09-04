package com.bq.gradle

import com.bq.gradle.data.ExtensionModel
import com.bq.gradle.data.TermBuilder

@SuppressWarnings(['GroovyAssignabilityCheck', 'GrMethodMayBeStatic', 'GrUnresolvedAccess'])
class FileRecords {
    private XmlParser _parser
    private def _file
    private Node _file_records
    private ExtensionModel _model
    private String _table_xml_String = """<resources>
                     <!-- Tablet strings -->
                    </resources>"""
    private def _tablet_records
    private def _tablet_notes
    private def strings_xml = 'strings.xml'
    private def name_attr = 'name'

    FileRecords(model, file) {
        _model = model
        _file = file
        _parser = new XmlParser()
        _file_records = _parser.parseText(file)

    }

    def set_file(file){
        _file = file
        _file_records = _parser.parseText(file)
    }

    static def import_strings(model, file, lang_code, create_for_tablet = false) {
        FileRecords records = new FileRecords(model, file)
                .remove_empty_nodes()
                .replace_brand_name()

        def _modifier = records.values_modifier(lang_code)
        def _folder_path = records.create_folder_path(_modifier)

        if (create_for_tablet) {
            records.replace_tabel_strings_to_his_own_records()
            def _tablet_folder_path = records.create_folder_path(_modifier, create_for_tablet)
            println "Writing ${_tablet_folder_path}/strings.xml file"
            records.write_file(_tablet_folder_path, records.tablet_records())
        }

        println "Writing ${_folder_path}/strings.xml file"
        records.write_file(_folder_path, records.file_records())

        records
    }

    static def new_terms_from_disk(model, file = null, records = new FileRecords(model, file)) {
        records.new_terms_form_disk()
    }

    def file_records() {
       return  _file_records
    }

    def write_file(folder_path, records = file_records()) {
        new File(folder_path, strings_xml).withWriter { w ->
            w << stringify(records)
        }
    }

    def open_file(folder = create_folder_path()) {
        new File("${folder.path}/${strings_xml}").text
    }

    def new_terms_form_disk(
            Node current = file_records(),
            Node from = new FileRecords(_model, open_file()).file_records()
    ) {
        def terms = []
        remove_empty_nodes(current)
        from.children().each {
            def remove = false
            current.each { item ->
                remove = remove || item.attributes()[name_attr] == it.attributes()[name_attr]
            }
            if (!remove) {
                terms << new TermBuilder().term(it.attributes()[name_attr]).content(it.value()[0]).build()
            }
            remove
        }
        terms
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
            it.@name.endsWith('_tablet')
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
            tablet_records().append(it)
        }
        this
    }

    def create_folder_path(value_modifiler = values_modifier(), is_tablet = false) {
        def folder_path
        if (is_tablet) folder_path = value_modifiler != _model.defaultLang ? "values-${value_modifiler}-sw600dp" : 'values-sw600dp'
        else folder_path = value_modifiler != _model.defaultLang ? "values-${value_modifiler}" : 'values'
        string_folder_file(folder_path)
    }

    def string_folder_file(folder_path) {
        create_dir_if_not_exists(
                new File("${_model.resDirPath}/${folder_path}"),
                folder_path
        )
    }

    def create_dir_if_not_exists(strings_folder_file, folder_path) {
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
            if (_model.keysExcludedForReplacement.contains(it.attributes()[name_attr])) {
                return
            }
            def resultValue = it.value()[0]
                    .replaceAll(_model.brandNameOld, _model.brandNameNew)
                    .replaceAll(brandNameOldUpperCase, brandNameNewUpperCase)
                    .replaceAll(brandNameOldCapitalized, brandNameNewCapitalized)
                    .getChars()
            it.setValue(resultValue)
        }
    }

    def remove_empty_nodes(Node root_node = file_records()) {
        def emptyNodes = root_node.children().findAll {
            return it.name() == "string" && it.value().size() == 0
        }
        emptyNodes.each {
            root_node.remove(it)
        }
        this
    }

    String values_modifier(String langCode = _model.defaultLang) {
        if (!langCode.contains('-')) {
            return langCode
        } else {
            String[] langParts = langCode.split('-')
            return langParts[0] + '-' + 'r' + langParts[1].toUpperCase()
        }
    }

}
