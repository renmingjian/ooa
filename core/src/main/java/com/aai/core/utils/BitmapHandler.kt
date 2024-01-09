package com.aai.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

class BitmapHandler {

    companion object {
        fun compressImage(file: File, quality: Int, compressedFileName: String): File? {
            // 'quality' ranges from 1 - 100 (100 being the highest quality)
            // 'compressedFileName' is the name for the new file after compression

            return try {
                // BitmapFactory options to reduce the size
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = false
                    inPreferredConfig = Bitmap.Config.RGB_565
                    // Adjust this factor to your needs
                    inSampleSize = 2
                }

                // Decode bitmap with inSampleSize set
                val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

                // Write the compressed bitmap at the destination specified by filename.
                val compressedFile = File(file.parent, compressedFileName)
                FileOutputStream(compressedFile).use { out ->
                    // Compress the bitmap and write to outputStream
                    // Format can be PNG, JPEG or WEBP.
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    out.flush()
                }

                // Return the compressed file path
                compressedFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

}