package com.aai.core.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.aai.core.banner.CustomBanner

class BannerUtils {

    fun setBannerData(
        banner: CustomBanner<String>,
        height: Int,
        beans: List<String>,
        defaultImageMap: Map<String, Int>
    ) {
        OSPLog.log("bannerHeight = $height")
        banner.showIndicatorsAccordingSize(beans.size)
        banner.setBannerSize(height)
        setBean(banner, beans, defaultImageMap)
    }

    private fun setBean(
        banner: CustomBanner<String>,
        beans: List<String>,
        defaultImageMap: Map<String, Int>
    ) {

        banner.setPages(object : CustomBanner.ViewCreator<String> {
            override fun createView(context: Context?, position: Int): View {
                return ImageView(context)
            }

            override fun updateUI(context: Context?, view: View?, position: Int, t: String) {
                if (view is ImageView) {
                    if (t.endsWith("svg")) {
                        defaultImageMap[t]?.let {
                            view.setImageResource(it)
                        }
                    } else {
                        ImageLoader.load(t, view)
                    }
                }
            }
        }, beans)
            .startTurning(2000)
    }

}