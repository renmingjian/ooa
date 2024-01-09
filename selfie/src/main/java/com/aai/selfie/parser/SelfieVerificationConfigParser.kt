package com.aai.selfie.parser

import com.aai.core.processManager.dataparser.OSPDataParser
import com.aai.core.processManager.model.OSPImage
import com.aai.core.processManager.model.OSPSelfieConfig
import com.aai.core.processManager.model.OSPSelfieVerificationConfig
import com.aai.core.utils.hasAndNotNull
import org.json.JSONException
import org.json.JSONObject

/**
 * 适用于nodeCode为FACE_PHOTO的2D和3D数据结构使用
 * 如果nodeCode为SELFIE_VERIFICATION，则需要使用另外一个数据解析器[SelfieVerificationConfigParser]
 */
class SelfieVerificationConfigParser : OSPDataParser {

    val ospSelfieConfig = OSPSelfieVerificationConfig()

    override fun parse(response: String) {
        val jsonObject = JSONObject(response)
        if (jsonObject.hasAndNotNull("selfieType")) {
            ospSelfieConfig.selfieType = jsonObject.getString("selfieType")
        }
        if (jsonObject.hasAndNotNull("displayLogo")) {
            ospSelfieConfig.displayLogo = jsonObject.getBoolean("displayLogo")
        }
        if (jsonObject.hasAndNotNull("pages")) {
            val pagesJSONObject = jsonObject.getJSONObject("pages")
            if (pagesJSONObject.hasAndNotNull("retryPage")) {
                val retryPageJSONObject = pagesJSONObject.getJSONObject("retryPage")
                val retryPage = ospSelfieConfig.pages.retryPage
                if (retryPageJSONObject.hasAndNotNull("text")) {
                    retryPage.text = retryPageJSONObject.getString("text")
                }
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
                if (retryPageJSONObject.hasAndNotNull("iconText_1")) {
                    retryPage.iconText_1 = retryPageJSONObject.getString("iconText_1")
                }
                if (retryPageJSONObject.hasAndNotNull("iconText_2")) {
                    retryPage.iconText_2 = retryPageJSONObject.getString("iconText_2")
                }
                if (retryPageJSONObject.hasAndNotNull("headerTitle")) {
                    retryPage.headerTitle = retryPageJSONObject.getString("headerTitle")
                }
            }
            if (pagesJSONObject.hasAndNotNull("failurePage")) {
                val failurePageJ = pagesJSONObject.getJSONObject("failurePage")
                val failurePage = ospSelfieConfig.pages.failurePage
                if (failurePageJ.hasAndNotNull("subTitle")) {
                    failurePage.subTitle = failurePageJ.getString("subTitle")
                }
            }
            if (pagesJSONObject.hasAndNotNull("successPage")) {
                val successPageJ = pagesJSONObject.getJSONObject("successPage")
                val successPage = ospSelfieConfig.pages.successPage
                if (successPageJ.hasAndNotNull("subTitle")) {
                    successPage.subTitle = successPageJ.getString("subTitle")
                }
            }
            if (pagesJSONObject.hasAndNotNull("enableCameraPage")) {
                val cameraPageJ = pagesJSONObject.getJSONObject("enableCameraPage")
                val cameraPage = ospSelfieConfig.pages.enableCameraPage
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
            if (pagesJSONObject.hasAndNotNull("takeSelfiePhotoPage")) {
                val takeSelfiePhotoPageJ = pagesJSONObject.getJSONObject("takeSelfiePhotoPage")
                val takeSelfiePhotoPage = ospSelfieConfig.pages.takeSelfiePhotoPage
                if (takeSelfiePhotoPageJ.hasAndNotNull("pageName")) {
                    takeSelfiePhotoPage.pageName = takeSelfiePhotoPageJ.getString("pageName")
                }
                if (takeSelfiePhotoPageJ.hasAndNotNull("button")) {
                    takeSelfiePhotoPage.button = takeSelfiePhotoPageJ.getString("button")
                }
                if (takeSelfiePhotoPageJ.hasAndNotNull("content")) {
                    takeSelfiePhotoPage.content = takeSelfiePhotoPageJ.getString("content")
                }
                if (takeSelfiePhotoPageJ.hasAndNotNull("pageName")) {
                    takeSelfiePhotoPage.pageName = takeSelfiePhotoPageJ.getString("pageName")
                }
                if (takeSelfiePhotoPageJ.hasAndNotNull("subTitle")) {
                    takeSelfiePhotoPage.subTitle = takeSelfiePhotoPageJ.getString("subTitle")
                }
                if (takeSelfiePhotoPageJ.hasAndNotNull("headerTitle")) {
                    takeSelfiePhotoPage.headerTitle = takeSelfiePhotoPageJ.getString("headerTitle")
                }
                if (takeSelfiePhotoPageJ.has("props")) {
                    val propsJ = takeSelfiePhotoPageJ.getJSONObject("props")
                    val props = takeSelfiePhotoPage.props
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
                if (takeSelfiePhotoPageJ.hasAndNotNull("images")) {
                    takeSelfiePhotoPage.images?.clear()
                    val array = takeSelfiePhotoPageJ.getJSONArray("images")
                    try {
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
                            if (takeSelfiePhotoPage.images == null) {
                                takeSelfiePhotoPage.images = mutableListOf()
                            }
                            takeSelfiePhotoPage.images?.add(imageObject)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }

        }
    }
}