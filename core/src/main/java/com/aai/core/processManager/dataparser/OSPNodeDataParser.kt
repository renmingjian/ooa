package com.aai.core.processManager.dataparser

import com.aai.core.processManager.model.OSPNodeData
import com.aai.core.utils.hasAndNotNull
import org.json.JSONException
import org.json.JSONObject

/**
 * 只用来解析currentNode接口data这一层数据结构.
 * 本层数据解析成功后，才知道是什么类型的节点，然后使用对应的节点的parser解析数据
 */
class OSPNodeDataParser : OSPDefaultDataParser() {

    val ospNodeData = OSPNodeData()
    var ospNodeDataJson = ""

    override fun parse(response: String) {
        val data = parseResponse(response)
        ospNodeDataJson = data
        val dataObject = JSONObject(data)
        if (dataObject.hasAndNotNull("completedFlag")) {
            ospNodeData.completedFlag = dataObject.getBoolean("completedFlag")
        }
        if (dataObject.hasAndNotNull("transId")) {
            ospNodeData.transId = dataObject.getString("transId")
        }
        if (dataObject.hasAndNotNull("nodeType")) {
            ospNodeData.nodeType = dataObject.getString("nodeType")
        }
        if (dataObject.hasAndNotNull("finalStatus")) {
            ospNodeData.finalStatus = dataObject.getString("finalStatus")
        }
        if (dataObject.hasAndNotNull("waiting")) {
            ospNodeData.waiting = dataObject.getBoolean("waiting")
        }
        if (dataObject.hasAndNotNull("retry")) {
            ospNodeData.retry = dataObject.getBoolean("retry")
        }
        if (dataObject.hasAndNotNull("attemptsRemain")) {
            ospNodeData.attemptsRemain = dataObject.getInt("attemptsRemain")
        }
        if (dataObject.hasAndNotNull("nodeCode")) {
            ospNodeData.nodeCode = dataObject.getString("nodeCode")
            if (dataObject.has("nodeConfig")) {
                val nodeConfig = dataObject.optJSONObject("nodeConfig")
                ospNodeData.nodeConfig = nodeConfig?.toString()
            }
        }
        if (dataObject.hasAndNotNull("message")) {
            val array = dataObject.optJSONArray("message")
            array?.let {
                try {
                    for (i in 0 until it.length()) {
                        val element = it.getString(i)
                        ospNodeData.message?.add(element)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }
}