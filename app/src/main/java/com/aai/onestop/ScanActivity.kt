package com.aai.onestop

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aai.core.OSPOptions
import com.aai.core.OSPProcessCallback
import com.aai.core.OSPSdk
import com.aai.core.network.HeaderCallback
import com.aai.core.network.HttpUrlConnectionClient
import com.aai.core.network.NetMethod
import com.aai.core.network.NetRequest
import com.aai.core.network.NetWorkCallback
import com.aai.core.processManager.model.NodeCode
import com.aai.core.processManager.model.UrlConst
import com.aai.core.utils.showToast
import com.aai.iqa.node.DocumentNode
import com.aai.selfie.SelfieNode
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var flTag: FrameLayout
    private lateinit var viewFinder: PreviewView
    private lateinit var btnReset: Button

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        initView()

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun initView() {
        flTag = findViewById(R.id.flTag)
        viewFinder = findViewById(R.id.viewFinder)
        btnReset = findViewById(R.id.btnReset2)


        btnReset.setOnClickListener {
            startCamera()
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val image = imageProxy.image
                        if (image != null) {
                            val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
                            val scanner = BarcodeScanning.getClient()
                            scanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    println("barcode success barcodesSize = ${barcodes.size}")
                                    val size = barcodes.size
                                    if (size > 0) {
                                        stopScan()
                                    }
                                    if (barcodes.size == 1) {
                                        barcodes[0].rawValue?.let { url ->
                                            getSDKToken(url)
                                        }
                                    } else {
                                        removeImageViews()
                                        for (barcode in barcodes) {
                                            generate(barcode)
                                            val rawValue = barcode.rawValue
                                            println("barcode success rawValue = ${barcode.boundingBox}, preViewleft = ${viewFinder.left}, top = ${viewFinder.top}")
                                            // 处理二维码/条码
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    println("barcode error rawValue = ${it.message}")
                                    // 处理错误
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                    println("barcode completed")
                                }
                        }
                    }
                }

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to bind camera use cases", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopScan() {
        imageAnalysis?.clearAnalyzer()
        cameraProvider?.unbindAll()
    }

    private fun generate(barcode: Barcode) {
        val previewViewWidth = viewFinder.width
        val previewViewHeight = viewFinder.height

        val cameraAspectRatio = viewFinder.width.toFloat() / viewFinder.height
        val viewAspectRatio = previewViewWidth.toFloat() / previewViewHeight
        val scaleX: Float
        val scaleY: Float

        if (cameraAspectRatio > viewAspectRatio) {
            scaleY = cameraAspectRatio / viewAspectRatio
            scaleX = 1f
        } else {
            scaleX = viewAspectRatio / cameraAspectRatio
            scaleY = 1f
        }
        val barcodeBoundingBox = barcode.boundingBox
        val transformedX = (barcodeBoundingBox?.centerX() ?: 1) * scaleX
        val transformedY = (barcodeBoundingBox?.centerY() ?: 1) * scaleY
        val redDot = View(this).apply {
            background = ContextCompat.getDrawable(this@ScanActivity, R.drawable.bg_image)
            layoutParams = RelativeLayout.LayoutParams(80, 80) // 小红点的大小
            x = transformedX - layoutParams.width / 2
            y =
                transformedY - layoutParams.height / 2 + viewFinder.top + barcodeBoundingBox.height() / 2
        }
        flTag.addView(redDot)
        redDot.setOnClickListener {
            stopScan()
            barcode.rawValue?.let { rawValue ->
                getSDKToken(rawValue)
            }
        }
    }

    private fun removeImageViews() {
        flTag.removeAllViews()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSDKToken(url: String) {
        if (url.isEmpty()) {
            Toast.makeText(this, "你得输入一个URL才行", Toast.LENGTH_SHORT).show()
            return
        }
        val baseUrlMap = mutableMapOf(
            "sandbox" to "https://sandbox",
            "uat" to "https://uat",
            "production" to "https://production",
            "pre" to "https://pre",
            "dev" to "https://dev",
            "sg" to "https://sg",
            "id" to "https://id"
        )
        for ((key, value) in baseUrlMap) {
            if (url.startsWith(value)) {
                UrlConst.currentEvn = key
                break
            }
        }
        val request = NetRequest(
            url = url,
            method = NetMethod.GET,
        )
        HttpUrlConnectionClient.instance.sendRequest(request,
            netWorkCallback = object : NetWorkCallback {
                override fun onSuccess(response: String) {
                    println("sdkToken: Success: $response")
                }

                override fun onError(code: String, message: String) {
                    println("sdkToken: error: $message")
                }

            },
            onHeaderCallback = object : HeaderCallback {
                override fun onGetHeaders(map: Map<String, List<String>>) {
                    map["Location"]?.get(0)?.let {
                        val sdkToken = Uri.parse(it).getQueryParameter("sdkToken")
                        println("sdkToken = $sdkToken")
                        sdkToken?.let {
                            val instance = OSPSdk.instance
                            instance.init(
                                OSPOptions(
                                    context = this@ScanActivity,
                                    key = "microlblink",
                                    sdkToken = sdkToken,
                                    openLog = true,
                                    processCallback = object : OSPProcessCallback {
                                        override fun onReady() {

                                        }

                                        override fun onExit(nodeCode: String) {

                                        }

                                        override fun onError(message: String?) {
                                            showToast(this@ScanActivity, message ?: "")
                                        }

                                        override fun onEvent(
                                            eventName: String,
                                            params: MutableMap<String, String>?
                                        ) {

                                        }

                                        override fun onComplete() {
                                            finish()
                                        }

                                    }
                                )
                            )
                            instance.registerNode(NodeCode.SELFIE, SelfieNode())
                            instance.registerNode(NodeCode.DOCUMENT_VERIFICATION, DocumentNode())
                            instance.startFlow(this@ScanActivity)
                        }
                    }
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}