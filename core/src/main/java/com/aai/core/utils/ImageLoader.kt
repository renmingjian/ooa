package com.aai.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.aai.core.processManager.model.OSPThemeBasicData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object ImageLoader {
    fun load(url: String, imageView: ImageView) {
        GlobalScope.launch(Dispatchers.IO) {
            val bitmap = loadBitmapFromUrl(url)
            runOnUIThread {
                bitmap?.let {
                    imageView.setImageBitmap(it)
                }
            }
        }
    }

    fun loadLogo(url: String, imageView: ImageView) {
        var bitmap = OSPThemeBasicData.logoBitmap
        if (bitmap != null) {
            resetLogoWidth(imageView, bitmap)
            imageView.setImageBitmap(bitmap)
            return
        }
        GlobalScope.launch(Dispatchers.IO) {
            bitmap = loadBitmapFromUrl(url)
            runOnUIThread {
                bitmap?.let {
                    resetLogoWidth(imageView, it)
                    imageView.setImageBitmap(it)
                    OSPThemeBasicData.logoBitmap = it
                }
            }
        }
    }

    /**
     * imageWidth为后端下发的宽度
     */
    private fun resetLogoWidth(
        imageView: ImageView,
        bitmap: Bitmap,
    ) {
        val ratio = bitmap.width.toFloat() / bitmap.height
//        // 后端下发的图片的宽度
//        OSPLog.log("serviceWidth = $serviceWidth")
//        // 修正图片宽度，不能大于屏幕宽度
//        if (serviceWidth > screenWidth() - 32.toPx()) serviceWidth = screenWidth() - 32.toPx()
//        var height: Int = (serviceWidth / ratio).toInt()
//        // 不能超出父控件的高度，如果超出，则以父控件的最高为高度，同时按照比例更改宽度
//        if (height > 50.toPx()) {
//            height = 50.toPx()
//            serviceWidth = (height * ratio).toInt()
//        }
//        OSPLog.log("serviceWidth = $serviceWidth, height = $height")
        // 高度固定为50dp
        var height = 50.toPx()
        var serviceWidth = (ratio * height).toInt()
        if (serviceWidth > screenWidth() - 32.toPx()) serviceWidth = screenWidth() - 32.toPx()
        height = (serviceWidth / ratio).toInt()
        val lp = imageView.layoutParams
        lp.width = serviceWidth
        lp.height = height
        imageView.layoutParams = lp
    }

    // 获取到图片后就加载图片，然后缓存起来
    fun loadLogoBitmap(url: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val bitmap = loadBitmapFromUrl(url)
            runOnUIThread {
                bitmap?.let {
                    OSPThemeBasicData.logoBitmap = it
                }
            }
        }
    }

    private fun loadBitmapFromUrl(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            val inputStream: InputStream = connection.inputStream
            val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)

            // 关闭连接和输入流
            inputStream.close()
            connection.disconnect()

            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}