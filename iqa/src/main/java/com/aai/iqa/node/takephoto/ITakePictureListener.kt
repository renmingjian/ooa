package com.aai.iqa.node.takephoto

import android.graphics.Bitmap

interface ITakePictureListener {

    fun onPictureTake(bitmap: Bitmap)

}