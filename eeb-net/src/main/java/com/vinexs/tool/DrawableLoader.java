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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UnknownFormatFlagsException;

@SuppressWarnings("unused")
public class DrawableLoader {

    public static void load(Context context, View view, String url) {
        try {
            load(context, view, new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    public static void load(final Context context, final View view, URL url) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        cacheDir = new File(cacheDir, "drawable/"+ url.getHost());
        if (!cacheDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cacheDir.mkdirs();
        }

        Object[] passVars = {url, cacheDir};
        new AsyncTask<Object, Integer, InputStream>() {

            @Override
            protected InputStream doInBackground(Object... params) {
                URL url = (URL)params[0];
                File cacheDir = (File)params[1];
                File cacheFile;
                try {
                    HttpURLConnection conn = null;
                    Integer followLimit = 7;
                    Integer followCount = 0;
                    DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    do {
                        if (conn == null) {
                            conn = (HttpURLConnection)url.openConnection();
                            cacheFile = new File(cacheDir, ""+ url.hashCode());
                            if (cacheFile.exists()) {
                                conn.setRequestProperty("If-Modified-Since", dateFormat.format(cacheFile.lastModified()));
                                conn.setRequestProperty("Cache-Control", "max-age=0");
                            }
                        } else {
                            int respCode = conn.getResponseCode();
                            if (respCode != HttpURLConnection.HTTP_OK && respCode != HttpURLConnection.HTTP_NOT_MODIFIED
                                    && respCode != HttpURLConnection.HTTP_MOVED_TEMP && respCode != HttpURLConnection.HTTP_MOVED_PERM) {
                                return null;
                            } else if (respCode == HttpURLConnection.HTTP_MOVED_TEMP || respCode == HttpURLConnection.HTTP_MOVED_PERM) {
                                String followUrl = conn.getHeaderField("Location");
                                url = new URL(followUrl);
                                cacheFile = new File(cacheDir, ""+ url.hashCode());
                                conn = (HttpURLConnection) url.openConnection();
                                if (cacheFile.exists()) {
                                    conn.setRequestProperty("If-Modified-Since", dateFormat.format(cacheFile.lastModified()));
                                    conn.setRequestProperty("Cache-Control", "max-age=0");
                                }
                                followCount++;
                            } else if (respCode == HttpURLConnection.HTTP_OK || respCode == HttpURLConnection.HTTP_NOT_MODIFIED ) {
                                cacheFile = new File(cacheDir, ""+ url.hashCode());
                                if (cacheFile.exists()) {
                                    if (respCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                                        // Do 304 response by header.
                                        Log.d("DrawableLoader", "Use cache file.["+ cacheFile.toString() +"] (304)");
                                        return new FileInputStream(cacheFile);
                                    } else {
                                        // Do 304 response manually.
                                        String urlLastMod = conn.getHeaderField("Last-Modified");
                                        if (urlLastMod != null) {
                                            Date date = dateFormat.parse(urlLastMod);
                                            if (cacheFile.lastModified() > date.getTime()) {
                                                Log.d("DrawableLoader", "Use cache file.["+ cacheFile.toString() +"] (200)");
                                                return new FileInputStream(cacheFile);
                                            }
                                        }
                                        //noinspection ResultOfMethodCallIgnored
                                        cacheFile.delete();
                                    }
                                }
                                // Storage the image.
                                String fileMIME = conn.getContentType();
                                Bitmap.CompressFormat format;
                                switch (fileMIME) {
                                    case "image/bmp":
                                    case "image/x-windows-bmp":
                                    case "image/jpg":
                                    case "image/jpeg":
                                    case "image/pjpeg":
                                        format = Bitmap.CompressFormat.JPEG;
                                        break;
                                    case "image/gif":
                                    case "image/png":
                                        format = Bitmap.CompressFormat.PNG;
                                        break;
                                    case "image/webp":
                                        format = Bitmap.CompressFormat.WEBP;
                                        break;
                                    default:
                                        throw new UnknownFormatFlagsException("Cannot handle "+ fileMIME +" file.");
                                }
                                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                                Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
                                bitmap.compress(format, 100, outputStream);
                                Log.d("DrawableLoader", "Use online file.");
                                return new FileInputStream(cacheFile);
                            }
                        }
                        conn.setDoInput(true);
                        conn.setConnectTimeout(15000);
                        // Do follow redirect manually
                        conn.setInstanceFollowRedirects(false);

                        Log.d("DrawableLoader", "Get Url: "+ conn.getURL().toString());
                        if (conn.getRequestProperty("If-Modified-Since")!=null) {
                            Log.d("DrawableLoader", "Local file last modified at " + conn.getRequestProperty("If-Modified-Since"));
                        }
                        conn.connect();
                    } while (followCount < followLimit);
                    // Image too many redirect..
                    return null;
                } catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final InputStream inputStream) {
                try {
                    if (inputStream == null) {
                        throw new FileNotFoundException();
                    }
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (view instanceof ImageView) {
                                ((ImageView) view).setImageBitmap(bitmap);
                            } else if (view instanceof ImageButton) {
                                ((ImageButton) view).setImageBitmap(bitmap);
                            }
                        }
                    });
                }catch (FileNotFoundException e) {
                    Log.e("DrawableLoader", "File not found.");
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.execute(passVars);
    }
}
