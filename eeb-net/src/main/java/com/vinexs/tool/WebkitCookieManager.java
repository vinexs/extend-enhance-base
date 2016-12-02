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

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieSyncManager;

import java.io.IOException;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@SuppressWarnings({"unused", "deprecation"})
public class WebkitCookieManager extends java.net.CookieManager {

    private android.webkit.CookieManager webkitCookieManager;

    public WebkitCookieManager(Context context) {
        this(context, null, null);
    }

    public WebkitCookieManager(Context context, CookieStore store, CookiePolicy cookiePolicy) {
        super(null, cookiePolicy);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(context);
        }
        webkitCookieManager = android.webkit.CookieManager.getInstance();
        webkitCookieManager.setAcceptCookie(true);
    }

    @Override
    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
        if ((uri == null) || (responseHeaders == null)) {
            return;
        }
        String url = uri.toString();
        for (String headerKey : responseHeaders.keySet()) {
            if ((headerKey == null) || !(headerKey.equalsIgnoreCase("Set-Cookie2") || headerKey.equalsIgnoreCase("Set-Cookie"))) {
                continue;
            }
            for (String headerValue : responseHeaders.get(headerKey)) {
                webkitCookieManager.setCookie(url, headerValue);
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        }
    }

    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
        if ((uri == null) || (requestHeaders == null)) {
            throw new IllegalArgumentException("Argument is null");
        }
        String url = uri.toString();
        Map<String, List<String>> res = new java.util.HashMap<>();
        String cookie = webkitCookieManager.getCookie(url);
        if (cookie != null) res.put("Cookie", Collections.singletonList(cookie));
        return res;
    }

    @Override
    public CookieStore getCookieStore() {
        // We don't want anyone to work with this cookie store directly
        throw new UnsupportedOperationException();
    }

    public HashMap<String, String> getCookies(String url) {
        String cookies = webkitCookieManager.getCookie(url);
        if (cookies == null) {
            return new HashMap<>();
        }
        HashMap<String, String> cookieMap = new HashMap<>();
        String[] cookie = cookies.trim().split("; ");
        for (int i = 0; i < cookie.length; i++) {
            String[] data = cookie[i].split("=");
            if (data.length != 2) {
                continue;
            }
            cookieMap.put(data[0], data[1]);
        }
        return cookieMap;
    }

    public String getCookie(String url, String name) {
        String cookies = webkitCookieManager.getCookie(url);
        if (cookies == null) {
            return null;
        }
        String[] cookie = cookies.trim().split("; ");
        for (int i = 0; i < cookie.length; i++) {
            String[] data = cookie[i].split("=");
            if (data.length != 2) {
                continue;
            }
            if (data[0].equals(name)) {
                return data[1].trim();
            }
        }
        return null;
    }

    public boolean setCookie(String url, String name, String value) {
        try {
            URL storeUrl = new URL(url);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            Date currentTime = cal.getTime();
            Date cookieLife = new Date(currentTime.getTime() + 864000000); // 10 Days
            DateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            date.setTimeZone(TimeZone.getTimeZone("GMT"));

            StringBuilder builder = new StringBuilder();
            builder.append(name + "=" + value + "; expires=" + date.format(cookieLife) + "; domain=" + storeUrl.getHost() + "; path=" + storeUrl.getPath() + "; ");
            if (!storeUrl.getPath().isEmpty()) {
                builder.append(" path=" + storeUrl.getPath() + ";");
            } else {
                builder.append(" path=/;");
            }
            webkitCookieManager.setCookie(url, builder.toString());
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.getInstance().sync();
            }
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeCookie(String url, String name) {
        String cookies = webkitCookieManager.getCookie(url);
        if (cookies == null) {
            return false;
        }
        try {
            URL storeUrl = new URL(url);
            String[] cookie = cookies.trim().split("; ");
            for (String aCookie : cookie) {
                String[] data = aCookie.split("=");
                if (data.length != 2 && data[0].equals(name)) {
                    webkitCookieManager.setCookie(url, data[0] + "=; expires=Thu, 01 Jan 1970 00:00:01 GMT; domain=" + storeUrl.getHost() + "; path=" + storeUrl.getPath() + "; ");
                    return true;
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.getInstance().sync();
            }
            return false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeAllCookie(String url) {
        try {
            URL storeUrl = new URL(url);
            String cookies = webkitCookieManager.getCookie(url);
            if (cookies == null) {
                return false;
            }
            String[] cookie = cookies.trim().split("; ");
            for (String aCookie : cookie) {
                String[] data = aCookie.split("=");
                if (data.length != 2) {
                    continue;
                }
                webkitCookieManager.setCookie(url, data[0] + "=; expires=Thu, 01 Jan 1970 00:00:01 GMT; domain=" + storeUrl.getHost() + "; path=" + storeUrl.getPath() + "; ");
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.getInstance().sync();
            }
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void debugShowCookies(String url) {
        try {
            URL storeUrl = new URL(url);
            String cookies = webkitCookieManager.getCookie(url);
            if (cookies == null) {
                throw new Exception("No cookie to show.");
            }
            String[] cookie = cookies.trim().split("; ");
            Log.d("WebkitCookieManager", "Cookie of " + url + " listed below.");
            for (String aCookie : cookie) {
                Log.d("WebkitCookieManager", aCookie);
            }
        } catch (Exception e) {
            Log.d("WebkitCookieManager", e.getMessage());
        }
    }

}
