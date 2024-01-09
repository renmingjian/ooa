package com.aai.iqa.parser

import com.aai.core.processManager.dataparser.OSPDefaultDataParser
import com.aai.core.processManager.model.OSPCountryType
import com.aai.core.processManager.model.OSPDocConfig
import com.aai.core.processManager.model.OSPImage
import com.aai.core.processManager.model.OSPSupportCountry
import com.aai.core.utils.hasAndNotNull
import org.json.JSONArray
import org.json.JSONObject

/**
 * Document节点数据结构，用来解析currentNode->data->nodeConfig结构
 */
class OSPDocNodeDataParser : OSPDefaultDataParser() {

    val docPageConfig = OSPDocConfig()

    override fun parse(response: String) {
        val data = JSONObject(response)
        if (data.hasAndNotNull("skipSelectCountryIfPossible")) {
            docPageConfig.skipSelectCountryIfPossible =
                data.getBoolean("skipSelectCountryIfPossible")
        }
        if (data.hasAndNotNull("skipSelectDocTypeIfPossible")) {
            docPageConfig.skipSelectDocTypeIfPossible =
                data.getBoolean("skipSelectDocTypeIfPossible")
        }
        if (data.hasAndNotNull("documentVerificationConfig")) {
            val dvcJson = data.getJSONObject("documentVerificationConfig")
            if (dvcJson.hasAndNotNull("instructionConfig")) {
                val instructionConfigJ = dvcJson.getJSONObject("instructionConfig")
                val instructionConfig = docPageConfig.documentVerificationConfig.instructionConfig
                if (instructionConfigJ.hasAndNotNull("iqa")) {
                    val iqaJ = instructionConfigJ.getJSONObject("iqa")
                    if (iqaJ.hasAndNotNull("enabled")) {
                        instructionConfig.iqa.enabled = iqaJ.getBoolean("enabled")
                    }
                }
                if (instructionConfigJ.hasAndNotNull("imageCaptureMethods")) {
                    val methodsJ = instructionConfigJ.getJSONObject("imageCaptureMethods")
                    if (methodsJ.hasAndNotNull("mobileNative")) {
                        instructionConfig.imageCaptureMethods.mobileNative =
                            methodsJ.getString("mobileNative")
                    }
                }
                if (instructionConfigJ.hasAndNotNull("enableCountriesAndIdTypes")) {
                    val array = instructionConfigJ.getJSONArray("enableCountriesAndIdTypes")
                    instructionConfig.enableCountriesAndIdTypes.addAll(parseCountry(array))
                }
            }
        }
        if (data.hasAndNotNull("pages")) {
            val pagesJSONObject = data.getJSONObject("pages")
            if (pagesJSONObject.hasAndNotNull("retry")) {
                val retryPageJSONObject = pagesJSONObject.getJSONObject("retry")
                val retryPage = docPageConfig.pages.retryPage
                if (retryPageJSONObject.hasAndNotNull("button")) {
                    retryPage.button = retryPageJSONObject.getString("button")
                }
                if (retryPageJSONObject.hasAndNotNull("content")) {
                    retryPage.content = retryPageJSONObject.getString("content")
                }
                if (retryPageJSONObject.hasAndNotNull("pageName")) {
                    retryPage.pageName = retryPageJSONObject.getString("pageName")
                }
                if (retryPageJSONObject.hasAndNotNull("subTitle")) {
                    retryPage.subTitle = retryPageJSONObject.getString("subTitle")
                }
                if (retryPageJSONObject.hasAndNotNull("headerTitle")) {
                    retryPage.headerTitle = retryPageJSONObject.getString("headerTitle")
                }
                if (retryPageJSONObject.hasAndNotNull("contentData")) {
                    val contentDataJ = retryPageJSONObject.getJSONObject("contentData")
                    val contentData = retryPage.contentData
                    if (contentDataJ.hasAndNotNull("IQA_FAILED")) {
                        contentData.IQA_FAILED = contentDataJ.getString("IQA_FAILED")
                    }
                    if (contentDataJ.hasAndNotNull("OTHER_REASON")) {
                        contentData.OTHER_REASON = contentDataJ.getString("OTHER_REASON")
                    }
                    if (contentDataJ.hasAndNotNull("MAX_RETRY_EXCEED")) {
                        contentData.MAX_RETRY_EXCEED = contentDataJ.getString("MAX_RETRY_EXCEED")
                    }
                    if (contentDataJ.hasAndNotNull("ID_FORGERY_FAILED")) {
                        contentData.ID_FORGERY_FAILED = contentDataJ.getString("ID_FORGERY_FAILED")
                    }
                    if (contentDataJ.hasAndNotNull("CERTIFICATE_FAILED")) {
                        contentData.CERTIFICATE_FAILED =
                            contentDataJ.getString("CERTIFICATE_FAILED")
                    }
                    if (contentDataJ.hasAndNotNull("AGE_COMPARISON_FAILED")) {
                        contentData.AGE_COMPARISON_FAILED =
                            contentDataJ.getString("AGE_COMPARISON_FAILED")
                    }
                    if (contentDataJ.hasAndNotNull("NUMBER_CONSISTENCY_FAILED")) {
                        contentData.NUMBER_CONSISTENCY_FAILED =
                            contentDataJ.getString("NUMBER_CONSISTENCY_FAILED")
                    }
                }
                if (retryPageJSONObject.hasAndNotNull("images")) {
                    retryPage.images.clear()
                    val array = retryPageJSONObject.getJSONArray("images")
                    for (i in 0 until array.length()) {
                        val imageObject = OSPImage()
                        val element = array.getJSONObject(i)
                        if (element.hasAndNotNull("width")) {
                            imageObject.width = element.getInt("width")
                        }
                        if (element.hasAndNotNull("imageUrl")) {
                            imageObject.imageUrl = element.getString("imageUrl")
                        }
                        if (element.hasAndNotNull("imageName")) {
                            imageObject.imageName = element.getString("imageName")
                        }
                        retryPage.images.add(imageObject)
                    }
                }
            }
            if (pagesJSONObject.hasAndNotNull("failurePage")) {
                val failurePageJ = pagesJSONObject.getJSONObject("failurePage")
                val failurePage = docPageConfig.pages.failurePage
                if (failurePageJ.hasAndNotNull("subTitle")) {
                    failurePage.subTitle = failurePageJ.getString("subTitle")
                }
                if (failurePageJ.hasAndNotNull("pageName")) {
                    failurePage.pageName = failurePageJ.getString("pageName")
                }
            }
            if (pagesJSONObject.hasAndNotNull("enableCameraPage")) {
                val cameraPageJ = pagesJSONObject.getJSONObject("enableCameraPage")
                val cameraPage = docPageConfig.pages.enableCameraPage
                if (cameraPageJ.hasAndNotNull("button")) {
                    cameraPage.button = cameraPageJ.getString("button")
                }
                if (cameraPageJ.hasAndNotNull("content")) {
                    cameraPage.content = cameraPageJ.getString("content")
                }
                if (cameraPageJ.hasAndNotNull("pageName")) {
                    cameraPage.pageName = cameraPageJ.getString("pageName")
                }
                if (cameraPageJ.hasAndNotNull("headerTitle")) {
                    cameraPage.headerTitle = cameraPageJ.getString("headerTitle")
                }
            }
            if (pagesJSONObject.hasAndNotNull("documentType")) {
                val documentTypePageJ = pagesJSONObject.getJSONObject("documentType")
                val documentTypePage = docPageConfig.pages.documentType
                if (documentTypePageJ.hasAndNotNull("pageName")) {
                    documentTypePage.pageName = documentTypePageJ.getString("pageName")
                }
                if (documentTypePageJ.hasAndNotNull("headerTitle")) {
                    documentTypePage.headerTitle = documentTypePageJ.getString("headerTitle")
                }
                if (documentTypePageJ.hasAndNotNull("subTitle")) {
                    documentTypePage.subTitle = documentTypePageJ.getString("subTitle")
                }
                if (documentTypePageJ.hasAndNotNull("skipSelectDocTypeIfPossible")) {
                    documentTypePage.skipSelectDocTypeIfPossible =
                        documentTypePageJ.getBoolean("skipSelectDocTypeIfPossible")
                }
            }
            if (pagesJSONObject.hasAndNotNull("selectCountry")) {
                val selectCountryPageJ = pagesJSONObject.getJSONObject("selectCountry")
                val selectCountryPage = docPageConfig.pages.selectCountry
                if (selectCountryPageJ.hasAndNotNull("pageName")) {
                    selectCountryPage.pageName = selectCountryPageJ.getString("pageName")
                }
                if (selectCountryPageJ.hasAndNotNull("button")) {
                    selectCountryPage.button = selectCountryPageJ.getString("button")
                }
                if (selectCountryPageJ.hasAndNotNull("subTitle")) {
                    selectCountryPage.subTitle = selectCountryPageJ.getString("subTitle")
                }
                if (selectCountryPageJ.hasAndNotNull("headerTitle")) {
                    selectCountryPage.headerTitle = selectCountryPageJ.getString("headerTitle")
                }
                if (selectCountryPageJ.hasAndNotNull("iqaEnabled")) {
                    selectCountryPage.iqaEnabled = selectCountryPageJ.getBoolean("iqaEnabled")
                }
                if (selectCountryPageJ.hasAndNotNull("skipSelectCountryIfPossible")) {
                    selectCountryPage.skipSelectCountryIfPossible =
                        selectCountryPageJ.getBoolean("skipSelectCountryIfPossible")
                }
                if (selectCountryPageJ.hasAndNotNull("cuntryCount")) {
                    selectCountryPage.cuntryCount = selectCountryPageJ.getInt("cuntryCount")
                }
                if (selectCountryPageJ.hasAndNotNull("supportCountries")) {
                    val countryArray = selectCountryPageJ.getJSONArray("supportCountries")
                    docPageConfig.pages.selectCountry.supportCountries.addAll(
                        parseCountry(
                            countryArray
                        )
                    )
                }
            }
            if (pagesJSONObject.hasAndNotNull("documentUpload")) {
                val howToUploadDocumentPageJ = pagesJSONObject.getJSONObject("documentUpload")
                val howToUploadDocumentPage = docPageConfig.pages.documentUpload
                if (howToUploadDocumentPageJ.hasAndNotNull("pageName")) {
                    howToUploadDocumentPage.pageName =
                        howToUploadDocumentPageJ.getString("pageName")
                }
                if (howToUploadDocumentPageJ.hasAndNotNull("button")) {
                    howToUploadDocumentPage.button = howToUploadDocumentPageJ.getString("button")
                }
                if (howToUploadDocumentPageJ.hasAndNotNull("headerTitle")) {
                    howToUploadDocumentPage.headerTitle =
                        howToUploadDocumentPageJ.getString("headerTitle")
                }
            }
            if (pagesJSONObject.hasAndNotNull("howToUploadDocument")) {
                val howToUploadDocumentPageJ = pagesJSONObject.getJSONObject("howToUploadDocument")
                val howToUploadDocumentPage = docPageConfig.pages.howToUploadDocument
                if (howToUploadDocumentPageJ.hasAndNotNull("pageName")) {
                    howToUploadDocumentPage.pageName =
                        howToUploadDocumentPageJ.getString("pageName")
                }
                if (howToUploadDocumentPageJ.hasAndNotNull("button")) {
                    howToUploadDocumentPage.button = howToUploadDocumentPageJ.getString("button")
                }
                if (howToUploadDocumentPageJ.hasAndNotNull("headerTitle")) {
                    howToUploadDocumentPage.headerTitle =
                        howToUploadDocumentPageJ.getString("headerTitle")
                }
                if (howToUploadDocumentPageJ.hasAndNotNull("content")) {
                    howToUploadDocumentPage.content = howToUploadDocumentPageJ.getString("content")
                }
                if (howToUploadDocumentPageJ.has("props")) {
                    val propsJ = howToUploadDocumentPageJ.getJSONObject("props")
                    val props = howToUploadDocumentPage.props
                    if (propsJ.hasAndNotNull("max")) {
                        props.max = propsJ.getInt("max")
                    }
                    if (propsJ.hasAndNotNull("min")) {
                        props.min = propsJ.getInt("min")
                    }
                    if (propsJ.hasAndNotNull("height")) {
                        props.height = propsJ.getInt("height")
                    }
                }
                if (howToUploadDocumentPageJ.hasAndNotNull("images")) {
                    howToUploadDocumentPage.images.clear()
                    val array = howToUploadDocumentPageJ.getJSONArray("images")
                    for (i in 0 until array.length()) {
                        val imageObject = OSPImage()
                        val element = array.getJSONObject(i)
                        if (element.hasAndNotNull("width")) {
                            imageObject.width = element.getInt("width")
                        }
                        if (element.hasAndNotNull("imageUrl")) {
                            imageObject.imageUrl = element.getString("imageUrl")
                        }
                        if (element.hasAndNotNull("imageName")) {
                            imageObject.imageName = element.getString("imageName")
                        }
                        howToUploadDocumentPage.images.add(imageObject)
                    }
                }
            }
        }
    }

