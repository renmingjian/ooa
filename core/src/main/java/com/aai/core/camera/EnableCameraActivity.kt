package com.aai.core.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aai.core.OSPSdk
import com.aai.core.R
import com.aai.core.processManager.model.BundleConst
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.ProcessEvent
import com.aai.core.utils.getTextFromAssets
import com.aai.core.utils.setCommonButtonTheme
import com.aai.core.utils.setContentFont
import com.aai.core.utils.setHeadingFont
import com.aai.core.utils.setLogo
import com.aai.core.utils.setPageBackgroundColor
import com.aai.core.utils.stateBar
import com.aai.core.utils.textWithKey
import com.aai.core.views.OSPButton

class EnableCameraActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var btnTryAgain: OSPButton
    private lateinit var logo: ImageView


    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val APP_SETTINGS_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateBar(false)
        setContentView(R.layout.activity_enable_camera)

        initView()
        initData()
    }

    private fun initView() {
        tvTitle = findViewById(R.id.tvTitle)
        tvContent = findViewById(R.id.tvContent)
        btnTryAgain = findViewById(R.id.btnTryAgain)
        logo = findViewById(R.id.logo)
        findViewById<View>(R.id.ivBack).setOnClickListener {
            finish()
        }
        btnTryAgain.setOnClickListener {
            handleCameraPermission()
        }
    }

    private fun initData() {
        val rlRoot = findViewById<RelativeLayout>(R.id.rlRoot)
        setPageBackgroundColor(rlRoot)
        intent.getStringExtra(BundleConst.TITLE_KEY)?.let {
            setHeadingFont(tvTitle, textWithKey(it, "FaceTec_camera_permission_enable_camera"))
        }
        intent.getStringExtra(BundleConst.CONTENT_KEY)?.let {
            setContentFont(tvContent, textWithKey(it, "camera_permission_content"))
        }
        val buttonKey =
            intent.getStringExtra(BundleConst.BUTTON_KEY) ?: getTextFromAssets("osp_try_again")
        val buttonText =
            textWithKey(buttonKey, "osp_try_again").ifEmpty { getString(R.string.osp_try_again) }
        setCommonButtonTheme(btnTryAgain, buttonText)
        val nodeCode =
            intent.getStringExtra(BundleConst.NODE_CODE) ?: NodeCode.DOCUMENT_VERIFICATION

        OSPSdk.instance.getProcessCallback()?.onEvent(
            eventName = ProcessEvent.EVENT_ENABLE_CAMERA,
            params = mutableMapOf("source" to if (nodeCode == NodeCode.DOCUMENT_VERIFICATION) "document" else "selfie")
        )
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission()) goBack()
    }

    private fun handleCameraPermission() {
        if (hasCameraPermission()) {
            goBack()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun goBack() {
        val returnIntent = Intent()
        returnIntent.putExtra(BundleConst.HAVE_PERMISSION, hasCameraPermission())
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        goBack()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    goBack()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.CAMERA
                        )
                    ) {
                        redirectToSettings()
                    }
                }
            }
        }
    }

    private fun redirectToSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivityForResult(intent, APP_SETTINGS_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_SETTINGS_REQUEST_CODE) {
            if (hasCameraPermission()) {
                goBack()
            }
        }
    }

}