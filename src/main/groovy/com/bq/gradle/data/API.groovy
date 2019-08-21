package com.bq.gradle.data

import groovy.json.JsonSlurper

class API {

    final static String POEDITOR_API_V2_UPLOAD_URL = 'https://api.poeditor.com/v2/projects/upload'
    final static String POEDITOR_API_URL = 'https://poeditor.com/api/'

    def model

    API(ExtensionModel model) {
        this.model = model
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

    static def post(process, callback) {
        def http = new Https(process)
        def exception = http.check()
        if (exception == null) callback.onSuccess(http.json())
        else callback.onError(http.check())
    }

    static def download(url, callback) {
        def http = new Https(['curl', '-X', 'GET', url])
        def exception = http.check()
        if (exception == null) callback.onSuccess(http.response())
        else callback.onError(http.check())
    }

    private def response(process = _process) {
        if (_response == null) _response = process.execute().text
        _response
    }

    private def json(text = _response) {
        if (_json == null) _json = json_slurper.parseText(text)
        _json
    }

    private def check(response = response()) {
        def code = response.response.code
        if (code != "200") {
            return new HttpsException(
                    code,
                    "An error occurred while trying to export from PoEditor API: \n\n" + json().toString()
            )
        }
        null
    }
}

interface Callback {
    def onSuccess(json, exception)

    def onError(exception)
}

class HttpsException extends IllegalStateException {
    def code

    HttpsException(code, message) {
        super(message)
        this.code = code
    }
}