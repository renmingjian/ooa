package com.aai.core.processManager.dataparser

import com.aai.core.processManager.model.OSPStartPageConfig
import com.aai.core.processManager.model.OSPImage
import com.aai.core.utils.hasAndNotNull
import org.json.JSONException
import org.json.JSONObject

/**
 * StartPage数据结构，用来解析currentNode->data->nodeConfig结构
 */
class OSPStartPageNodeDataParser : OSPDefaultDataParser() {

    val startPageConfig = OSPStartPageConfig()

    override fun parse(response: String) {
        val data = JSONObject(response)
        if (data.hasAndNotNull("pageName")) {
            startPageConfig.pageName = data.getString("pageName")
        }
        if (data.hasAndNotNull("button")) {
            startPageConfig.button = data.getString("button")
        }
        if (data.hasAndNotNull("subTitle")) {
            startPageConfig.subTitle = data.getString("subTitle")
        }
        if (data.hasAndNotNull("displayLogo")) {
            startPageConfig.displayLogo = data.getBoolean("displayLogo")
        }
        if (data.hasAndNotNull("headerTitle")) {
            startPageConfig.headerTitle = data.getString("headerTitle")
        }
        if (data.has("props")) {
            val propsJ = data.getJSONObject("props")
            val props = startPageConfig.props
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
        if (data.hasAndNotNull("images")) {
            startPageConfig.images?.clear()
            val array = data.getJSONArray("images")
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
                    if (startPageConfig.images == null) {
                        startPageConfig.images = mutableListOf()
                    }
                    startPageConfig.images?.add(imageObject)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}