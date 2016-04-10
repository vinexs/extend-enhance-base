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
package com.vinexs.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.vinexs.eeb.misc.BaseWebChromeClient;
import com.vinexs.eeb.misc.BaseWebViewClient;
import com.vinexs.view.abst.AbstractEnhancedWebView;

@SuppressWarnings("unused")
public class EnhancedWebView extends AbstractEnhancedWebView {
    /**
     * <pre>Enhanced WebView has extra feature than normal Webview.
     * It's basically support loading effect, auto config WebChromeClient & WebViewClient.
     * Also able to detect scrolling event and scroll bottom event.
     *
     * @param context The context to use. Usually your Application or Activity object.</pre>
     */
    public EnhancedWebView(Context context) {
        super(context);
        initialize();
    }

    public EnhancedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public EnhancedWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    @Override
    public BaseWebChromeClient getBaseWebChromeClient() {
        return new DefaultWebChromeClient(getContext());
    }

    @Override
    public BaseWebViewClient getBaseWebViewClient() {
        return new DefaultWebViewClient(getContext());
    }

    public class DefaultWebChromeClient extends BaseWebChromeClient {
        public DefaultWebChromeClient(Context context) {
            super(context);
        }
    }

    public class DefaultWebViewClient extends BaseWebViewClient {
        public DefaultWebViewClient(Context context) {
            super(context);
        }

        @Override
        public void onHttpError(WebView view, int errorCode) {
        }

        @Override
        public void onLoadingStart(WebView view, String url) {
            setLoading(true);
        }

        @Override
        public void onLoadingEnd(WebView view, String url) {
            setLoading(false);
        }
    }

    @Override
    public void onScrollUp() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onScrollDown() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onScrollBottom() {
        // TODO Auto-generated method stub
    }
}
