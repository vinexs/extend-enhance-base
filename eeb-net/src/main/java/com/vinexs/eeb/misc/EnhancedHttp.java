package com.vinexs.eeb.misc;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("unused")
public class EnhancedHttp {

    private static final String TAG = "EnhancedHttp";
    private static final int METHOD_GET = 0;
    private static final int METHOD_POST = 1;
    private static final int METHOD_POST_MULTIPART = 2;

    private static final String LINE_FEED = "\r\n";
    private static final String BOUNDARY = "REQUESTBOUNDARY";

    private HttpURLConnection conn = null;
    private Context context = null;
    private int useMethod = EnhancedHttp.METHOD_GET;
    private Boolean noCache = false;
    private Boolean allowRedirect = false;
    private LinkedHashMap<String, String> requestProperty = new LinkedHashMap<>();
    private LinkedHashMap<String, String> data = new LinkedHashMap<>();
    private LinkedHashMap<String, File> files = new LinkedHashMap<>();

    private int htmlStatusCode;
    private String htmlResponse;

    public EnhancedHttp(Context context) {
        this.context = context;
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
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

    public EnhancedHttp setData(String name, String value) {
        data.put(name, value);
        return this;
    }

    public EnhancedHttp setData(String name, File file) {
        files.put(name, file);
        useMethod = EnhancedHttp.METHOD_POST_MULTIPART;
        return this;
    }

    public EnhancedHttp setUserAgent(String agent) {
        String userAgent = "Mozilla/5.0(Linux; Android; EEB)";
        requestProperty.put("User-Agent", userAgent);
        return this;
    }

    public EnhancedHttp setRequestProperty(String field, String newValue) {
        requestProperty.put(field, newValue);
        return this;
    }

    public void get(String url) {
        get(url, null);
    }

    public void get(String url, LinkedHashMap<String, Object> vars, OnResponseListener response) {
        for (Map.Entry<String, Object> variable : vars.entrySet()) {
            if (!(variable.getValue() instanceof java.io.File)) {
                data.put(variable.getKey(), variable.getValue().toString());
            }
        }
        get(url, response);
    }

    public void get(String url, OnResponseListener response) {
        buildConnection(url, response);
    }

    public void post(String url) {
        post(url, null);
    }

    public void post(String url, LinkedHashMap<String, Object> vars, OnResponseListener response) {
        for (Map.Entry<String, Object> variable : vars.entrySet()) {
            if (variable.getValue() instanceof java.io.File) {
                if (!((File) variable.getValue()).exists()) {
                    continue;
                }
                files.put(variable.getKey(), (File) variable.getValue());
                useMethod = EnhancedHttp.METHOD_POST_MULTIPART;
            } else {
                data.put(variable.getKey(), variable.getValue().toString());
            }
        }
        post(url, response);
    }

    public void post(String url, OnResponseListener response) {
        if (useMethod != EnhancedHttp.METHOD_POST_MULTIPART) {
            useMethod = EnhancedHttp.METHOD_POST;
        }
        buildConnection(url, response);
    }

    public void buildConnection(final String url, final OnResponseListener responseListener) {

        new AsyncTask<Object, Integer, String>() {

            @Override
            protected String doInBackground(Object... params) {
                try {
                    String actionUrl = url;
                    if (useMethod == EnhancedHttp.METHOD_GET) {
                        if (!data.isEmpty()) {
                            actionUrl += ((!url.contains("?")) ? "?" : "&") + getQueryStr();
                        }
                    }
                    URL urlconn = new URL(actionUrl);
                    if (!actionUrl.startsWith("https:")) {
                        conn = (HttpURLConnection) urlconn.openConnection();
                    } else {
                        conn = (HttpsURLConnection) urlconn.openConnection();
                    }
                    conn.setDoInput(responseListener != null);
                    conn.setInstanceFollowRedirects(allowRedirect);
                    conn.setUseCaches(!noCache);
                    conn.setRequestProperty("charset", "utf-8");
                    conn.setRequestProperty("Accept-Encoding", "gzip");
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
                            DataOutputStream multipartOutputStream = new DataOutputStream(conn.getOutputStream());
                            if (!files.isEmpty()) {
                                for (Map.Entry<String, File> file : files.entrySet()) {
                                    if (!file.getValue().exists()) {
                                        Log.e(TAG, "File dropped. [" + file.getValue().toString() + "]");
                                        continue;
                                    }
                                    multipartAddFile(multipartOutputStream, file.getKey(), file.getValue());
                                }
                            }
                            if (!data.isEmpty()) {
                                for (Map.Entry<String, String> var : data.entrySet()) {
                                    multipartAddParam(multipartOutputStream, var.getKey(), var.getValue());
                                }
                            }
                            multipartOutputStream.close();
                            break;
                        case EnhancedHttp.METHOD_POST:
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            if (!data.isEmpty()) {
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
                                (conn instanceof HttpsURLConnection && "gzip".equals(conn.getContentEncoding())) ?
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

    public void multipartAddParam(DataOutputStream outputStream, String name, String value) throws IOException {
        outputStream.writeBytes("--" + BOUNDARY + LINE_FEED);
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED);
        outputStream.writeBytes("Content-Type: text/plain; chart=utf8" + LINE_FEED);
        outputStream.writeBytes(LINE_FEED);
        outputStream.writeBytes(value + LINE_FEED);
        outputStream.writeBytes("--" + BOUNDARY + LINE_FEED + LINE_FEED);
    }

    public void multipartAddFile(DataOutputStream outputStream, String name, File file) throws IOException {
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

    public String getQueryStr() {
        return getQueryStr(data);
    }

    public String getQueryStr(LinkedHashMap<String, String> params) {
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            try {
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e("EnhancedHttp", "Variable [" + param.getKey() + "] fail to encode.");
            }
        }
        return postData.toString();
    }

    public interface OnResponseListener {
        void onSuccess(Context context, String response);

        void onError(Context context, int stateCode);
    }

}
