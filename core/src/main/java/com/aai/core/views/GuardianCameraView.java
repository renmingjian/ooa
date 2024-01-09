package com.aai.core.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;

import com.aai.core.utils.BitmapUtil;
import com.aai.core.utils.CameraUtils2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 相机预览控件
 * createTime:2019/4/17
 *
 * @author fan.zhang@advance.ai
 */
public class GuardianCameraView extends TextureView implements Camera.PreviewCallback, View.OnLayoutChangeListener {
    /**
     * 自动对焦间隔
     */
    protected long mAutoFocusInterval = 1300L;
    /**
     * 依附的 activity
     */
    protected Activity mActivity;
    /**
     * 相机回调
     */
    protected CallBack mCallBack;
    /**
     * 相机对象
     */
    protected Camera mCamera;
    /**
     * 预览尺寸
     */
    public Camera.Size mPreviewSize;
    /**
     * 正在打开相机，锁定方法
     */
    protected boolean mOnCameraOpening;
    /**
     * 当前的摄像头 id
     */
    protected int mCameraId;
    /**
     * First back camera id
     */
    protected int mBackCameraId;
    /**
     * First front camera id
     */
    protected int mFrontCameraId;
    /**
     * 相机角度
     */
    protected int mCameraAngle;
    /**
     * 等待打开相机
     */
    protected boolean mWaitOpenCamera;
    /**
     * 控件的宽高
     */
    protected int mMeasureWidth, mMeasureHeight;
    /**
     * 自动对焦 handler
     */
    protected AutoFocusHandler mAutoFocusHandler;
    /**
     * 自动对焦回调
     */
    protected Camera.AutoFocusCallback mAutoFocusCallback;
    /**
     * 自动对焦是否可用
     */
    protected boolean mAutoFocusEnable;
    /**
     * 是否要取这一帧
     */
    protected boolean mCaptureCurrentFrame;
    protected int mWidth, mHeight;
    /**
     * 图像处理线程池
     */
    protected ExecutorService mExecutor;

    /**
     * surface 就绪
     */
    protected boolean mSurfaceTextureAvailable = false;

    public void onPause() {

    }

    public void onResume() {
    }

    protected boolean mSoundPlayEnable = true;

    /**
     * 声音是否可以用
     *
     * @param playEnable 是否可以播放
     */
    public void setSoundPlayEnable(boolean playEnable) {
        this.mSoundPlayEnable = playEnable;
    }

