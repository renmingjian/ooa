package com.aai.selfie.trheed

import com.aai.core.processManager.model.OSPImage
import com.aai.core.processManager.model.OSPSelfiePhotoComponentPage
import java.util.Calendar

/**
 * nodeCode == SELFIE_VERIFICATION
 */
class Selfie3DVerificationIntroActivity :
    BaseSelfie3DIntroActivity<Selfie3DVerificationViewModel>() {

    override val viewModel = Selfie3DVerificationViewModel()

    override fun getConfig(): OSPSelfiePhotoComponentPage =
        viewModel.configParser.ospSelfieConfig.pages.takeSelfiePhotoPage

    override fun getImages(): List<OSPImage>? = getConfig().images

    override fun onSDKInitCompleted(successful: Boolean) {
        super.onSDKInitCompleted(successful)
        if (successful) {
            viewModel.startTime =
                viewModel.getCurrentISO8601FormattedTime(Calendar.getInstance().time)
        }
    }

}