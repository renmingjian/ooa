package com.aai.core.processManager.dataparser

import com.aai.core.processManager.model.OSPImage
import com.aai.core.processManager.model.OSPResultConfig
import com.aai.core.utils.hasAndNotNull
import org.json.JSONException
import org.json.JSONObject

class OSPResultPageDataParser: OSPDefaultDataParser() {
    val resultPageConfig = OSPResultConfig()

    override fun parse(response: String) {
        val data = JSONObject(response)
        if (data.has("pageName")) {
            resultPageConfig.pageName = data.getString("pageName")
        }
        if (data.has("button")) {
            resultPageConfig.button = data.getString("button")
        }
        if (data.has("subTitle")) {
            resultPageConfig.subTitle = data.getString("subTitle")
        }
        if (data.has("displayLogo")) {
            resultPageConfig.displayLogo = data.getBoolean("displayLogo")
        }
        if (data.has("headerTitle")) {
            resultPageConfig.headerTitle = data.getString("headerTitle")
        }
        if (data.has("props")) {
            val propsJ = data.getJSONObject("props")
            val props = resultPageConfig.props
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
        if (data.has("images")) {
            resultPageConfig.images?.clear()
            val array = data.getJSONArray("images")
            try {
                for (i in 0 until array.length()) {
                    val imageObject = OSPImage()
                    val element = array.getJSONObject(i)
                    if (element.has("width")) {
                        imageObject.width = element.getInt("width")
                    }
                    if (element.has("imageUrl")) {
                        imageObject.imageUrl = element.getString("imageUrl")
                    }
                    if (element.has("imageName")) {
                        imageObject.imageName = element.getString("imageName")
                    }
                    if (resultPageConfig.images == null) {
                        resultPageConfig.images = mutableListOf()
                    }
                    resultPageConfig.images?.add(imageObject)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }


}