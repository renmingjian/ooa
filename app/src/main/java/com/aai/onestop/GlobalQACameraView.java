package com.aai.onestop;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.aai.core.utils.DimensionExtKt;
import com.aai.core.views.GuardianCameraView;

import org.json.JSONArray;


/**
 * createTime:2023/2/20
 *
 * @author fan.zhang@advancegroup.com
 */
public class GlobalQACameraView extends GuardianCameraView {
    public GlobalQACameraView(Context context) {
        super(context);
    }

    public GlobalQACameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    CropParams cropParams;
    boolean mLightOpened;

    public boolean isLightOpened() {
        return mLightOpened;
    }

    int getCameraId() {
        return mCameraId;
    }

    JSONArray mCameraPreviewSizeArray;

    @Override
    protected Camera.Size calBestPreviewSize(Camera.Parameters parameters) {
        mCameraPreviewSizeArray = new JSONArray();
//        Camera.Size size = null;
        Camera.Size size = null;
        for (Camera.Size supportedPreviewSize : parameters.getSupportedPreviewSizes()) {
            if (size == null) {
                size = supportedPreviewSize;
            }
            if (isPortrait()) {
                // 分辨率宽度大于屏幕宽度，并且宽大于高
                if (supportedPreviewSize.width >= getScreenSize().x && supportedPreviewSize.width / (float) supportedPreviewSize.height >= (8 / 5f)) {
                    if (supportedPreviewSize.width <= size.width) {
                        size = supportedPreviewSize;
                    }

                }
            } else {
                if (supportedPreviewSize.height >= getScreenSize().y && supportedPreviewSize.width / (float) supportedPreviewSize.height >= (386 / 248f)) {
                    if (supportedPreviewSize.height <= size.height) {
                        size = supportedPreviewSize;
                    }

                }
            }
            mCameraPreviewSizeArray.put(supportedPreviewSize.width + "*" + supportedPreviewSize.height);
        }
        return size;
//        return mPageState.isPortrait() ? super.calBestPreviewSize(parameters) : calBestLandscapePreviewSize(parameters);
    }

    /**
     * 注意，下面的代码仅适用于iqa，未做其他场景的适配
     */
    @Override
    protected void transformTexture() {
        if (mPreviewSize != null) {
            float viewWidth = getViewWidth();
            float viewHeight = getViewHeight();
            RectF fromRect;
            RectF toRect;//映射的矩阵像素点为 texture 的尺寸
            Matrix matrix = new Matrix();

            if (isPortrait()) {
                fromRect = new RectF(0, 0, viewHeight, getRatio(mPreviewSize) * viewHeight);
                toRect = new RectF(0, 0, viewHeight, viewWidth);
            } else {
                fromRect = new RectF(0, 0, viewWidth, viewHeight);
                toRect = new RectF(0, 0, viewWidth, viewHeight);
            }
            mCameraTransformWidthRatio = 1f;
            mCameraTransformHeightRatio = fromRect.height() / toRect.height();
            matrix.setRectToRect(fromRect, toRect, isPortrait() ? Matrix.ScaleToFit.FILL : Matrix.ScaleToFit.START);
            setTransform(matrix);
            cropParams = createCropParams();
        }
    }

    @Override
    public boolean isPortrait() {
        return super.isPortrait();
    }

    int getCameraAngle() {
        return mCameraAngle;
    }

    /**
     * 正在对焦
     */
    boolean mAutoFocusing;
    long mLastAutoFocusFinishTimeMills;

