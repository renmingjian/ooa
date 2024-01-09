package com.aai.core.network

import java.io.DataOutputStream
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * method = POST
 */
class WriteRequestBody {

    fun write(connection: HttpURLConnection, requestBody: OSPRequestBody) {
        when (requestBody) {
            is OSPRequestBody.OSPJsonRequestBody -> {
                writeJson(connection, requestBody)
            }

            is OSPRequestBody.OSPTextRequestBody -> {
                writeText(connection, requestBody)
            }

            is OSPRequestBody.OSPFormUrlRequestBody -> {
                writeFormUrl(connection, requestBody)
            }

            is OSPRequestBody.OSPMultiPartRequestBody -> {
                writeMultiPart(connection, requestBody)
            }
        }
    }

    private fun writeText(
        connection: HttpURLConnection,
        requestBody: OSPRequestBody.OSPTextRequestBody
    ) {
        connection.setRequestProperty("Content-Type", "text/plain")
        DataOutputStream(connection.outputStream).use {
            it.writeBytes(requestBody.text)
        }
    }

    private fun writeJson(
        connection: HttpURLConnection,
        requestBody: OSPRequestBody.OSPJsonRequestBody
    ) {
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        DataOutputStream(connection.outputStream).use {
            it.write(requestBody.json.toByteArray())
        }
    }

    private fun writeFormUrl(
        connection: HttpURLConnection,
        requestBody: OSPRequestBody.OSPFormUrlRequestBody
    ) {
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        val value = requestBody.map.map { (key, value) ->
            "${
                URLEncoder.encode(
                    key,
                    StandardCharsets.UTF_8.name()
                )
            }=${URLEncoder.encode(value.toString(), StandardCharsets.UTF_8.name())}"
        }.joinToString("&")
        DataOutputStream(connection.outputStream).use {
            it.writeBytes(value)
        }
    }

    private fun writeMultiPart(
        connection: HttpURLConnection,
        requestBody: OSPRequestBody.OSPMultiPartRequestBody
    ) {
        val boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
        val lineEnd = "\r\n"
        val twoHyphens = "--"
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        DataOutputStream(connection.outputStream).use { os ->
            requestBody.values.forEach { body ->
                os.writeBytes(twoHyphens + boundary + lineEnd)
                var disposition = "Content-Disposition: form-data; name=${body.key}"
                if (body.fileName.isNotEmpty()) {
                    disposition = "$disposition; filename=${body.fileName}"
                }
                disposition = "$disposition$lineEnd"
                os.writeBytes(disposition)
                val contentType = body.contentType
                if (contentType.isNotEmpty()) {
                    os.writeBytes("Content-Type: $contentType; charset=UTF-8$lineEnd")
                }
                os.writeBytes(lineEnd)
                if (body.file != null) {
                    val file = body.file
                    val buffer = ByteArray(1024 * 8)
                    var bytesRead: Int

                    val fileInputStream = FileInputStream(file)
                    while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                        os.write(buffer, 0, bytesRead)
                    }
                    os.writeBytes(lineEnd)
                }
                if (body.value != null) {
                    os.writeBytes(body.value)
                    os.writeBytes(lineEnd)
                }
                if (body.bytes != null) {
                    os.write(body.bytes)
                    os.writeBytes(lineEnd)
                }
            }
            os.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
        }
    }

}