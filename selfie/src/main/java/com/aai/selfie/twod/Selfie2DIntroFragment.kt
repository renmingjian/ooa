package com.aai.selfie.twod

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.aai.core.EventName
import com.aai.core.EventTracker
import com.aai.core.mvvm.BaseViewModelFragment
import com.aai.core.processManager.FragmentJumper
import com.aai.core.utils.BitmapHandler
import com.aai.core.utils.OSPLog
import com.aai.core.utils.colorToInt
import com.aai.core.utils.getThemeBasic
import com.aai.core.utils.getThemeColor
import com.aai.core.utils.getThemeFont
import com.aai.core.utils.getTypeface
import com.aai.core.utils.runOnUIThread
import com.aai.core.utils.screenWidth
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.setSubtitleFont
import com.aai.core.utils.textWithKey
import com.aai.core.utils.toPx
import com.aai.core.views.CustomBulletSpan
import com.aai.core.views.OSPButton
import com.aai.core.views.TitleLayout
import com.aai.selfie.R
import com.aai.selfie.SelfieNode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Selfie2DIntroFragment : BaseViewModelFragment<Selfie2DViewModel>() {

    private lateinit var tv2DSubTitle: TextView
    private lateinit var tv2DContent: TextView
    private lateinit var logo: ImageView
    private lateinit var btnCapture: OSPButton
    private lateinit var titleLayout: TitleLayout

    override fun getLayoutId(): Int = R.layout.fragment_selfie_2d_intro

    override fun initView(view: View) {
        tv2DSubTitle = view.findViewById(R.id.tv2DSubTitle)
        tv2DContent = view.findViewById(R.id.tv2DContent)
        btnCapture = view.findViewById(R.id.btnCapture)
        logo = view.findViewById(R.id.logo)
        titleLayout = view.findViewById(R.id.titleLayout)
        val rlRoot = view.findViewById<RelativeLayout>(R.id.rlRoot)
        setPageBackgroundColor(rlRoot)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(), mutableListOf(
                    Manifest.permission.CAMERA,
                ).toTypedArray(), 111
            )
        }
    }

    private fun allPermissionsGranted() = mutableListOf(
        Manifest.permission.CAMERA,
    ).toTypedArray().all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun initData() {
        val twoDConfig =
            activityViewModel.configParser.ospSelfieConfig.pages.takeSelfiePhotoPage.component.SELFIE_2D_PHOTO
        titleLayout.setElements(
            textWithKey(twoDConfig.headerTitle, "doc_select_country_title"),
            true
        )
        titleLayout.backClick = {
            clickBack()
        }
        setSubtitleFont(tv2DSubTitle, textWithKey(twoDConfig.subTitle, "selfie_2d_subtitle"))
        OSPLog.log("content2d = ${textWithKey(twoDConfig.content, "selfie_2d_content")}")
        setContent(textWithKey(twoDConfig.content, "selfie_2d_content"))
        setCommonButtonTheme(btnCapture, text = textWithKey("start_button", "start_button"))
        setLogo(logo, activityViewModel.nodeCode)
        btnCapture.setOnClickListener {
            takePhoto()
            EventTracker.trackEvent(EventName.CLICK_NEXT, null)
        }
    }

    private fun setContent(text: String) {
        if (text.isNotEmpty()) {
            val bulletSpan = SpannableStringBuilder()
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                val spannableLine = SpannableString(line)
                spannableLine.setSpan(
                    CustomBulletSpan(
                        colorToInt(getThemeColor().bodyTextColor),
                        3.toPx(),
                        4.toPx(),
                        Typeface.create(getTypeface(getThemeFont().smallTextFont), Typeface.NORMAL)
                    ), 0, line.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                bulletSpan.append(spannableLine)
                if (index < lines.size - 1) {
                    bulletSpan.append("\n")
                }
            }
            tv2DContent.text = bulletSpan
            tv2DContent.setTextColor(colorToInt(getThemeColor().bodyTextColor))
        }
    }

    private fun takePhoto() {
        val activity = requireActivity()
        if (activity is FragmentJumper) {
            activity.jump(SelfieNode.FRAGMENT_2D)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        OSPLog.log("onRequestPermissionsResult")
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 权限被授予
        } else {
            OSPLog.log("permission-denied")
            clickBack()
        }
    }

}