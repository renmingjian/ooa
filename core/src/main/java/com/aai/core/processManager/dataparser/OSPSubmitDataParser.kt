package com.aai.core.processManager.dataparser

import com.aai.core.OSPSdk
import com.aai.core.utils.OSPLog
import com.aai.core.utils.hasAndNotNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

/**
 * 解析submit接口数据
 * submit接口主要是返回字符串资源，客户端保存该资源，当用户选择切换语言的时候，要从对应的语言中获取字符串展示
 */
class OSPSubmitDataParser : OSPDefaultDataParser() {

    private var i18n: JSONObject? = null
    private var supportedLanguages: MutableList<String> = mutableListOf()
    private var languageStrings: JSONObject? = null
    private var assetsLanguage: JSONObject? = null

    var local = Locale.getDefault().country.lowercase()
    var themeId: Int? = null
    var tenantId: Int? = null
    var journeyId: Int? = null

    override fun parse(response: String) {
        OSPLog.log("Current Local: $local")
        val data = parseResponse(response)
        val jsonObject = JSONObject(data)
        if (jsonObject.hasAndNotNull("themeId")) {
            themeId = jsonObject.getInt("themeId")
        }
        if (jsonObject.hasAndNotNull("tenantId")) {
            tenantId = jsonObject.getInt("tenantId")
        }
        if (jsonObject.hasAndNotNull("journeyId")) {
            journeyId = jsonObject.getInt("journeyId")
        }
        if (jsonObject.hasAndNotNull("i18n")) {
            val i18nObject = jsonObject.optJSONObject("i18n")
            if (i18nObject != null) {
                if (i18nObject.hasAndNotNull("supportedLanguages")) {
                    val array = i18nObject.optJSONArray("supportedLanguages")
                    array?.let {
                        try {
                            for (i in 0 until it.length()) {
                                val element = it.getString(i)
                                supportedLanguages.add(element)
                            }
                            val contains = supportedLanguages.any { language ->
                                language.equals(
                                    local,
                                    ignoreCase = true
                                )
                            }
                            OSPLog.log("contains = $contains, supportedLanguages = $supportedLanguages, local = $local")
                            if (!contains) {
                                local =
                                    if (supportedLanguages.isNotEmpty()) supportedLanguages[0] else "en"
                                assetsLanguage = getTranslationsForCountry(local)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }

                if (i18nObject.hasAndNotNull("data")) {
                    i18n = i18nObject.optJSONObject("data")
                }
            }
        }
    }

    /**
     * 根据当前选择的语言环境获取接口中对应的语言的字符串对象
     */
    private fun getLanguageStringsWithLocal(local: String): JSONObject? {
        if (languageStrings != null) return languageStrings
        i18n?.let {
            if (it.has(local.lowercase())) {
                languageStrings = it.getJSONObject(local.lowercase())
            }
        }
        return languageStrings
    }

    /**
     * 根据字符串的key获取字符串，先从接口取，接口取不到，从本地取。但是本地和接口定义的key不一致。为什么不定义为一致：
     * 1.接口的key大多数都不见名知意
     * 2.接口的key很多带uuid，uuid为随机生成，app端无法统一
     *
     * @param key 在submit接口中的key
     * @param assetsKey 在本地字符串中的key
     */
    fun getValueWithKey(key: String?, assetsKey: String?): String? =
        getValueWithKeyAndLocal(local, key, assetsKey)

    /**
     * 根据字符串的key以及选择的语言环境获取字符串
     */
    fun getValueWithKeyAndLocal(local: String, key: String?, assetsKey: String?): String? {
        OSPLog.log("currentLocal: $local, key = $key")
        val languageStrings = getLanguageStringsWithLocal(local.lowercase())
        var value: String? = null
        languageStrings?.let { obj ->
            key?.let {
                if (obj.hasAndNotNull(it)) {
                    value = obj.getString(it)
                    OSPLog.log("original text = $value")
                    value = extractTextFromHtml(value)
                    OSPLog.log("result text = $value")
                }
            }
        }
        if (value == null) {
            OSPLog.log("getValueFromAssets, key = $key")
            value = getValueFromAssets(assetsKey)
        }
        return value
    }

    /**
     * 从assets目录下找
     * 场景：有一些文案在接口是没有的，需要从本地取，但是如果根据系统设置可能找不到对应的语言。例如：系统选择的是日语，
     * 接口不支持日语，然后从接口拿到支持的最高优先级是印尼语，此时所有的文案都需要是印尼语，但是系统语言是日语，那么
     * 根据R.string.xxx拿到的就是日语，然而应该显示印尼语才行
     */
    fun getValueFromAssets(key: String?): String? {
        if (assetsLanguage != null && key != null) {
            if (assetsLanguage!!.hasAndNotNull(key)) {
                OSPLog.log("key = $key, assetsLanguage = $assetsLanguage")
                return assetsLanguage!!.getString(key)
            }
        }

        return getValueFromXML(key)
    }

    /**
     * 如果本地找不到，则从strings.xml中找
     */
    fun getValueFromXML(key: String?): String? {
        if (key.isNullOrEmpty()) return null
        val context = OSPSdk.instance.options.context
        val resId = context.resources.getIdentifier(key, "string", context.packageName)

        return if (resId != 0) {
            context.getString(resId)
        } else {
            null
        }
    }

    /**
     * 文案是HTML格式的富文本，这里给干掉HTML标签
     */
    private fun extractTextFromHtml(html: String?): String? {
        if (html.isNullOrEmpty()) return html
        // 正则表达式移除标签
        var text = html.replace(Regex("<[^>]*>"), "")

        // 替换常见的HTML实体
        text = text.replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
        return text
    }

    private fun getTranslationsForCountry(countryCode: String): JSONObject? {
        OSPLog.log("country code = $countryCode")
        try {
            // 从assets文件夹读取translations.json文件。该文件是在编译期间动态编译出来的
            OSPSdk.instance.options.context.assets.open("osp_languages.json").use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)

                // 将读取的内容转换为字符串
                val json = String(buffer, Charsets.UTF_8)
                val obj = JSONObject(json)

                // 获取特定国家代码的翻译
                return obj.getJSONObject(countryCode.lowercase())
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }
        return null
    }

    fun getAssetsLanguageObj(): JSONObject? = assetsLanguage

}