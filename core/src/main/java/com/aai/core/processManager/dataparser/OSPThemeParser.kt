package com.aai.core.processManager.dataparser

import com.aai.core.processManager.model.OSPThemeAdvanceData
import com.aai.core.processManager.model.OSPThemeBasicData
import com.aai.core.processManager.model.OSPThemeButtonData
import com.aai.core.processManager.model.OSPThemeColorData
import com.aai.core.processManager.model.OSPThemeData
import com.aai.core.processManager.model.OSPThemeFontData
import com.aai.core.utils.OSPLog
import org.json.JSONObject

class OSPThemeParser : OSPDefaultDataParser() {

    val themeData = OSPThemeData()

    override fun parse(response: String) {
        val data = parseResponse(response)
        val jsonObject = JSONObject(data)
        if (jsonObject.has("data") && !jsonObject.isNull("data")) {
            val str = jsonObject.getString("data")
            val jsonData = JSONObject(str)
            OSPLog.log("themeData: $jsonData")
            if (jsonData.has("basicSetting") && !jsonData.isNull("basicSetting")) {
                val basicData = OSPThemeBasicData()
                val basicSetting = jsonData.getJSONObject("basicSetting")
                if (basicSetting.has("primaryColor")) {
                    basicData.primaryColor = basicSetting.getString("primaryColor")
                }
                if (basicSetting.has("primaryButtonTextColor")) {
                    basicData.primaryButtonTextColor = basicSetting.getString("primaryButtonTextColor")
                }
                if (basicSetting.has("primaryButtonFillColor")) {
                    basicData.primaryButtonFillColor = basicSetting.getString("primaryButtonFillColor")
                }
                if (basicSetting.has("backgroundColor")) {
                    basicData.backgroundColor = basicSetting.getString("backgroundColor")
                }
                if (basicSetting.has("bodyFont")) {
                    basicData.bodyFont = basicSetting.getString("bodyFont")
                }
                if (basicSetting.has("buttonBorderRadius")) {
                    basicData.buttonBorderRadius = basicSetting.getString("buttonBorderRadius")
                }
                if (basicSetting.has("logo")) {
                    basicData.logo = basicSetting.getString("logo")
                }
                if (basicSetting.has("logoShow")) {
                    basicData.logoShow = basicSetting.getString("logoShow")
                }
                if (basicSetting.has("logoWidth")) {
                    basicData.logoWidth = basicSetting.getString("logoWidth")
                }
                themeData.basicSetting = basicData
            }
            if (jsonData.has("color") && !jsonData.isNull("color")) {
                val colorData = OSPThemeColorData()
                val color = jsonData.getJSONObject("color")
                if (color.has("defaultImageFillColor")) {
                    colorData.defaultImageFillColor = color.getString("defaultImageFillColor")
                }
                if (color.has("defaultSuccessImageFillColor")) {
                    colorData.defaultSuccessImageFillColor =
                        color.getString("defaultSuccessImageFillColor")
                }
                if (color.has("defaultDeclineImageFillColor")) {
                    colorData.defaultDeclineImageFillColor =
                        color.getString("defaultDeclineImageFillColor")
                }
                if (color.has("iconFillColor")) {
                    colorData.iconFillColor = color.getString("iconFillColor")
                }
                if (color.has("iconStrokeColor")) {
                    colorData.iconStrokeColor = color.getString("iconStrokeColor")
                }
                if (color.has("successButtonFillColor")) {
                    colorData.successButtonFillColor = color.getString("successButtonFillColor")
                }
                if (color.has("successButtonTextColor")) {
                    colorData.successButtonTextColor = color.getString("successButtonTextColor")
                }
                if (color.has("declineButtonFillColor")) {
                    colorData.declineButtonFillColor = color.getString("declineButtonFillColor")
                }
                if (color.has("declineButtonTextColor")) {
                    colorData.declineButtonTextColor = color.getString("declineButtonTextColor")
                }
                if (color.has("pendingButtonFillColor")) {
                    colorData.pendingButtonFillColor = color.getString("pendingButtonFillColor")
                }
                if (color.has("pendingButtonTextColor")) {
                    colorData.pendingButtonTextColor = color.getString("pendingButtonTextColor")
                }
                if (color.has("headingTextColor")) {
                    colorData.headingTextColor = color.getString("headingTextColor")
                }
                if (color.has("subtitleTextColor")) {
                    colorData.subtitleTextColor = color.getString("subtitleTextColor")
                }
                if (color.has("bodyTextColor")) {
                    colorData.bodyTextColor = color.getString("bodyTextColor")
                }
                if (color.has("smallTextColor")) {
                    colorData.smallTextColor = color.getString("smallTextColor")
                }
                themeData.color = colorData
            }

            if (jsonData.has("font") && !jsonData.isNull("font")) {
                val fontData = OSPThemeFontData()
                val font = jsonData.getJSONObject("font")
                if (font.has("headingFont")) {
                    fontData.headingFont = font.getString("headingFont")
                }
                if (font.has("headingFontSize")) {
                    fontData.headingFontSize = font.getString("headingFontSize")
                }
                if (font.has("headingFontWeight")) {
                    fontData.headingFontWeight = font.getString("headingFontWeight")
                }
                if (font.has("subTitleFont")) {
                    fontData.subTitleFont = font.getString("subTitleFont")
                }
                if (font.has("subTitleFontSize")) {
                    fontData.subTitleFontSize = font.getString("subTitleFontSize")
                }
                if (font.has("bodyFontSize")) {
                    fontData.bodyFontSize = font.getString("bodyFontSize")
                }
                if (font.has("smallTextFont")) {
                    fontData.smallTextFont = font.getString("smallTextFont")
                }
                if (font.has("smallTextFontSize")) {
                    fontData.smallTextFontSize = font.getString("smallTextFontSize")
                }
                if (font.has("buttonFontWeight")) {
                    fontData.buttonFontWeight = font.getString("buttonFontWeight")
                }
                if (font.has("textAlign")) {
                    fontData.textAlign = font.getString("textAlign")
                }
                themeData.font = fontData
            }

            if (jsonData.has("buttons") && !jsonData.isNull("buttons")) {
                val buttonData = OSPThemeButtonData()
                val buttons = jsonData.getJSONObject("buttons")
                if (buttons.has("buttonStyle")) {
                    buttonData.buttonStyle = buttons.getString("buttonStyle")
                }
                if (buttons.has("buttonTextTransform")) {
                    buttonData.buttonTextTransform = buttons.getString("buttonTextTransform")
                }
                themeData.buttons = buttonData
            }

            if (jsonData.has("advanced") && !jsonData.isNull("advanced")) {
                val advanceData = OSPThemeAdvanceData()
                val advanced = jsonData.getJSONObject("advanced")
                if (advanced.has("link")) {
                    advanceData.link = advanced.getString("link")
                }
                if (advanced.has("modalBorderRadius")) {
                    advanceData.modalBorderRadius = advanced.getString("modalBorderRadius")
                }
                themeData.advanced = advanceData
            }
        }
    }
}