    public void setAspRatio(int width, int height) {
        mWidth = width;
        mHeight = height;
        requestLayout();
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        if (mWidth == 0 || mHeight == 0) {
//            setMeasuredDimension(width, height);
//        } else {
//
//            float aspectRatio = mWidth * 1F / mHeight;
//            float actualRatio = width > height ? aspectRatio : 1f / aspectRatio;
//            int newWidth;
//            int newHeight;
//            if (width < height * actualRatio) {
//                newHeight = height;
//                newWidth = (int) (height * actualRatio);
//            } else {
//                newWidth = width;
//                newHeight = (int) (width / actualRatio);
//            }
//            setMeasuredDimension(newWidth, newHeight);
//        }
//    }

    public boolean isSoundPlayEnable() {
        return mSoundPlayEnable;
    }


    public GuardianCameraView(Context context) {
        this(context, null);
    }


    public GuardianCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }

        detectCameras();
        setSurfaceTextureListener(mSurfaceTextureListener);
        addOnLayoutChangeListener(this);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFocusOnce();
            }
        });
    }

    protected void detectCameras() {
        mBackCameraId = CameraUtils2.getBackCameraId();
        mFrontCameraId = CameraUtils2.getFrontCameraId();
    }

    /**
     * 对焦一次
     */
    public void autoFocusOnce() {
        getMainHandler().forceSendAutoFocusOnce();
    }

    /**
     * 获取线程池
     */
    protected ExecutorService getExecutor() {
        if (mExecutor == null) {
            mExecutor = Executors.newCachedThreadPool();
        }
        return mExecutor;
    }

    /**
     * 自动对焦 handler
     */
    protected class AutoFocusHandler extends Handler {
        /**
         * 自动对焦消息序列号
         */
        static final int WHAT_AUTO_FOCUS = 9245;
        /**
         * 触摸对焦序列号
         */
        static final int WHAT_TOUCH_FOCUS = 9246;

        /**
         * 强制发送自动对焦消息
         */
        synchronized void forceSendAutoFocusOnce() {
            sendEmptyMessage(WHAT_TOUCH_FOCUS);
        }

        /**
         * 发送自动对焦消息
         */
        synchronized void sendAutoFocusMsg() {
            if (mAutoFocusEnable) {
                sendEmptyMessageDelayed(WHAT_AUTO_FOCUS, mAutoFocusInterval);
            }
        }

        /**
         * 停止自动对焦
         */
        synchronized void stopAutoFocus() {
            removeMessages(WHAT_AUTO_FOCUS);
            removeMessages(WHAT_TOUCH_FOCUS);
        }

        AutoFocusHandler(Looper looper) {
            super(looper);
        }

        /**
         * 执行对焦操作
         */
        protected void performFocus() {
            try {
                mCamera.autoFocus(getAutoFocusCallback());
            } catch (Exception ignored) {
            }

        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_AUTO_FOCUS:
                    if (cameraEnable() && mAutoFocusEnable) {
                        performFocus();
                    }
                    break;
                case WHAT_TOUCH_FOCUS:
                    performFocus();
                    break;
            }
        }
    }

    /**
     * 设置自动对焦是否可用
     *
     * @param autoFocusInterval 对焦间隔
     */
    public void setAutoFocusEnable(long autoFocusInterval) {
        mAutoFocusEnable = true;
        mAutoFocusInterval = autoFocusInterval;
        if (cameraEnable()) {//如果已经打开了相机，则直接开始自动对焦
            startAutoFocus();
        }
    }

    /**
     * 设置自动对焦不可用
     */
    public void setAutoFocusDisable() {
        mAutoFocusEnable = false;
        stopAutoFocus();
    }

    /**
     * 设置自动对焦是否可用
     */
    public void setAutoFocusEnable() {
        mAutoFocusEnable = true;
    }

    /**
     * 是否可以自动对焦
     */
    public boolean isAutoFocusEnable() {
        return mAutoFocusEnable;
    }

    /**
     * 获取自动对焦 handler
     */
    private synchronized AutoFocusHandler getMainHandler() {
        if (mAutoFocusHandler == null) {
            mAutoFocusHandler = new AutoFocusHandler(Looper.getMainLooper());
        }
        return mAutoFocusHandler;
    }

    /**
     * 获取自动对焦回调,如果没有则创建
     */
    protected synchronized Camera.AutoFocusCallback getAutoFocusCallback() {
        if (mAutoFocusCallback == null) {
            mAutoFocusCallback = new Camera.AutoFocusCallback() {

                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    getMainHandler().sendAutoFocusMsg();
                }
            };
        }
        return mAutoFocusCallback;
    }

    /**
     * 开启自动对焦
     */
    protected synchronized void startAutoFocus() {
        if (cameraEnable()) {
            getMainHandler().sendAutoFocusMsg();
        }
    }

    /**
     * 关闭自动对焦
     */
    protected void stopAutoFocus() {
        getMainHandler().stopAutoFocus();
    }

    /**
     * 获取预览尺寸
     */
    public Camera.Size getPreviewSize() {
        return mPreviewSize;
    }

    /**
     * 获取相机
     */
    public Camera getCamera() {
        return mCamera;
    }

    public static final String TAG = "zhang";

    /**
     * Open the camera
     */
    public void open(int cameraId) {
        if (!mOnCameraOpening) {
            try {
                mOnCameraOpening = true;
                mCamera = Camera.open(cameraId);
                Camera.Parameters params = mCamera.getParameters();
                mPreviewSize = calBestPreviewSize(mCamera.getParameters());
                params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                mCameraAngle = getCameraAngle(cameraId);
                mCamera.setDisplayOrientation(mCameraAngle);
                mCamera.setParameters(params);
                transformTexture();
                startAutoFocus();
            } catch (Exception e) {
//                LogUtil.sdkLogE("open camera exception:" + e.getMessage());
            }
            if (mCamera == null && mCallBack != null) {
                mCallBack.onCameraOpenFailed();
            }
            mOnCameraOpening = false;
        }
    }

    protected float getRatio(Camera.Size size) {
        return getPreviewWidth(size) / (float) getPreviewHeight(size);
    }

    protected int getPreviewWidth(Camera.Size size) {
        return isPortrait() ? size.height : size.width;
    }

    protected int getPreviewHeight(Camera.Size size) {
        return isPortrait() ? size.width : size.height;
    }

    /**
     * 选择预览尺寸,规则：
     * 1.按照宽高比进行排序
     * 2.
     */
    protected Camera.Size calBestPreviewSize(Camera.Parameters camPara) {
        Camera.Size mBestSize = null;
        try {
            //垂直选ratio最大的
            List<Camera.Size> allSupportedSize = camPara.getSupportedPreviewSizes();
            final float screenRatio = getViewWidth() / (float) getViewHeight();
            Collections.sort(allSupportedSize, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size o1, Camera.Size o2) {
                    float ratio1 = getRatio(o1);
                    float ratio2 = getRatio(o2);
                    float diff1 = Math.abs(screenRatio - ratio1);
                    float diff2 = Math.abs(screenRatio - ratio2);
                    return Float.compare(diff1, diff2);
                }
            });

            List<Camera.Size> appropriateSizeList = new ArrayList<>();//选定的尺寸集合
            float proximateRatio = 0;//最接近控件尺寸的比例，选定第一个元素的尺寸
            if (allSupportedSize.size() > 0) {
                proximateRatio = getRatio(allSupportedSize.get(0));
            }
            for (Camera.Size size : allSupportedSize) {
                float ratio = getRatio(size);
                float diff = Math.abs(ratio - proximateRatio);
                if (diff < 0.1) {
                    appropriateSizeList.add(size);
                }
            }
            // 按照和当前控件尺寸的宽高接近程度进行排序
            Collections.sort(appropriateSizeList, new Comparator<Camera.Size>() {
                int differSize(Camera.Size size) {
                    return Math.abs((getViewWidth() - getPreviewWidth(size)) + (getViewHeight() - getPreviewHeight(size)));
                }

                @Override
                public int compare(Camera.Size o1, Camera.Size o2) {
                    float sizeSum1 = differSize(o1);
                    float sizeSum2 = differSize(o2);
                    return Float.compare(sizeSum1, sizeSum2);
                }
            });

            if (appropriateSizeList.size() > 0) {
                mBestSize = appropriateSizeList.get(0);
            } else if (allSupportedSize.size() > 0) {
                mBestSize = allSupportedSize.get(0);
            }
        } catch (Exception ignored) {

        }
        return mBestSize;
    }

    public float getScale() {
        return 1f;
    }

    /**
     * 相机预览映射的尺寸
     */
    protected float mCameraTransformWidthRatio;
    protected float mCameraTransformHeightRatio;

    /**
     * 预览帧矩阵转换
     */
    protected void transformTexture() {
        if (mPreviewSize != null) {
            float viewWidth = getViewWidth();
            float viewHeight = getViewHeight();
            float ratio = getRatio(mPreviewSize);
            RectF fromRect;
            RectF toRect = new RectF(0, 0, viewHeight, viewWidth);//映射的矩阵像素点为 texture 的尺寸
            if (isPortrait()) {
                fromRect = new RectF(0, 0, viewHeight, ratio * viewHeight);
                mCameraTransformWidthRatio = fromRect.width() / toRect.width();
                mCameraTransformHeightRatio = fromRect.height() / toRect.height();
            } else {
                fromRect = new RectF(0, 0, viewWidth / ratio, viewWidth);
                mCameraTransformWidthRatio = fromRect.height() / toRect.height();
                mCameraTransformHeightRatio = fromRect.width() / toRect.width();
            }
            mCameraTransformWidthRatio = fromRect.width() / toRect.width();
            mCameraTransformHeightRatio = fromRect.height() / toRect.height();
            Matrix matrix = new Matrix();
            matrix.setRectToRect(fromRect, toRect, Matrix.ScaleToFit.FILL);
            setTransform(matrix);
        }
    }

    /**
     * 相机是否已打开
     *
     * @return 已打开返回 true
     */
    public boolean cameraEnable() {
        return mCamera != null;
    }

    /**
     * 打开前置摄像头
     *
     * @param callback
     */
    public void openFrontCamera(CallBack callback) {
        if (mFrontCameraId == -1 && callback != null) {
            callback.onCameraOpenFailed();
        } else {
            openCamera(mFrontCameraId, callback);
        }
    }

    /**
     * 打开后置摄像头
     */
    public void openFrontCamera() {
        openFrontCamera(null);
    }

    /**
     * 打开后置摄像头
     *
     * @param callback
     */
    public void openBackCamera(CallBack callback) {
        if (mBackCameraId == -1 && callback != null) {
            callback.onCameraOpenFailed();
        } else {
            openCamera(mBackCameraId, callback);
        }
    }

    /**
     * 打开前置摄像头,无回调
     */
    public void openBackCamera() {
        openBackCamera(null);
    }

    /**
     * 切换摄像头
     */
    public void transformCamera() {
        restartCamera(isFrontCamera() ? CameraUtils2.getBackCameraId() : CameraUtils2.getFrontCameraId());
    }

    /**
     * 重新打开相机
     */
    public void restartCamera(int cameraId) {
        closeCamera();
        openCamera(cameraId, mCallBack);
        startPreviewAndCallBack();
    }

    /**
     * 重新打开相机
     */
    public void restartCamera() {
        restartCamera(mCameraId);
    }

    /**
     * Whether the current camera is front-facing
     *
     * @return
     */
    public boolean isFrontCamera() {
        if (mFrontCameraId == -1) {
            return false;
        }
        return mCameraId == mFrontCameraId;
    }


    /**
     * 打开相机
     */
    public void openCamera(final int cameraId, CallBack callback) {
        mCallBack = callback;
        mCameraId = cameraId;
        if (mActivity == null) {
            if (mCallBack != null) {
                mCallBack.onCameraOpenFailed();
            }
        } else {
            if (mMeasureWidth == 0) {//控件宽度为0，标记等待控件不为0的时候打开相机
                mWaitOpenCamera = true;
            } else {//打开相机
                mWaitOpenCamera = false;
                open(cameraId);
            }

        }

    }

    /**
     * 关闭相机
     */
    public void closeCamera() {
        stopAutoFocus();
        if (mCamera != null) {
//            getExecutor().shutdown();
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                               int oldTop, int oldRight, int oldBottom) {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        boolean sizeChanged = mMeasureWidth != measuredWidth || mMeasureHeight != measuredHeight;
        if (sizeChanged) {//尺寸发生变化,需要刷新相机预览
            mMeasureWidth = getMeasuredWidth();
            mMeasureHeight = getMeasuredHeight();
            if (mWaitOpenCamera) {//初次打开相机
                restartCamera(mCameraId);
            } else if (cameraEnable()) {//相机已打开，要重新进行矩阵转换
                transformTexture();
            }

        }
    }

    /**
     * 相机回调
     */
    public interface CallBack {
        /**
         * 页面更新回调
         */
        void onSurfaceTextureUpdated(SurfaceTexture surface);

        /**
         * 相机打开失败
         */
        void onCameraOpenFailed();

        /**
         * 每一帧的 yuv 回调
         */
        void onGetYuvData(byte[] data, Camera.Size previewSize);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCallBack != null && mCamera != null && mPreviewSize != null) {
            mCallBack.onGetYuvData(data, mPreviewSize);
        }
        captureYuvFrame(data);
    }

    /**
     * 启动预览
     */
    public void startPreviewAndCallBack() {
        if (cameraEnable() && mSurfaceTextureAvailable) {
            startPreview(this);
            mCamera.setPreviewCallback(this);
        }
    }

    /**
     * 停止预览
     */
    public void stopPreviewAndCallBack() {
        try {
            if (cameraEnable()) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
            }
        } catch (Exception ignored) {

        }

    }


    protected void resizeView(Camera.Size size) {
//        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
//        int viewWidth = getViewWidth();
//        int viewHeight = getViewHeight();
//        int realWidth = (int) (viewHeight * size.height / (float) size.width);
//        int offset = (realWidth - viewWidth) / 2;
//        LogUtil.sdkLogE("realWidth:" + realWidth + ",offset：" + offset + ",viewWidth=" + viewWidth);
//        params.leftMargin = -offset;
//        params.rightMargin = -offset;
//        setLayoutParams(params);
    }

    /**
     * 视图回调
     */
    protected SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (mPreviewSize != null) {
                resizeView(mPreviewSize);
            }
            mSurfaceTextureAvailable = true;
            startPreviewAndCallBack();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mSurfaceTextureAvailable = false;
            stopPreviewAndCallBack();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            if (mCallBack != null) {
                mCallBack.onSurfaceTextureUpdated(surface);
            }
        }
    };

    /**
     * 开启预览
     */
    public void startPreview(GuardianCameraView textureView) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(textureView.getSurfaceTexture());
                mCamera.startPreview();
            } catch (IOException e) {
            }
        }
    }

    protected int getViewWidth() {
        return mMeasureWidth;
    }

    protected int getViewHeight() {
        return mMeasureHeight;
    }

    /**
     * 是否是竖屏
     */
    protected boolean isPortrait() {
        return getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;//是否是竖屏
    }


    /**
     * 获取相机旋转角度
     */
    protected int getCameraAngle(int cameraId) {
        return CameraUtils2.getCameraAngle(cameraId, mActivity);
    }

    public float getCameraTransformWidthRatio() {
        return mCameraTransformWidthRatio;
    }

    public float getCameraTransformHeightRatio() {
        return mCameraTransformHeightRatio;
    }

    private Bitmap scaleFrontBitmap(Bitmap bitmap) {
        Bitmap resultBitmap;
        if (isFrontCamera()) {
            Matrix matrix = new Matrix();
            matrix.postScale(-1, 1);
            resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
        } else {
            resultBitmap = bitmap;
        }

        return resultBitmap;

    }

    /**
     * 将此帧输出为 bitmap
     */
    protected void captureYuvFrame(final byte[] data) {
        if (mCaptureCurrentFrame) {
            // 将本帧存储，进行图像处理
            mCaptureCurrentFrame = false;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    final Bitmap originalBitmap = BitmapUtil.parseYUVToBitmap(data, mPreviewSize.width, mPreviewSize.height, mCameraAngle, isPortrait(), isFrontCamera());

                    if (mCropRect == null) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mActivity != null && mTakePhotoCallback != null) {
                                    mTakePhotoCallback.onSuccess(scaleFrontBitmap(originalBitmap));
                                }
                            }
                        });

                    } else {
                        float topRatio = mCropRect.top / (float) (getViewHeight()) * getCameraTransformHeightRatio();
                        float leftRatio = mCropRect.left / (float) getViewWidth() * getCameraTransformWidthRatio();
                        float rightRatio = mCropRect.right / (float) getViewWidth() * getCameraTransformWidthRatio();
                        float bottomRatio = mCropRect.bottom / (float) getViewHeight() * getCameraTransformHeightRatio();
                        int bitmapCropLeft = (int) (leftRatio * originalBitmap.getWidth());
                        int bitmapCropTop = (int) (topRatio * originalBitmap.getHeight());
                        int bitmapCropWidth = (int) ((rightRatio - leftRatio) * originalBitmap.getWidth());
                        int bitmapCropHeight = (int) ((bottomRatio - topRatio) * originalBitmap.getHeight());

                        try {
                            int bitmapWidth = originalBitmap.getWidth();
                            int bitmapHeight = originalBitmap.getHeight();
                            int x, y, width, height;
                            if (isFrontCamera()) {//前置摄像头左右颠倒
                                x = (int) ((1 - rightRatio) * originalBitmap.getWidth());
                                y = bitmapCropTop;
                                width = bitmapCropWidth;
                                height = bitmapCropHeight;

                            } else {
                                x = bitmapCropLeft;
                                y = bitmapCropTop;
                                width = bitmapCropWidth;
                                height = bitmapCropHeight;
                            }
                            if (x + width > bitmapWidth) {
                                width = bitmapWidth - x;
                            }
                            if (y + height > bitmapHeight) {
                                height = bitmapHeight - y;
                            }
                            if (width > originalBitmap.getWidth()) width = originalBitmap.getWidth();
                            if (height > originalBitmap.getHeight()) height = originalBitmap.getHeight();
                            final Bitmap resultBitmap = Bitmap.createBitmap(originalBitmap, x, y, width, height, null, false);
                            originalBitmap.recycle();
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mActivity != null && mTakePhotoCallback != null) {
                                        mTakePhotoCallback.onSuccess(scaleFrontBitmap(resultBitmap));
                                    }
                                }
                            });
                        } catch (Exception ignored) {
                            ignored.printStackTrace();
                        }
                    }
                }
            };
            getExecutor().execute(runnable);
        }
    }

    public void takeCropPhoto(Rect cropRect, TakePhotoCallback callback) {
        mTakePhotoCallback = callback;
        mCropRect = cropRect;
        mCaptureCurrentFrame = true;
    }

    public void takePhoto(TakePhotoCallback callback) {
        mTakePhotoCallback = callback;
        mCaptureCurrentFrame = true;
    }

    protected Rect mCropRect;
    protected TakePhotoCallback mTakePhotoCallback;

    public interface TakePhotoCallback {
        void onSuccess(Bitmap bitmap);
    }
}
