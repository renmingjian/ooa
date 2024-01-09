package com.aai.core.utils;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;

/**
 * createTime:2019-11-07
 *
 * @author fan.zhang@advance.ai
 */
public class CameraUtils2 {
    /**
     * 获取活体使用的相机参数
     *
     * @return 异常则返回空，表示相机打不开
     */
    public static Camera.CameraInfo getTargetCameraInfo(int cameraId) {
        Camera.CameraInfo info = null;
        try {
            info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
        } catch (Exception e) {
        }
        return info;
    }

    /**
     * 根据facing获取cameraId
     *
     * @param facing CAMERA_FACING_BACK or CAMERA_FACING_FRONT.
     * @return 失败返回-1
     */
    public static int getCameraIdByFacing(int facing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo cameraInfo = getTargetCameraInfo(i);
            if (cameraInfo != null && cameraInfo.facing == facing) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取前置摄像头id
     */
    public static int getFrontCameraId() {
        return getCameraIdByFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    /**
     * 获取前置摄像头id
     */
    public static int getBackCameraId() {
        return getCameraIdByFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /**
     * Obtain camera rotation Angle
     */
    public static int getCameraAngle(int cameraId, Activity activity) {
        int rotateAngle;
        Camera.CameraInfo info = getTargetCameraInfo(cameraId);
        if (info == null) {
            return -1;
        } else {
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotateAngle = (info.orientation + degrees) % 360;
                // compensate the mirror
                rotateAngle = (360 - rotateAngle) % 360;
            } else { // back-facing
                rotateAngle = (info.orientation - degrees + 360) % 360;
            }
            return rotateAngle;
        }
    }
}
