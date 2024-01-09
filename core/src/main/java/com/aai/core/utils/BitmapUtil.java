package com.aai.core.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;

/**
 * 图片裁剪类
 */
public class BitmapUtil {
    /**
     * 默认的裁剪格式
     */
    public static Bitmap.Config defaultConfig = Bitmap.Config.RGB_565;

    /**
     * 获取裁剪后的bitmap
     *
     * @param data       yuv格式的data
     * @param left       裁剪点左坐标
     * @param top        裁剪点上坐标
     * @param cropWidth  裁剪区域宽度
     * @param cropHeight 裁剪区域高度
     * @param width      预览宽度
     * @param height     预览高度
     * @return
     */
    public static Bitmap parseYuvToCropBitmap(byte[] data, int left, int top, int cropWidth, int cropHeight, int width, int height, boolean isFrontCamera, boolean isPortrait) {//width

        YuvImage localYuvImage = new YuvImage(data, 17, width, height, null);

        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        //先对图片进行裁剪
        if (left < 0) left = 0;
        if (top < 0) top = 0;

        int right = cropWidth + left - 1;
        int bottom = cropHeight + top - 1;

        if (right > width) right = width;
        if (bottom > height) bottom = height;

        localYuvImage.compressToJpeg(new Rect(left, top, right, bottom), 80, localByteArrayOutputStream);

        Bitmap bmp = decodeRGBStreamToBitmap(localByteArrayOutputStream);
        //需要对图片进行旋转
        return isPortrait ? rotateBitmap(bmp, isFrontCamera ? -90 : -270) : bmp;
    }

    /**
     * 将 rgb 流转成 bitmap
     *
     * @param localByteArrayOutputStream
     * @return
     */
    public static Bitmap decodeRGBStreamToBitmap(ByteArrayOutputStream localByteArrayOutputStream, BitmapFactory.Options localOptions) {
        byte[] mParamArrayOfByte = localByteArrayOutputStream.toByteArray();
        Bitmap bmp = decodeRGBByteToBitmap(mParamArrayOfByte, localOptions);
        if (localByteArrayOutputStream != null) {
            try {
                localByteArrayOutputStream.close();
            } catch (IOException e) {
            }
        }
        return bmp;
    }

    /**
     * 将 rgb 流转成 bitmap
     *
     * @return
     */
    public static Bitmap decodeRGBByteToBitmap(byte[] mParamArrayOfByte, BitmapFactory.Options localOptions) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mParamArrayOfByte);

        SoftReference softReference = new SoftReference(BitmapFactory.decodeStream(inputStream, null, localOptions));
        Bitmap bmp = (Bitmap) softReference.get();
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
        return bmp;
    }

    /**
     * 将 rgb 流转成 bitmap
     *
     * @return
     */
    public static Bitmap decodeRGBByteToBitmap(byte[] mParamArrayOfByte) {
        BitmapFactory.Options localOptions = new BitmapFactory.Options();
        localOptions.inPreferredConfig = defaultConfig;
        return decodeRGBByteToBitmap(mParamArrayOfByte, localOptions);
    }

    /**
     * 将 rgb 流转成 bitmap
     *
     * @param localByteArrayOutputStream
     * @return
     */
    public static Bitmap decodeRGBStreamToBitmap(ByteArrayOutputStream localByteArrayOutputStream) {
        BitmapFactory.Options localOptions = new BitmapFactory.Options();
        localOptions.inPreferredConfig = defaultConfig;
        return decodeRGBStreamToBitmap(localByteArrayOutputStream, localOptions);
    }


    /**
     * 获取原图
     *
     * @param data
     * @param width
     * @param height
     * @param isFrontCamera
     * @return
     * @deprecated 请使用带相机角度的
     */
    public static Bitmap parseYUVToBitmap(byte[] data, int width, int height, boolean isPortrait, boolean isFrontCamera) {
        return parseYUVToBitmap(data, width, height, 0, isPortrait, isFrontCamera);
    }

    /**
     * 获取原图
     *
     * @param data
     * @param width
     * @param height
     * @param isFrontCamera
     * @return
     */
    public static Bitmap parseYUVToBitmap(byte[] data, int width, int height, int cameraAngle, boolean isPortrait, boolean isFrontCamera) {
        YuvImage localYuvImage = new YuvImage(data, 17, width, height, null);
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();

        localYuvImage.compressToJpeg(new Rect(0, 0, width, height), 80, localByteArrayOutputStream);
        Bitmap bmp = decodeRGBStreamToBitmap(localByteArrayOutputStream);
        //需要对图片进行旋转
        return isPortrait ? rotateBitmap(bmp, cameraAngle - (isFrontCamera ? 180 : 360)) : rotateBitmap(bmp, cameraAngle);
    }

    /**
     * 获取裁剪后的bitmap
     *
     * @param data yuv格式的data
     * @param rect left,right,width,height
     * @return
     */
    public static Bitmap parseYuvToCropBitmap(byte[] data, int[] rect, boolean isFrontCamera, boolean isPortrait) {
        //我们需要裁剪的图片的四个点
        int left = rect[0];
        int top = rect[1];
        int cropWidth = rect[2];
        int cropHeight = rect[3];
        int width = rect[4];
        int height = rect[5];
        return parseYuvToCropBitmap(data, left, top, cropWidth, cropHeight, width, height, isFrontCamera, isPortrait);
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param rotate 旋转角度，可正可负
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float rotate) {
        if (rotate == 0) {
            return origin;
        } else {
            if (origin == null) {
                return null;
            }
            int width = origin.getWidth();
            int height = origin.getHeight();
            Matrix matrix = new Matrix();
            matrix.setRotate(rotate);
            // 围绕原地进行旋转
            Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
            origin.recycle();
            return newBM;
        }
    }

    /**
     * @param image ARGB888
     * @return
     */
    public static byte[] getPixelsARGB(Bitmap image) {
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] argbBuffer = buffer.array(); // Get the underlying array containing the
        //image.recycle();
        //buffer.clear();
        return argbBuffer;
    }

    /**
     * 将 bitmap 转成 byte 数组
     *
     * @param bitmap
     * @return
     */
    public static byte[] parseBitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getByteCount());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] bytes = outputStream.toByteArray();
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
            }
        }
        return bytes;
    }


}
