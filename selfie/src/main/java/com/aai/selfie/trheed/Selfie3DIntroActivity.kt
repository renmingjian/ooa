package com.aai.selfie.trheed

import androidx.viewpager.widget.ViewPager.LayoutParams
import com.aai.core.processManager.model.OSPImage
import com.aai.core.processManager.model.OSPSelfiePhotoComponentPage

/**
 * nodeCode == FACE_PHOTO && selfieType == SELFIE_3D_VIDEO
 */
class Selfie3DIntroActivity : BaseSelfie3DIntroActivity<Selfie3DViewModel>() {

    override val viewModel = Selfie3DViewModel()

    // 接口不返回图片资源，这里写死显示
    override fun getImages(): List<OSPImage>? {
        val image = OSPImage()
        image.imageUrl = "selfie-photo.svg"
        image.width = LayoutParams.WRAP_CONTENT
       return mutableListOf(image)
    }

    override fun getConfig(): OSPSelfiePhotoComponentPage =
        viewModel.configParser.ospSelfieConfig.pages.takeSelfiePhotoPage.component.SELFIE_3D_VIDEO

}