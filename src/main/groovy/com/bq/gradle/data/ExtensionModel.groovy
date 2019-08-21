package com.bq.gradle.data

@SuppressWarnings("GroovyAssignabilityCheck")
class ExtensionModel {
    def apiToken = ""
    def projectId = ""
    def defaultLang = ""
    def resDirPath = ""
    boolean generateTabletRes = false

    def excludedLanguageCodes
    def shouldReplaceBrandName = false

    def keysExcludedForReplacement

    def brandNameOld
    def brandNameNew

    private ExtensionModel(project, apiToken, projectId, defaultLang, resDirPath, generateTabletRes) {
        this.apiToken = apiToken
        this.projectId = projectId
        this.defaultLang = defaultLang
        this.resDirPath = resDirPath
        this.generateTabletRes = generateTabletRes
        this.excludedLanguageCodes = project.poEditorPlugin.excluded_language_codes.split(' ')
        this.shouldReplaceBrandName = project.poEditorPlugin.replace_brand_name
        this.keysExcludedForReplacement = project.extensions.poEditorPlugin.keys_excluded_for_replacement.split(" ")
        this.brandNameOld = project.extensions.poEditorPlugin.brand_name_old
        this.brandNameNew = project.extensions.poEditorPlugin.brand_name_new
    }

    static Builder builder() throws Exception {
        new Builder()
    }

    static ExtensionModel define(project) {
        def extensions = project.extensions
        builder().project(project)
                .apiToken(extensions.poEditorPlugin.api_token)
                .projectId(extensions.poEditorPlugin.project_id)
                .defaultLang(extensions.poEditorPlugin.default_lang)
                .resDirPath(extensions.poEditorPlugin.res_dir_path)
                .generateTabletRes(extensions.poEditorPlugin.generate_tablet_res)
                .build()
    }
}

@SuppressWarnings(["GrUnresolvedAccess", "GroovyAssignabilityCheck"])
class Builder {
    private def _project
    private def _apiToken = ""
    private def _projectId = ""
    private def _defaultLang = ""
    private def _resDirPath = ""
    private def _generateTabletRes = false

    protected Builder() {}

    def project(data) {
        _project = data
        this
    }

    def apiToken(data) {
        if (data.length() == 0)
            throw new Exception('Invalid params: api_token is ""')
        _apiToken = data
        this
    }

    def projectId(data) {
        if (data.length() == 0)
            throw new Exception('Invalid params: project_id is ""')
        _projectId = data
        this
    }

    def defaultLang(data) {
        if (data.length() == 0)
            throw new Exception('Invalid params: default_lang is ""')
        _defaultLang = data
        this
    }

    def resDirPath(data) {
        if (data.length() == 0)
            throw new Exception('Invalid params: res_dir_path is ""')
        _resDirPath = data
        this
    }

    def generateTabletRes(data) {
        _generateTabletRes = data
        this
    }

    ExtensionModel build() {
        new ExtensionModel(
                _project,
                _apiToken,
                _projectId,
                _defaultLang,
                _resDirPath,
                _generateTabletRes
        )
    }
}

