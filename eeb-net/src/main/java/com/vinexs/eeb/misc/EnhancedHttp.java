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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;

import com.vinexs.tool.WebkitCookieManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings({"unused", "WeakerAccess"})
public class EnhancedHttp {

    public static final int METHOD_GET = 0;
    public static final int METHOD_POST = 1;
    public static final int METHOD_POST_MULTIPART = 2;

    protected HttpURLConnection conn = null;
    protected Context context = null;

    private static final String TAG = "EnhancedHttp";
    private static final String LINE_FEED = "\r\n";
    private static final String BOUNDARY = "REQUESTBOUNDARY";

    private int useMethod = EnhancedHttp.METHOD_GET;
    private Boolean noCache = false;
    private Boolean allowRedirect = false;
    private String userAgent = "Mozilla/5.0(Linux; Android; HttpURLConnection; EEB)";
    private Map<String, String> requestProperty = new HashMap<>();
    private Map<String, Object> vars = new HashMap<>();

    private int htmlStatusCode;
    private String htmlResponse;

    public EnhancedHttp(Context context) {
        this.context = context;
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            android.webkit.CookieSyncManager.createInstance(context);
        }
        cookieManager.setAcceptCookie(true);
        java.net.CookieHandler.setDefault(new WebkitCookieManager(context, null, java.net.CookiePolicy.ACCEPT_ALL));
    }

    public HttpURLConnection getConnection() {
        return conn;
    }

    public int getStatusCode() {
        return htmlStatusCode;
    }

    public String getHtmlResponse() {
        return htmlResponse;
    }

    public EnhancedHttp setNoCache() {
        noCache = true;
        return this;
    }

    public EnhancedHttp setAllowRedirect() {
        allowRedirect = true;
        return this;
    }

    public EnhancedHttp setData(String name, Object value) {
        vars.put(name, value);
        if (value instanceof java.io.File) {
            useMethod = EnhancedHttp.METHOD_POST_MULTIPART;
        }
        return this;
    }

    public EnhancedHttp setData(Map<String, Object> vars) {
        for (Map.Entry<String, Object> variable : vars.entrySet()) {
            this.vars.put(variable.getKey(), variable.getValue());
        }
        return this;
    }

    public EnhancedHttp setUserAgent(String agent) {
        userAgent = agent;
        return this;
    }

    public EnhancedHttp setRequestProperty(String field, String newValue) {
        requestProperty.put(field, newValue);
        return this;
    }

    public void get(String url) {
        get(url, null);
    }

    public void get(String url, Map<String, Object> vars, OnResponseListener response) {
        for (Map.Entry<String, Object> variable : vars.entrySet()) {
            vars.put(variable.getKey(), variable.getValue());
        }
        get(url, response);
    }

    public void get(String url, OnResponseListener response) {
        useMethod = EnhancedHttp.METHOD_GET;
        buildConnection(url, response);
    }

    public void post(String url) {
        post(url, null);
    }

    public void post(String url, Map<String, Object> vars, OnResponseListener response) {
        for (Map.Entry<String, Object> variable : vars.entrySet()) {
            if (variable.getValue() instanceof java.io.File) {
                useMethod = EnhancedHttp.METHOD_POST_MULTIPART;
            }
            vars.put(variable.getKey(), variable.getValue());
        }
        post(url, response);
    }

    public void post(String url, OnResponseListener response) {
        if (useMethod != EnhancedHttp.METHOD_POST_MULTIPART) {
            useMethod = EnhancedHttp.METHOD_POST;
        }
        buildConnection(url, response);
    }

    private void buildConnection(final String url, final OnResponseListener responseListener) {

        new AsyncTask<Object, Integer, String>() {

            @Override
            protected String doInBackground(Object... params) {
                try {
                    String actionUrl = url;
                    // Patch Url to avoid MalformedURLException.
                    if (!url.startsWith("http")) {
                        if (url.startsWith("//")) {
                            actionUrl = "http:" + actionUrl;
                        } else {
                            actionUrl = "http://" + actionUrl;
                        }
                    }

                    // Add query behind action url.
                    if (useMethod == EnhancedHttp.METHOD_GET) {
                        if (!vars.isEmpty()) {
                            actionUrl += ((!url.contains("?")) ? "?" : "&") + getQueryStr();
                        }
                    }
                    URL urlConn = new URL(actionUrl);
                    conn = urlConn.getProtocol().equals("http") ?
                            (HttpURLConnection) urlConn.openConnection() :
                            (HttpsURLConnection) urlConn.openConnection();
                    conn.setDoInput(responseListener != null);
                    conn.setInstanceFollowRedirects(allowRedirect);
                    conn.setUseCaches(!noCache);
                    conn.setRequestProperty("charset", "utf-8");
                    conn.setRequestProperty("Accept-Encoding", "gzip");
                    conn.setRequestProperty("User-Agent", userAgent);
                    if (requestProperty.size() > 0) {
                        for (Map.Entry<String, String> request : requestProperty.entrySet()) {
                            conn.setRequestProperty(request.getKey(), request.getValue());
                        }
                    }
                    switch (useMethod) {
                        case EnhancedHttp.METHOD_POST_MULTIPART:
                            conn.setUseCaches(false);
                            conn.setDoOutput(true);
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Connection", "Keep-Alive");
                            conn.setRequestProperty("Cache-Control", "no-cache");
                            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
                            createMultipartDataOutputStream(conn);
                            break;
                        case EnhancedHttp.METHOD_POST:
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            if (!vars.isEmpty()) {
                                byte[] postData = getQueryStr().getBytes("UTF-8");
                                conn.setDoOutput(true);
                                conn.setRequestProperty("Content-Length", String.valueOf(postData.length));
                                DataOutputStream postOutputStream = new DataOutputStream(conn.getOutputStream());
                                postOutputStream.write(postData);
                                postOutputStream.close();
                            }
                            break;
                        case EnhancedHttp.METHOD_GET:
                        default:
                            break;
                    }
                    htmlStatusCode = conn.getResponseCode();
                    if (htmlStatusCode != HttpURLConnection.HTTP_OK) {
                        conn.disconnect();
                        return null;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    if (responseListener != null) {
                        String line;
                        InputStream inputStream =
                                ("gzip".equals(conn.getContentEncoding())) ?
                                        new GZIPInputStream(conn.getInputStream()) : conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        reader.close();
                    }
                    conn.disconnect();
                    htmlResponse = stringBuilder.toString();
                    return htmlResponse;
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        htmlStatusCode = conn.getResponseCode();
                        conn.disconnect();
                    } catch (Exception ignored) {
                    }
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String htmlResponse) {
                if (responseListener != null) {
                    if (htmlStatusCode == HttpURLConnection.HTTP_OK) {
                        responseListener.onSuccess(context, htmlResponse);
                    } else {
                        Log.e("EnhancedHttp", conn.getURL().toString() + " response error " + htmlStatusCode);
                        responseListener.onError(context, htmlStatusCode);
                    }
                }
            }

        }.execute();
    }

    public String getQueryStr() {
        return getQueryStr(vars);
    }

    public void createMultipartDataOutputStream(HttpURLConnection conn) {
        createMultipartDataOutputStream(conn, vars);
    }

    public static String getQueryStr(Map<String, Object> params) {
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (param.getValue() instanceof java.io.File) {
                continue;
            }
            if (query.length() != 0) {
                query.append('&');
            }
            try {
                query.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                query.append('=');
                query.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e("EnhancedHttp", "Variable [" + param.getKey() + "] fail to encode.");
            }
        }
        return query.toString();
    }

    public static void createMultipartDataOutputStream(HttpURLConnection conn, Map<String, Object> vars) {
        try {
            DataOutputStream multipartOutputStream = new DataOutputStream(conn.getOutputStream());
            if (!vars.isEmpty()) {
                for (Map.Entry<String, Object> var : vars.entrySet()) {
                    if (var.getValue() instanceof java.io.File) {
                        File fileUpload = (File) var.getValue();
                        if (!fileUpload.exists()) {
                            Log.e("EnhancedHttp", "Cannot find file in path " + fileUpload.getPath());
                            continue;
                        }
                        multipartAddFile(multipartOutputStream, var.getKey(), fileUpload);
                    } else {
                        multipartAddParam(multipartOutputStream, var.getKey(), String.valueOf(var.getValue()));
                    }
                }
            }
            multipartOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void multipartAddParam(DataOutputStream outputStream, String name, String value) throws IOException {
        outputStream.writeBytes("--" + BOUNDARY + LINE_FEED);
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED);
        outputStream.writeBytes("Content-Type: text/plain; chart=utf8" + LINE_FEED);
        outputStream.writeBytes(LINE_FEED);
        outputStream.writeBytes(value + LINE_FEED);
        outputStream.writeBytes("--" + BOUNDARY + LINE_FEED + LINE_FEED);
    }

    public static void multipartAddFile(DataOutputStream outputStream, String name, File file) throws IOException {
        String fileName = file.getName();
        outputStream.writeBytes("--" + BOUNDARY + LINE_FEED);
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\";filename=\"" + fileName + "\"" + LINE_FEED);
        outputStream.writeBytes("Content-Type: " + HttpURLConnection.guessContentTypeFromName(fileName) + LINE_FEED);
        outputStream.writeBytes("Content-Transfer-Encoding: binary" + LINE_FEED);
        outputStream.writeBytes(LINE_FEED);
        byte[] fileByte = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        //noinspection ResultOfMethodCallIgnored
        fileInputStream.read(fileByte);
        fileInputStream.close();
        outputStream.write(fileByte);
        outputStream.writeBytes(LINE_FEED);
        outputStream.writeBytes("--" + BOUNDARY + LINE_FEED + LINE_FEED);
    }

    public interface OnResponseListener {
        void onSuccess(Context context, String response);

        void onError(Context context, int stateCode);
    }

}