    /**
     * 获取自动对焦回调,如果没有则创建
     */
    protected synchronized Camera.AutoFocusCallback getAutoFocusCallback() {
        if (mAutoFocusCallback == null) {
            mAutoFocusCallback = new Camera.AutoFocusCallback() {

                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    mAutoFocusing = false;
                    mLastAutoFocusFinishTimeMills = System.currentTimeMillis();
                }
            };
        }
        return mAutoFocusCallback;
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        if (width < height * mWidth / mHeight) {
//            System.out.println("width = " + width + ", height = " + (width * mHeight / mWidth));
//            setMeasuredDimension(width, width * mHeight / mWidth);
//        } else {
//            System.out.println("width = " + (height * mWidth / mHeight) + ", height = " + mHeight);
//            setMeasuredDimension(height * mWidth / mHeight, height);
//        }
//    }

    /**
     * 创建裁剪参数
     */
    CropParams createCropParams() {
        if (mPreviewSize == null) {
            return new CropParams();
        }
        int previewWidth = mPreviewSize.width;
        int previewHeight = mPreviewSize.height;
        CropParams cropParams = new CropParams();
        cropParams.previewWidth = mPreviewSize.width;
        cropParams.previewHeight = mPreviewSize.height;
        if (mCameraAngle == 270 || mCameraAngle == 180) {// 倒立，需要从下面截取
            if (isPortrait()) {
                if (isFrontCamera()) {
                    cropParams.cropRect = new Rect(0, 0, (int) (mCameraTransformHeightRatio * previewWidth), previewHeight);
                } else {
                    cropParams.cropRect = new Rect((int) ((1 - mCameraTransformHeightRatio) * previewWidth), 0, previewWidth, previewHeight);
                }
                //裁剪矩阵 = Rect(270, 0 , 720, 720)，裁剪比例：RectF(0.375, 0.0, 1.0, 1.0)
            } else {
                float cameraRatio = (float) mPreviewSize.width / mPreviewSize.height;
                float viewRatio = (float) getViewWidth() / getViewHeight();
                if (cameraRatio > viewRatio) {
                    cropParams.cropRect = new Rect((int) (previewWidth * (1 - mCameraTransformWidthRatio)), 0, previewWidth, previewHeight);
                } else {
                    cropParams.cropRect = new Rect(0, (int) (previewHeight * (1 - mCameraTransformHeightRatio)), previewWidth, previewHeight);
                }
            }

        } else {
            if (isPortrait()) {
                if (isFrontCamera()) {
                    cropParams.cropRect = new Rect((int) ((1 - mCameraTransformHeightRatio) * previewWidth), 0, previewWidth, previewHeight);

                } else {
                    cropParams.cropRect = new Rect(0, 0, (int) (mCameraTransformHeightRatio * previewWidth), previewHeight);
                }
            } else {
                float cameraRatio = (float) mPreviewSize.width / mPreviewSize.height;
                float viewRatio = (float) getViewWidth() / getViewHeight();
                if (cameraRatio > viewRatio) {
                    cropParams.cropRect = new Rect(0, 0, (int) (previewWidth * mCameraTransformWidthRatio), previewHeight);
                } else {
                    cropParams.cropRect = new Rect(0, 0, previewWidth, (int) (previewHeight * mCameraTransformHeightRatio));
                }
            }
        }
        return cropParams;
    }

    public boolean isSupportLight() {
        PackageManager packageManager = getContext().getPackageManager();
        FeatureInfo[] features = packageManager.getSystemAvailableFeatures();
        for (FeatureInfo feature : features) {
            if (PackageManager.FEATURE_CAMERA_FLASH.equals(feature.name)) {
                return true;
            }
        }
        return false;
    }

    public void toggleLight() {
        if (mCamera != null) {
            if (isSupportLight() && !isFrontCamera()) {
                Camera.Parameters parameters = mCamera.getParameters();
                if (mLightOpened) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                } else {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
                mCamera.setParameters(parameters);
                mLightOpened = !mLightOpened;
            }
        }
    }

    public Point getScreenSize() {
        DisplayMetrics dm = new DisplayMetrics();

        WindowManager windowMgr = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getRealMetrics(dm);
        // 获取高度
        int height = dm.heightPixels;
        // 获取宽度
        int width = dm.widthPixels;
        return new Point(width, height);
    }
}
