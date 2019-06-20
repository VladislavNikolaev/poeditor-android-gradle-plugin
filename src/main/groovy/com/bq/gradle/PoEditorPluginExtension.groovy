package com.bq.gradle

/**
 * Extension class that represents the needed params that will
 * be passed to the different tasks of the plugin.
 *
 * Created by imartinez on 11/1/16.
 */
class PoEditorPluginExtension {
    // PoEditor API TOKEN
    def String api_token = ""
    // PoEditor PROJECT ID
    def String project_id = ""
    // Default (and fallback) language code: i.e. "es"
    def String default_lang = "es"
    // Path to res/ directory: i.e. "${project.rootDir}/app/src/main/res"
    def String res_dir_path = ""

    def Boolean generate_tablet_res = false

    def String excluded_language_codes = ""

    def Boolean replace_brand_name = true

    def String brand_name_old = ""

    def String brand_name_new = ""

    def String keys_excluded_for_replacement = ""
}
