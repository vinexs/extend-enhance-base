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

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

@SuppressWarnings("unused")
public abstract class AsyncProgressDialog extends AsyncTask<Object, Void, Integer> {

    public ProgressDialog pdia;

    public Activity activity = null;

    public String message = "";

    public AsyncProgressDialog(Activity activity, int resId) {
        this.activity = activity;
        message = activity.getResources().getString(resId);
    }

    public AsyncProgressDialog(Activity activity, String str) {
        this.activity = activity;
        message = str;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pdia = new ProgressDialog(activity);
        pdia.setMessage(message);
        pdia.show();
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        pdia.dismiss();
        afterExecute();
    }

    public void dismiss() {
        pdia.dismiss();
    }

    public void changeTitle(final String title) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pdia.setTitle(title);
            }
        });
    }

    public void changeMessage(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pdia.setMessage(message);
            }
        });
    }

    /**
     * Thing to do in background.
     */
    @Override
    protected abstract Integer doInBackground(Object... arg0);


    /**
     * Thing to do after background job is finished.
     */
    protected abstract void afterExecute();

}