    private fun parseCountry(countryArray: JSONArray): MutableList<OSPSupportCountry> {
        val countries = mutableListOf<OSPSupportCountry>()
        for (i in 0 until countryArray.length()) {
            val countryJson = countryArray.getJSONObject(i)
            val country = OSPSupportCountry()
            if (countryJson.hasAndNotNull("id")) {
                country.id = countryJson.getInt("id")
            }
            if (countryJson.hasAndNotNull("label")) {
                country.label = countryJson.getString("label")
            }
            if (countryJson.hasAndNotNull("country")) {
                country.country = countryJson.getString("country")
            }
            if (countryJson.hasAndNotNull("countryCode")) {
                country.countryCode = countryJson.getString("countryCode")
            }
            if (countryJson.hasAndNotNull("types")) {
                val map = mutableMapOf<String, Any?>(
                    "ID_CARD" to "id_photo_document_type_id_card",
                    "PASSPORT" to "id_photo_document_type_passport"
                )
                val typeArray = countryJson.getJSONArray("types")
                val types = country.types
                for (j in 0 until typeArray.length()) {
                    val typeJson = typeArray.getJSONObject(j)
                    val type = OSPCountryType()
                    if (typeJson.hasAndNotNull("type")) {
                        val docType = typeJson.getString("type")
                        type.type = docType
                        val key = map[docType]?.toString()
                        type.labelKey = if (key.isNullOrEmpty()) docType else key
                    }
                    if (typeJson.hasAndNotNull("pages")) {
                        val pagesArray = typeJson.getJSONArray("pages")
                        for (k in 0 until pagesArray.length()) {
                            type.pages.add(pagesArray.getString(k))
                        }
                    }
                    types.add(type)
                }
            }
            countries.add(country)
        }
        return countries
    }
}