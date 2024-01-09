package com.aai.core.utils

import org.json.JSONObject

class JsonUtils {

    companion object {
        fun <T> convertObjToMap(obj: T): Map<String, Any?> {
            // 定义一个Map
            val map = mutableMapOf<String, Any?>()
            // 遍历对象的属性
            for (field in obj!!.javaClass.declaredFields) {
                // 获取属性的名称
                val fieldName = field.name
                // 获取属性的值
                field.isAccessible = true
                val fieldValue = field.get(obj)
                map[fieldName] = fieldValue
            }
            return map
        }

        fun <T> convertObjToJson(obj: T): JSONObject {
            val map = convertObjToMap(obj)
            return JSONObject(map)
        }
    }

}