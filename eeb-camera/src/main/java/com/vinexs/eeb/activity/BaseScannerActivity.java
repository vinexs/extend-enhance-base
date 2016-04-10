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

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;

import com.vinexs.eeb.BaseActivity;
import com.vinexs.eeb.camera.R;
import com.vinexs.eeb.fragment.BaseFragCamera;

@SuppressWarnings("unused")
public abstract class BaseScannerActivity extends BaseActivity implements BaseFragCamera.Callback {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addFragment(R.id.frame_content, new BaseFragCamera());
    }

    @Override
    public void onShutter() {
    }

    @Override
    public abstract void onNewIntent(Intent intent);

    @Override
    public abstract void onBaseBackStackChanged(boolean noTurnBack, int entryCount);

    @SuppressWarnings("deprecation")
    @Override
    public abstract void onPreviewFrame(byte[] bytes, Camera camera);

    @SuppressWarnings("deprecation")
    @Override
    public abstract void onPictureTaken(byte[] data, Camera camera);
}
