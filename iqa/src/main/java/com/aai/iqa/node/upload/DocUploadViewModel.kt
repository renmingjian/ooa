package com.aai.iqa.node.upload

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.aai.core.utils.OSPLog
import java.io.ByteArrayOutputStream
import java.io.IOException

class DocUploadViewModel {

    suspend fun compressPhoto(context: Context, uri: Uri): Bitmap? {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        val max = 4096
        val min = 460
        var resultBitmap = bitmap
        try {
            var width = bitmap.width
            var height = bitmap.height
            OSPLog.log("compressPhoto, width = $width, height = $height, size = ${bitmap.byteCount / 1024 / 1024}")
            // 检查分辨率，如果小于指定大小，则直接报错
            if (width < min || height < min) {
                return null
            }
            // 如果大于指定大小，则先压缩分辨率
            if (width > max || height > max) {
                if (width > height) {
                    height = max * height / width
                    width = max
                } else {
                    width = max * width / height
                    height = max
                }
                if (width < min || height < min) {
                    return null
                }
                // 重新调整大小
                resultBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
            }
            // 检查大小
            val outputStream = ByteArrayOutputStream()
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            OSPLog.log("compressPhoto, originalSize = ${outputStream.toByteArray().size / 1024}")
            if (outputStream.toByteArray().size / 1024 < 50) { // 大于1MB
                return null
            }
            if (outputStream.toByteArray().size / 1024 > 1024) { // 大于1MB
                // 压缩图片
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // 压缩率50%
                OSPLog.log("compressPhoto, compressSize = ${outputStream.toByteArray().size / 1024}")
            }
            if (outputStream.toByteArray().size / 1024 > 1024) return null
            return resultBitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

//    private fun getFileSizeFromUri(context: Context, uri: Uri): Long {
//        var fileSize: Long = 0
//        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
//
//        cursor.use {
//            if (it != null && it.moveToFirst()) {
//                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
//                if (!it.isNull(sizeIndex)) {
//                    fileSize = it.getLong(sizeIndex)
//                }
//            }
//        }
//
//        return fileSize
//    }


}