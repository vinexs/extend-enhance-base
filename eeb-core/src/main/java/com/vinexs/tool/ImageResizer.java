/*
 * Copyright (c) 2015. Vin @ vinexs.com (MIT License)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.vinexs.tool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@SuppressWarnings("unused")
public class ImageResizer {
    public String filePath = null;

    public ImageResizer(String filePath) {
        this.filePath = filePath;
    }

    public Bitmap readWithinSize(float targetWidth, float targetHeight) {
        InputStream inputStream;
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeFile(this.filePath, options);
        } catch (Exception e) {
            Log.e("ImageResizer", "Unable to decode file.", e);
            return null;
        }
        if (options.outWidth > targetWidth || options.outHeight > targetHeight) {
            Log.d("ImageResizer", "Image file is larger than expected, process resize.");
            try {
                inputStream = new FileInputStream(new File(this.filePath));
            } catch (Exception e) {
                Log.e("ImageResizer", "Unable to read file.", e);
                return null;
            }
            bitmap = BitmapFactory.decodeStream(inputStream);
            int scaWidth;
            int scaHeight;
            if (options.outHeight > options.outWidth) {
                scaWidth = (int) ((float) options.outWidth / ((float) options.outHeight / targetHeight));
                scaHeight = (int) targetHeight;
            } else {
                scaWidth = (int) targetWidth;
                scaHeight = (int) ((float) options.outHeight / ((float) options.outWidth / targetWidth));
            }
            Matrix matrix = new Matrix();
            float ScaleWidthRatio = (float) scaWidth / (float) options.outWidth;
            float ScaleHeightRatio = (float) scaHeight / (float) options.outHeight;
            matrix.postScale(ScaleWidthRatio, ScaleHeightRatio);
            Log.d("ImageResizer", "Original Image Size is " + options.outWidth + " : " + options.outHeight + ".");
            Log.d("ImageResizer", "Return Image Size is " + scaWidth + " : " + scaHeight + ".");
            Log.d("ImageResizer", "Resize ratio " + ScaleWidthRatio + " : " + ScaleHeightRatio + ".");
            return Bitmap.createBitmap(bitmap, 0, 0, options.outWidth, options.outHeight, matrix, true);
        } else {
            Log.d("ImageResizer", "Image file size in range.");
            try {
                inputStream = new FileInputStream(new File(this.filePath));
            } catch (Exception e) {
                Log.d("ImageResizer", "Read Image File with exception -> " + e.toString());
                return null;
            }
            return BitmapFactory.decodeStream(inputStream);
        }
    }

    public Bitmap rotate(int degree) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(this.filePath, options);
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageHeight, matrix, true);
    }

}
