package com.aai.selfie.parser

import com.aai.core.processManager.dataparser.OSPDataParser
import com.aai.core.processManager.model.OSPImage
import com.aai.core.processManager.model.OSPSelfieConfig
import com.aai.core.utils.hasAndNotNull
import org.json.JSONException
import org.json.JSONObject

/**
 * 适用于nodeCode为FACE_PHOTO的2D和3D数据结构使用
 * 如果nodeCode为SELFIE_VERIFICATION，则需要使用另外一个数据解析器
 */
class SelfieConfigParser : OSPDataParser {

    val ospSelfieConfig = OSPSelfieConfig()

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
                if (takeSelfiePhotoPageJ.hasAndNotNull("selfieType")) {
                    takeSelfiePhotoPage.selfieType = takeSelfiePhotoPageJ.getString("selfieType")
                }
                if (takeSelfiePhotoPageJ.hasAndNotNull("component")) {
                    val componentJ = takeSelfiePhotoPageJ.getJSONObject("component")
                    val component = takeSelfiePhotoPage.component
                    if (componentJ.hasAndNotNull("SELFIE_2D_PHOTO")) {
                        val twoDJ = componentJ.getJSONObject("SELFIE_2D_PHOTO")
                        val twoD = component.SELFIE_2D_PHOTO
                        if (twoDJ.hasAndNotNull("button")) {
                            twoD.button = twoDJ.getString("button")
                        }
                        if (twoDJ.hasAndNotNull("content")) {
                            twoD.content = twoDJ.getString("content")
                        }
                        if (twoDJ.hasAndNotNull("subTitle")) {
                            twoD.subTitle = twoDJ.getString("subTitle")
                        }
                        if (twoDJ.hasAndNotNull("headerTitle")) {
                            twoD.headerTitle = twoDJ.getString("headerTitle")
                        }
                    }
                    if (componentJ.hasAndNotNull("SELFIE_3D_VIDEO")) {
                        val threeDJ = componentJ.getJSONObject("SELFIE_3D_VIDEO")
                        val threeD = component.SELFIE_3D_VIDEO
                        if (threeDJ.hasAndNotNull("button")) {
                            threeD.button = threeDJ.getString("button")
                        }
                        if (threeDJ.hasAndNotNull("content")) {
                            threeD.content = threeDJ.getString("content")
                        }
                        if (threeDJ.hasAndNotNull("subTitle")) {
                            threeD.subTitle = threeDJ.getString("subTitle")
                        }
                        if (threeDJ.hasAndNotNull("headerTitle")) {
                            threeD.headerTitle = threeDJ.getString("headerTitle")
                        }
                        threeD.props.height = 0
                        if (threeDJ.hasAndNotNull("images")) {
                            threeD.images?.clear()
                            val array = threeDJ.getJSONArray("images")
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
                                    if (threeD.images == null) {
                                        threeD.images = mutableListOf()
                                    }
                                    threeD.images?.add(imageObject)
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    if (componentJ.hasAndNotNull("selfieType")) {
                        takeSelfiePhotoPage.selfieType = componentJ.getString("selfieType")
                    }
                }
            }
        }
        if (jsonObject.hasAndNotNull("selfieCaptureConfig")) {
            val captureConfigJson = jsonObject.getJSONObject("selfieCaptureConfig")
            if (captureConfigJson.hasAndNotNull("generalConfig")) {
                val generalConfigJson = captureConfigJson.getJSONObject("generalConfig")
                if (generalConfigJson.hasAndNotNull("maximumRetries")) {
                    ospSelfieConfig.selfieCaptureConfig.generalConfig.maximumRetries =
                        generalConfigJson.getString("maximumRetries")
                }
            }
        }
    }
}