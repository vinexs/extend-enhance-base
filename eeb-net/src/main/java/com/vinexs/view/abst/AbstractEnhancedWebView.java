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
package com.vinexs.view.abst;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.vinexs.eeb.misc.BaseWebChromeClient;
import com.vinexs.eeb.misc.BaseWebViewClient;
import com.vinexs.eeb.net.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("unused")
public abstract class AbstractEnhancedWebView extends WebView {
    public GestureDetector gestureDetector;
    public AtomicBoolean preventAction = new AtomicBoolean(false);
    public AtomicLong preventActionTime = new AtomicLong(0);

    public boolean hasLoadingEffect = false;
    public View loadingScreen = null;
    public int defaultLoadingLayout = R.layout.lib_default_web_loading;

    public WebSettings webSettings = null;

    /**
     * <pre>Enhanced WebView has extra feature than normal Webview.
     * It's basically support loading effect, auto config WebChromeClient & WebViewClient.
     * Also able to detect scrolling event and scroll bottom event.
     *
     * @param context The context to use. Usually your Application or Activity object.
     *                </pre>
     */
    public AbstractEnhancedWebView(Context context) {
        super(context);
        initialize();
    }

    public AbstractEnhancedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public AbstractEnhancedWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initialize() {
        this.setBackgroundColor(0x00000000);
        this.setVerticalScrollBarEnabled(false);

        webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Set Secuity to webview
        webSettings.setSaveFormData(false);

        //Found path
        String cachePath = getContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File extCacheDir = getContext().getExternalCacheDir();
                if (extCacheDir == null) {
                    throw new Exception("Unable to get external cache directory.");
                }
                cachePath = getContext().getExternalCacheDir().toString();
            } else {
                cachePath = getContext().getApplicationContext().getFileStreamPath(null).toString();
            }
        } catch (Exception e) {
            Log.d("ExtendedWebView", "Use internal storage to store cache.[" + cachePath + "]");
        }

        //Enable Cache
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(cachePath);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAllowFileAccess(true);

        // Enable DOM storage
        webSettings.setDomStorageEnabled(true);

        // Display
        setInitialScale(1);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        setVerticalScrollBarEnabled(true);
        setHorizontalScrollBarEnabled(false);

        // HTML5

        // Enable HTML5 geolocation
        webSettings.setGeolocationEnabled(true);
        // Enable HTML5 database
        webSettings.setDatabaseEnabled(true);

        // Set Default WebView Plugin
        setWebViewPlugin();

        setHighRenderPriority();

        setRequireLayerType();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            jellyBeanSetting();
        }

        //Set Gesture Handle
        gestureDetector = new GestureDetector(getContext().getApplicationContext(), new GestureListener());

    }

    /**
     * Override [basicWebViewClient] , [basicWebChromeClient] if not using default plugin control.
     */
    public void setWebViewPlugin() {
        // Set Default Web Chrome Clinet
        if (getBaseWebChromeClient() == null) {
            Log.d("EnhancedWebView", "Basic Web Chrome Client is not set.");
        } else {
            setWebChromeClient(getBaseWebChromeClient());
        }

        // Set Default Web View Clinet
        if (getBaseWebViewClient() == null) {
            Log.d("EnhancedWebView", "Basic Web View Client is not set.");
        }
        setWebViewClient(getBaseWebViewClient());
    }


    /**
     * <pre>Set Enhanced WebView has its independent loading effect.
     * To exchange independent loading effect, the enhanced webview must place
     * inside an RelativeLayout.
     * </pre>
     */
    public void setHasLoadingScreen() {
        setHasLoadingScreen(defaultLoadingLayout);
    }

    /**
     * <pre>Set Enhanced WebView has its independent loading effect.
     * To exchange independent loading effect, the enhanced webview must place inside
     * an RelativeLayout.
     *
     * @param loadingScreenLayout Custom loading layout id. (Example: R.layout.loading)
     *                            </pre>
     */
    public void setHasLoadingScreen(int loadingScreenLayout) {
        try {
            ViewParent parent = getParent();
            if (parent == null) {
                throw new Exception("There is no parent of this webview.");
            }
            RelativeLayout relateLayout = (RelativeLayout) parent;
            LayoutInflater inflater = LayoutInflater.from(getContext());
            loadingScreen = inflater.inflate(loadingScreenLayout, relateLayout, false);
            try {
                loadingScreen.findViewById(R.id.loading_layout).setOnTouchListener(new OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            } catch (Exception ignored) {
            }
            relateLayout.addView(loadingScreen);
            loadingScreen.setVisibility(View.GONE);
            hasLoadingEffect = true;
        } catch (Exception e) {
            Log.d("EnhancedWebView", "Cannot add loading effect because Parent is not RelativeLayout.");
            Log.d("EnhancedWebView", e.toString());
        }
    }

    public void setLoading(boolean loading) {
        if (!hasLoadingEffect) {
            return;
        }
        loadingScreen.setVisibility((loading) ? View.VISIBLE : View.GONE);
    }

    public void postUrl(String url, Map<String, String> postData) {
        byte[] postEncode = "".getBytes();
        if (postData != null) {
            String postString = "";
            Set<String> mapKeys = postData.keySet();
            try {
                for (String key : mapKeys) {
                    postString += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(postData.get(key), "UTF-8") + "&";
                }
                postEncode = postString.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        postUrl(url, postEncode);
    }

    public void setUserAgentString(String userAgent) {
        webSettings.setUserAgentString(userAgent);
    }

    @SuppressWarnings("deprecation")
    public void setHighRenderPriority() {
        try {
            webSettings.setRenderPriority(RenderPriority.HIGH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setRequireLayerType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @SuppressLint("NewApi")
    public void evaluateJavascript(final String script) {
        try {
            final WebView webview = this;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webview.post(new Runnable() {
                    @Override
                    public void run() {
                        webview.evaluateJavascript(script, null);
                    }
                });
            } else {
                webview.post(new Runnable() {
                    @Override
                    public void run() {
                        webview.loadUrl("javascript:" + script);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (deltaY > 0 && scrollY > 0) {
            onScrollDown();
        } else if (deltaY < 0 && scrollY < scrollRangeY) {
            onScrollUp();
        }
        if (scrollY + getMeasuredHeight() >= getContentHeight()) {
            onScrollBottom();
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int temp_ScrollY = getScrollY();
            scrollTo(getScrollX(), getScrollY() + 1);
            scrollTo(getScrollX(), temp_ScrollY);
        }
        int index = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        int pointId = event.getPointerId(index);
        if (pointId == 0) {
            gestureDetector.onTouchEvent(event);
            if (preventAction.get()) {
                if (System.currentTimeMillis() - preventActionTime.get() > ViewConfiguration.getDoubleTapTimeout()) {
                    preventAction.set(false);
                } else {
                    return true;
                }
            }
            return super.onTouchEvent(event);
        } else {
            return true;
        }
    }

    public int getScrollRange() {
        float currentScale = (getBaseWebViewClient() != null) ? getBaseWebViewClient().scale : 1;
        return (int) Math.max(0, Math.floor(this.getContentHeight() * currentScale) - (getHeight() - getPaddingBottom() - getPaddingTop()));
    }

    public void onLoadCompleted(WebView webView) {
        webView.requestFocus(View.FOCUS_DOWN | View.FOCUS_UP);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void jellyBeanSetting() {
        webSettings.setAllowUniversalAccessFromFileURLs(true);
    }

    @Override
    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    public void addJavascriptInterface(Object object, String name) {
        super.addJavascriptInterface(object, name);
    }

    public void setDisableLongClick() {
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            preventAction.set(true);
            preventActionTime.set(System.currentTimeMillis());
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            preventAction.set(true);
            preventActionTime.set(System.currentTimeMillis());
            return true;
        }
    }

    public abstract BaseWebChromeClient getBaseWebChromeClient();

    public abstract BaseWebViewClient getBaseWebViewClient();

    /**
     * Scroll up event listener.
     */
    public abstract void onScrollUp();


    /**
     * Scroll down event listener.
     */
    public abstract void onScrollDown();


    /**
     * Scroll bar reach bottom event listener
     */
    public abstract void onScrollBottom();

	/* Example of Javascript Interface

	public class javascriptInterface{}

	*/
}
