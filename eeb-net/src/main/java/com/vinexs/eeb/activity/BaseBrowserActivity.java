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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.vinexs.eeb.BaseActivity;
import com.vinexs.eeb.BaseFragment;
import com.vinexs.eeb.misc.BaseWebViewClient;
import com.vinexs.eeb.misc.BundleArgs;
import com.vinexs.eeb.net.R;
import com.vinexs.tool.Utility;

import java.net.URI;

public abstract class BaseBrowserActivity extends BaseActivity {

    @Override
    public int getContentFrame() {
        return R.layout.frame_base_activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCloseAction(CLOSE_ACTION_NONE);

        Log.i("Browser", "For minSdkVersion >= 15 (ICE_CREAM_SANDWICH_MR1), we recommend to use Chrome Custom Tabs.");

        Intent intent = getIntent();
        BrowserFragment browserFrag = new BrowserFragment();
        Bundle args = intent == null ? new Bundle() : intent.getExtras();
        args.putBoolean(BundleArgs.addToBackStack, false);
        browserFrag.setArguments(args);
        addFragment(browserFrag);
    }

    @Override
    public void onBackPressed() {
        Utility.hideKeyBroad(this);
        try {
            if (allowBack) {
                // Handle Navigation Drawer
                if (drawerLayout != null &&
                        (drawerLayout.isDrawerOpen(GravityCompat.START) ||
                                drawerLayout.isDrawerOpen(GravityCompat.END))) {
                    closeLeftDrawer();
                    closeRightDrawer();
                    return;
                }

                // Handle browser fragment layer.
                BrowserFragment browserFrag;
                browserFrag = (BrowserFragment) getExistFragment("BrowserFragment");
                if (browserFrag != null) {
                    if (browserFrag.canGoBack()) {
                        browserFrag.goBack();
                    } else {
                        closeAppsConfirmation();
                    }
                    return;
                }

                closeAppsConfirmation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class BrowserFragment extends BaseFragment {

        public WebView webView;
        public SwipeRefreshLayout srLayout;
        private Toolbar toolbar;

        @Override
        public int getLayoutResId() {
            return R.layout.lib_default_web_browser;
        }

        @Override
        public int getToolbarResId() {
            return R.id.lib_web_browser_toolbar;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            MenuItem openItem = menu.add(R.string.open_external);
            openItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    String url = webView.getUrl();
                    if (url != null) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        getActivity().startActivity(browserIntent);
                    }
                    return true;
                }
            });
        }

        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            toolbar = (Toolbar) getActivity().findViewById(getToolbarResId());
            toolbar.setTitleTextAppearance(getActivity(), R.style.LibWebBrowserTitleTextAppearance);
            toolbar.setSubtitleTextAppearance(getActivity(), R.style.LibWebBrowserSubtitleTextAppearance);

            webView = (WebView) view.findViewById(R.id.lib_web_browser);
            webView.setVerticalScrollBarEnabled(true);
            webView.setHorizontalScrollBarEnabled(true);

            WebSettings setting = webView.getSettings();
            setting.setJavaScriptEnabled(true);
            setting.setAppCacheEnabled(false);
            setting.setAllowFileAccess(false);
            setting.setDomStorageEnabled(false);
            setting.setUseWideViewPort(true);
            setting.setSupportZoom(true);

            srLayout = (SwipeRefreshLayout) view.findViewById(R.id.lib_web_browser_swiperefresh);
            srLayout.setEnabled(true);
            srLayout.setNestedScrollingEnabled(true);
            srLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    srLayout.setRefreshing(true);
                    webView.reload();
                }
            });
            webView.setWebViewClient(new BaseWebViewClient(getActivity()) {

                @Override
                public void onHttpError(WebView view, int errorCode) {
                }

                @Override
                public void onLoadingStart(WebView view, String url) {
                }

                @Override
                public void onLoadingEnd(WebView view, String url) {
                    srLayout.setRefreshing(false);
                    if (toolbar != null) {
                        toolbar.setTitle(view.getTitle());
                        try {
                            URI uri = new URI(url);
                            toolbar.setSubtitle(uri.getHost());
                        } catch (Exception e) {
                            toolbar.setSubtitle("-");
                        }
                    }
                }
            });
        }

        @Override
        public void onStart() {
            super.onStart();
            String currentUrl = webView.getUrl();
            if (currentUrl == null || currentUrl.isEmpty()) {
                if (args.containsKey("url")) {
                    String url = args.getString("url");
                    if (toolbar != null) {
                        toolbar.setTitle(url);
                    }
                    webView.loadUrl(url);
                } else {
                    Log.e("Browser", "Activity require intent argument [url:String] to start.");
                }

            }
        }

        @Override
        public void onResume() {
            super.onResume();
            String currentUrl = webView.getUrl();
            if (currentUrl != null && !currentUrl.isEmpty() && toolbar != null) {
                toolbar.setTitle(webView.getTitle());
                try {
                    URI uri = new URI(currentUrl);
                    toolbar.setSubtitle(uri.getHost());
                } catch (Exception e) {
                    toolbar.setSubtitle("-");
                }
            }
        }

        public boolean canGoBack() {
            return webView.canGoBack();
        }

        public void goBack() {
            webView.goBack();
        }

    }
}
