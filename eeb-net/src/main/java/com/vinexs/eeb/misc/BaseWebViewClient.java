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
package com.vinexs.eeb.misc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.net.URL;

//import android.webkit.WebResourceError;

@SuppressWarnings("unused")
public abstract class BaseWebViewClient extends WebViewClient {

    public float scale = 1;
    protected Context context = null;
    protected AssetManager assetMgr = null;

    /**
     * BasicWebViewClient will use in EnhancedWebView or somewhere else.
     * It provide enhanced error handling and assets control.
     *
     */
    public BaseWebViewClient(Context context) {
        this.context = context;
        assetMgr = context.getAssets();
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        onLoadingStart(view, url);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        onLoadingEnd(view, url);
        super.onPageFinished(view, url);
    }

    /*
    @SuppressLint("NewApi")
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        switch (error.getErrorCode()) {
            // Server return file not found
            case WebViewClient.ERROR_FILE_NOT_FOUND:
                Log.e("BasicWebViewClient", "Url: " + request.getUrl() + " " + error.getDescription());
                onHttpError(view, 404);
                break;
            // Connection timeout
            case WebViewClient.ERROR_TIMEOUT:
                Log.e("BasicWebViewClient", "Url: " + request.getUrl() + " " + error.getDescription());
                onHttpError(view, 408);
                break;
            // Secure connection fail
            case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
                Log.e("BasicWebViewClient", "Url: " + request.getUrl() + " " + error.getDescription());
                onHttpError(view, 497);
                break;
            // No connection to server
            case WebViewClient.ERROR_CONNECT:
                Log.e("BasicWebViewClient", "Url: " + request.getUrl() + " " + error.getDescription());
                onHttpError(view, 503);
                break;
            // No connection to internet.
            case WebViewClient.ERROR_HOST_LOOKUP:
                Log.e("BasicWebViewClient", "Url: " + request.getUrl() + " " + error.getDescription());
                onHttpError(view, 444);
                break;
        }
    }
    */

    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Log.e("WebView", "Url: " + failingUrl + " " + description);
        switch (errorCode) {
            // Server return file not found
            case WebViewClient.ERROR_FILE_NOT_FOUND:
                onHttpError(view, 404);
                break;
            // Connection timeout
            case WebViewClient.ERROR_TIMEOUT:
                onHttpError(view, 408);
                break;
            // Secure connection fail
            case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
                onHttpError(view, 497);
                break;
            // No connection to server
            case WebViewClient.ERROR_CONNECT:
                onHttpError(view, 503);
                break;
            // No connection to internet.
            case WebViewClient.ERROR_HOST_LOOKUP:
                onHttpError(view, 444);
                break;
        }
    }

    @SuppressLint("NewApi")
    public WebResourceResponse shouldInterceptRequest (WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        try {
            URL urlData = new URL(url);
            String path = urlData.getPath().substring(1);
            if (!path.endsWith(".js") && !path.endsWith(".css")) {
                return null;
            }
            Log.d("BasicWebViewClient", "Find " + path + " from asset.");
            InputStream localStream = assetMgr.open(path);
            Log.d("BasicWebViewClient", url + " found, try load from asset.");
            return new WebResourceResponse((path.endsWith(".js") ? "text/javascript" : "text/css"), "UTF-8", localStream);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        try {
            URL urlData = new URL(url);
            String path = urlData.getPath().substring(1);
            if (!path.endsWith(".js") && !path.endsWith(".css")) {
                return null;
            }
            Log.d("BasicWebViewClient", "Find " + path + " from asset.");
            InputStream localStream = assetMgr.open(path);
            Log.d("BasicWebViewClient", url + " found, try load from asset.");
            return new WebResourceResponse((url.contains(".js") ? "text/javascript" : "text/css"), "UTF-8", localStream);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        scale = newScale;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }


    /**
     * <pre>Handle http error manually.
     * <p/>
     * view.stopLoading();
     * view.loadDataWithBaseURL( "local://base/asset", "&lt;HTML CODE>" , "text/html", "utf-8", view.getUrl() );
     * </pre>
     */
    public abstract void onHttpError(WebView view, int errorCode);

    public abstract void onLoadingStart(WebView view, String url);

    public abstract void onLoadingEnd(WebView view, String url);

}
