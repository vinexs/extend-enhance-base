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
import android.os.AsyncTask;

import com.vinexs.eeb.misc.EnhancedHttp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class FileDownload {

    public static void get(Context context, URL url, File storagePath, OnResponseListener response) {
        get(context, url, new HashMap<String, Object>(), storagePath, response);
    }

    public static void get(Context context, URL url, Map<String, Object> vars, File storagePath, OnResponseListener response) {
        downloadFile(context, url, EnhancedHttp.METHOD_GET, vars, storagePath, response);
    }

    private static void downloadFile(final Context context, final URL url, final int useMethod, final Map<String, Object> vars,
                                     final File storagePath, final OnResponseListener response) {
        new AsyncTask<String, String, File>() {

            private HttpURLConnection conn;
            private int responseCode;
            private int fileLength;
            private int count;
            private byte data[] = new byte[1024];
            private long total = 0;
            private int percentage = 0;

            @Override
            protected File doInBackground(String... strings) {
                try {
                    // Get request file length.
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("charset", "utf-8");
                    conn.setRequestProperty("Accept-Encoding", "gzip");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0(Linux; Android; HttpURLConnection; EEB)");

                    // TODO add post method support

                    // TODO add parameter support

                    conn.connect();
                    responseCode = conn.getResponseCode();
                    fileLength = conn.getContentLength();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        conn.disconnect();
                        throw new FileNotFoundException("Cannot find file in internet.");
                    }

                    // Check content is write to a file, not a directory.
                    File saveStorage = storagePath;
                    if (storagePath.exists()) {
                        if (storagePath.isDirectory()) {
                            String urlPath = url.getPath();
                            String filename = urlPath.substring(urlPath.lastIndexOf('/') + 1);
                            saveStorage = new File(storagePath, filename);
                        }
                    } else {
                        File storageParent = storagePath.getParentFile();
                        if (!storageParent.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            storageParent.mkdirs();
                        }
                    }

                    // Start download file
                    InputStream input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(saveStorage);
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);

                        // Calculate total download progress.
                        total += count;
                        Integer newPercentage = (int) ((total * 100) / fileLength);
                        if (newPercentage != percentage) {
                            percentage = newPercentage;
                            response.onDownloading(context, percentage);
                        }
                    }

                    output.flush();
                    output.close();
                    input.close();
                    conn.disconnect();

                    return saveStorage;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(File saveStored) {
                if (saveStored != null && saveStored.exists()) {
                    response.onSuccess(context, saveStored);
                } else {
                    response.onError(context, responseCode);
                }
            }

        }.execute();
    }


    public interface OnResponseListener {

        void onDownloading(Context context, int percentage);

        void onSuccess(Context context, File storagePath);

        void onError(Context context, int stateCode);
    }

}
