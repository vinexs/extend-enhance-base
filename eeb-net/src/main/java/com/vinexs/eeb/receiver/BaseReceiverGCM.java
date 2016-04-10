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

package com.vinexs.eeb.receiver;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.vinexs.eeb.net.R;
import com.vinexs.tool.Utility;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings("unused")
public abstract class BaseReceiverGCM extends BroadcastReceiver {

    protected NotificationManagerCompat notifyMgr;
    protected NotificationCompat.Builder builder;
    protected GoogleCloudMessaging gcm = null;
    protected Context context;
    protected CharSequence contentTitle = "";
    protected CharSequence contentText = "";
    protected int messageNum = 0;

    public void requestDeviceRegisterId(Activity activity, final String gcmSenderId) {
        ArrayList<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.INTERNET);
        permissionList.add(Manifest.permission.WAKE_LOCK);
        permissionList.add(Manifest.permission.GET_ACCOUNTS);
        permissionList.add(Manifest.permission.VIBRATE);
        permissionList.add("com.google.android.c2dm.permission.RECEIVE");
        permissionList.add(activity.getPackageName() + ".permission.C2D_MESSAGE");
        if (Utility.hasPermission(activity, permissionList)) {
            gcm = GoogleCloudMessaging.getInstance(activity.getApplicationContext());
            String register_id;
            try {
                register_id = gcm.register(gcmSenderId);
                if (!register_id.equals("")) {
                    Log.d("GoogleCloudMessaging", "Register ID: " + register_id);
                    handleReceivedRegisterId(register_id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(activity);
        if (setting.getInt("notifyId", 0) == 0) {
            setting.edit().putInt("notifyId", new Random().nextInt(65535)).apply();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);
        if (messageType == null) {
            return;
        }
        Log.d("GoogleCloudMessaging", "Receive message type: " + messageType);
        switch (messageType) {
            case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                break;
            case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                break;
            case GoogleCloudMessaging.ERROR_SERVICE_NOT_AVAILABLE:
                break;
            case GoogleCloudMessaging.ERROR_MAIN_THREAD:
                break;
            case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                onMessageTypeReceive(context, intent);
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onMessageTypeReceive(Context context, Intent intent) {
        notifyMgr = NotificationManagerCompat.from(context);
        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(context);
        int notifyId = setting.getInt("notifyId", new Random().nextInt(65535));
        Bundle receIntent = intent.getExtras();
        try {
            if (!receIntent.containsKey("contentTitle") || !receIntent.containsKey("contentText")) {
                throw new Exception("Message don't contain necessary data.");
            }

            if (builder == null) {
                builder = new NotificationCompat.Builder(context);
                contentTitle = receIntent.getCharSequence("contentTitle");
                contentText = receIntent.getCharSequence("contentText");

                builder.setDefaults(Notification.DEFAULT_ALL)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setSmallIcon(getMonoColorIcon())
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true);
                try {
                    if (Build.VERSION.SDK_INT < 14 || !receIntent.containsKey("largeIcon")) {
                        throw new Exception("Message don't contain [largeIcon] or device SDK lower than 14.");
                    }
                    String bigIconUrl = receIntent.getString("largeIcon");
                    if (bigIconUrl == null || bigIconUrl.isEmpty()) {
                        throw new Exception("Message [largeIcon] is empty.");
                    }
                    HttpURLConnection connection = (HttpURLConnection) new URL(bigIconUrl).openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    Bitmap bigIcon = BitmapFactory.decodeStream(connection.getInputStream());
                    builder.setLargeIcon(bigIcon);
                    // Add backgroud to wearable
                    NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
                    wearableExtender.setBackground(bigIcon);
                    builder.extend(wearableExtender);
                    // Set accent color
                    int[] attrs = new int[] { R.attr.colorAccent };
                    TypedArray ta = context.obtainStyledAttributes(attrs);
                    String colorAccent = ta.getString(0);
                    ta.recycle();
                    builder.setColor(Color.parseColor(colorAccent));
                } catch (Exception e) {
                    builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), getApplicationIcon()));
                }
                try {
                    if (!receIntent.containsKey("ticker")) {
                        throw new Exception("Message don't contain [ticker].");
                    }
                    builder.setTicker(receIntent.getCharSequence("ticker"));
                } catch (Exception e) {
                    builder.setTicker(receIntent.getCharSequence("contentText"));
                }
                if (Build.VERSION.SDK_INT >= 16) {
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
                }
            } else {
                contentText = contentText + "\n" + receIntent.getCharSequence("contentText");
                messageNum++;
                builder.setContentTitle(Utility.getAppName(context))
                        .setContentText(contentText)
                        .setTicker(receIntent.getCharSequence("contentText"))
                        .setNumber(messageNum);
            }

            if (Build.VERSION.SDK_INT >= 16) {
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));
            }

            Intent actionIntent = new Intent(context, getLauncherClass());
            if (receIntent.containsKey("intentAction")) {
                actionIntent.putExtra("onNewIntentAction", receIntent.getCharSequence("intentAction"));
            }
            actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            notifyMgr.notify(notifyId, builder.build());
        } catch (Exception e) {
            Log.d("GoogleCloudMessaging", "Exception occurred while show message as notification -> " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Assign application main class for pending intent.
     */
    public abstract Class<?> getLauncherClass();

    /**
     * Assign application launcher icon. It should be at least 256x256 px.
     */
    public abstract int getApplicationIcon();

    /**
     * Assign mono colored application icon. It will display in the notification.
     * It should be in 144x144 px.
     */
    public abstract int getMonoColorIcon();

    /**
     * Assign mono colored application icon. It will display in status bar.
     * It should be in 64x64 px.
     */
    @SuppressWarnings("unused")
    public abstract int getSmallMonoColorIcon();

    /**
     * Google GCM server will send a registered id for receiving notification.
     * @param register_id Registered returned from GCM server.
     */
    public abstract void handleReceivedRegisterId(String register_id);

}
