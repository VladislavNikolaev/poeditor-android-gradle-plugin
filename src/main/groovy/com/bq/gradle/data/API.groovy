package com.bq.gradle.data


import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import static com.bq.gradle.ExportPoEditorStringsTask.throwing

@SuppressWarnings(['GrMethodMayBeStatic', 'GroovyAssignabilityCheck'])
class API {

    final static String POEDITOR_API_URL = 'https://poeditor.com/api/'
    final static String POEDITOR_API_V2_URL = 'https://api.poeditor.com/v2/'
    final static String POEDITOR_API_V2_UPLOAD_URL = POEDITOR_API_V2_URL + 'projects/upload'
    final static String POEDITOR_API_V2_ADD_TERMS_URL = POEDITOR_API_V2_URL + 'terms/add'
    final static String POEDITOR_API_V2_LANG_UPDATE_URL = POEDITOR_API_V2_URL + 'languages/update'

    ExtensionModel model

    API(ExtensionModel model) {
        this.model = model
    }

    def default_lang_update(terms_list, callback) {
        add_terms(terms_list, { res, err ->
            throwing err
            languages_update(model.defaultLang, terms_list, callback)
        })

    }

    def add_terms(term_list, callback) {
        def terms_single_list = single_terms term_list
        def json = new JsonBuilder(terms_single_list).toPrettyString()
        Https.post(
                ['curl', '-X', 'POST',
                 '-d', "api_token=${model.apiToken}",
                 '-d', "id=${model.projectId}",
                 '-d', "data=${json}",
                 POEDITOR_API_V2_ADD_TERMS_URL],
                callback)
    }

    def languages_update(leng_code, term_list, Callback callback) {
        def json = new JsonBuilder(term_list).toPrettyString()
        Https.post(
                ['curl', '-X', 'POST',
                 '-d', "api_token=${model.apiToken}",
                 '-d', "id=${model.projectId}",
                 '-d', "language=${leng_code}",
                 '-d', "data=${json}",
                 POEDITOR_API_V2_LANG_UPDATE_URL],
                callback
        )
    }

    def list_languages(Callback callback) {
        Https.post(
                ['curl', '-X', 'POST',
                 '-d', "api_token=${model.apiToken}",
                 '-d', 'action=list_languages',
                 '-d', "id=${model.projectId}",
                 POEDITOR_API_URL],
                callback
        )
    }

    def translation_file_info(language, Callback callback) {
        Https.post(
                ['curl', '-X', 'POST',
                 '-d', "api_token=${model.apiToken}",
                 '-d', 'action=export',
                 '-d', "id=${model.projectId}",
                 '-d', 'type=android_strings',
                 '-d', "language=${language}",
                 POEDITOR_API_URL],
                callback
        )
    }

    def download_translation_file(String url, Callback callback) {
        Https.download(url, callback)
    }

    private List<TermSingle> single_terms(List<Term> terms) {
        List<TermSingle> singles = new ArrayList<TermSingle>()
        terms.each {
            singles.add(new TermSingle(
                    term: it.term
            ))
        }
        return singles
    }

}

class Https {
    def _json
    def _process
    def _response
    def _json_slurper

    private Https(process) {
        _json_slurper = new JsonSlurper()
        _process = process
    }

    static def post(process, Callback callback) {
        def http = new Https(process)
        callback.onResult(http.json(), http.check())
    }

    static def download(url, Callback callback) {
        def http = new Https(['curl', '-X', 'GET', url])
        callback.onResult(http.response(), null)
    }

    private def response(process = _process) {
        if (_response == null) _response = process.execute().text
        _response
    }

    private def json(text = response()) {
        if (_json == null) _json = _json_slurper.parseText(text)
        _json
    }

    private def check(json = json()) {
        def code = json.response.code
        if (code != "200") {
            return new HttpsException(
                    code,
                    "An error occurred while trying to export from PoEditor API: \n\n" + json.toString()
            )
        }
        null
    }
}

class HttpsException extends IllegalStateException {
    def code

    HttpsException(code, message) {
        super(message)
        this.code = code
    }
}
