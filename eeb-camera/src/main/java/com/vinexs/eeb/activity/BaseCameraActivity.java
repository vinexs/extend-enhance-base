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

package com.vinexs.eeb.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.vinexs.eeb.BaseActivity;
import com.vinexs.eeb.BaseFragment;
import com.vinexs.eeb.camera.R;
import com.vinexs.eeb.fragment.BaseFragCamera;
import com.vinexs.eeb.misc.BundleArgs;

@SuppressWarnings({"unused", "deprecation"})
public abstract class BaseCameraActivity extends BaseActivity implements BaseFragCamera.Callback {

    public static final int CAMERA_PERMISSION_REQUEST_CODE = 65001;


    @Override
    public int getContentFrameResId() {
        return R.layout.frame_base_activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            addCameraFragment();
        }
    }

    public void permissionDeniedAction() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseCameraActivity.this, R.string.camera_require_permission, Toast.LENGTH_LONG).show();
                finishActivity(RESULT_CANCELED);
            }
        }, 3000);
    }

    public void addCameraFragment() {
        Intent intent = getIntent();
        BaseFragment cameraFrag = (BaseFragment) getCameraFragment();
        if (cameraFrag == null) {
            Log.e("Camera", "Camera Fragment missing. Please initial code in getCameraFragment().");
            return;
        }
        Bundle args = getIntent().getExtras();
        args.putBoolean(BundleArgs.addToBackStack, false);
        cameraFrag.setArguments(args);
        addFragment(R.id.frame_content, cameraFrag);
    }

    /* Abstract Methods */

    public abstract int getLayoutResId();

    public abstract Fragment getCameraFragment();

    @Override
    public abstract void onPreviewFrame(final byte[] bytes, Camera camera, final int previewWidth, final int previewHeight);

    @Override
    public abstract void onPictureTaken(final byte[] data, Camera camera, final int pictureWidth, final int pictureHeight);

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                Boolean hasGrant = false;
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        hasGrant = true;
                    }
                }
                if (hasGrant) {
                    addCameraFragment();
                } else {
                    permissionDeniedAction();
                }
                break;
        }
    }
}
