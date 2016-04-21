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

package com.vinexs.eeb.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.vinexs.R;
import com.vinexs.eeb.BaseFragment;

import java.util.Calendar;

/**
 * <p>A splash screen is a graphical control element consisting of window containing an image, a logo and the current version of the software.
 * A splash screen usually appears while a game or program is launching. </p>
 * <p>To use this as a splash screen ,add this fragment to R.id.frame_full when application activiy onCreate().</p>
 *
 * @author Vin Wong
 */
@SuppressWarnings("unused")
public abstract class BaseSplashFragment extends BaseFragment {

    protected long timeStart = 0;
    protected TextView txtVersion;
    protected TextView txtStatus;
    private ProgressDialog progressDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getBaseActivity().lockBackPress();
        timeStart = Calendar.getInstance().getTimeInMillis();
        onPrepareSplash();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getLayoutResId() == 0) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();
            executeProgress(null);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        executeProgress(view);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getBaseActivity().unlockBackPress();
    }

    private void executeProgress(@Nullable View view) {
        if (view != null) {
            txtVersion = getVersionTextView(view);
            txtStatus = getStatusTextView(view);
        }
        Log.d(TAG, "Splash screen shown.");
        new AsyncTask<Object, Void, String>() {

            @Override
            protected void onPreExecute() {
                Log.d(TAG, "Splash created.");
                onSplashCreated();
                if (txtVersion != null) {
                    txtVersion.setText(getVersionText());
                }
            }

            @Override
            protected String doInBackground(Object... params) {
                Log.d(TAG, "Splash loading.");
                onSplashLoading();
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d(TAG, "Splash loaded.");
                onSplashLoaded();
                long usedTime = Calendar.getInstance().getTimeInMillis() - timeStart;
                if (usedTime > getSplashLength()) {
                    Log.d(TAG, "Splash closed immediately. ");
                    onSplashClose();
                    afterExecute();
                } else {
                    int delayMillis = (int) (getSplashLength() - usedTime);
                    Log.d(TAG, "Splash closed after " + delayMillis + " milliseconds. ");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                onSplashClose();
                                afterExecute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, delayMillis);
                }
            }

        }.execute(null, null, null);
    }

    public void afterExecute() {
        if (getLayoutResId() == 0) {
            progressDialog.dismiss();
        }
        removeSelf();
    }

    @SuppressWarnings("unused")
    public void setStatusText(int resId) {
        setStatusText(getString(resId));
    }

    public void setStatusText(String statusText) {
        getActivity().runOnUiThread(new Runnable() {
            String text = "";

            @Override
            public void run() {
                if (txtStatus != null) {
                    txtStatus.setText(text);
                } else {
                    progressDialog.setMessage(text);
                }
            }

            public Runnable setDisplayText(String text) {
                this.text = text;
                return this;
            }

        }.setDisplayText(statusText));
    }

    public int getLayoutResId() {
        return getSplashView();
    }

    /**
     * Assign splash layout resource id.
     */
    public abstract int getSplashView();

    /**
     * Assign splash duration.
     */
    public abstract long getSplashLength();

    /**
     * Assign splash duration.
     */
    public abstract String getVersionText();

    /**
     * Assign version TextView.
     */
    public abstract TextView getVersionTextView(View view);

    /**
     * Assign status TextView.
     */
    public abstract TextView getStatusTextView(View view);

    /**
     * Event to trigger before splash show.
     */
    public abstract void onPrepareSplash();

    /**
     * Event to trigger while splash being show.
     */
    public abstract void onSplashCreated();

    /**
     * Event to trigger while splash is showing.
     */
    public abstract void onSplashLoading();

    /**
     * Event to trigger while splash being close.
     */
    public abstract void onSplashLoaded();

    /**
     * Event to trigger after splash close.
     */
    public abstract void onSplashClose();

}
