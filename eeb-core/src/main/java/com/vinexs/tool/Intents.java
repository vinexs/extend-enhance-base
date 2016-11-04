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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.widget.Toast;

import com.vinexs.R;

import java.io.File;
import java.util.List;

@SuppressWarnings("unused WeakerAccess")
public class Intents {

    public static final String TWITTER = "com.twitter.";
    public static final String GMAIL = "com.google.android.gm.";
    public static final String GOOGLEPLUS = "com.google.android.apps.plus.";
    public static final String DOCS = "com.google.android.apps.docs.";
    public static final String EMAIL = "com.android.email.";
    public static final String BLUETOOTH = "com.android.bluetooth.";

    public static boolean isAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    public static boolean isAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static ComponentName startService(Context context, Class<?> serviceClass) {
        return context.startService(new Intent(context, serviceClass));
    }

    public static boolean stopService(Context context, Class<?> serviceClass) {
        return isServiceRunning(context, serviceClass) && context.stopService(new Intent(context, serviceClass));
    }

    // ===== return per-handle intent
    // ==========================================================

    /**
     * Create image picker intent for select image and return file URI.
     * Inside method [onActivityResult], use [data.getData()] to get select image file URI.
     *
     * @return Image picker intent.
     */
    public static Intent getImagePicker() {
        return new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    public static Intent getAudioPicker() {
        return new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    public static Intent getVideoPicker() {
        return new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
    }

    public static Intent getFilePicker() {
        return getFilePicker(null);
    }

    public static Intent getFilePicker(String allowMIME) {
        if (allowMIME == null) {
            allowMIME = "*";
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(allowMIME + "/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    public static Intent getWirelessSetting() {
        return new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
    }

    public static Intent getBlueToothSetting() {
        return new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
    }

    /**
     * Create camera intent for capture image and return file URI.
     * Inside method [onActivityResult], use [ IntentController.CAMERA_TEMP_IMG ] to get captured image file URI.
     *
     * @return Camera intent.
     */
    public static Intent getCamera(Context context) {
        File photoCachePath;
        String extStorageState = Environment.getExternalStorageState();
        if (extStorageState.equals(Environment.MEDIA_MOUNTED)) {
            photoCachePath = new File(context.getExternalFilesDir(null), "tmp.jpg");
        } else {
            photoCachePath = new File(context.getFilesDir(), "tmp.jpg");
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoCachePath));
        return intent;
    }

    // ===== One way intent
    // ==========================================================

    public static void browse(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    public static void dial(Context context, String telNo) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(telNo));
        context.startActivity(intent);
    }

    public static void call(Context context, String telNo) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(telNo));
        //noinspection MissingPermission
        context.startActivity(intent);
    }

    public static void showWifiSetting(Context context) {
        context.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
    }

    public static void showWirelessSetting(Context context) {
        context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
    }

    public static void showGooglePlay(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + context.getPackageName()));
        if (isAvailable(context, intent)) {
            context.startActivity(intent);
            return;
        }
        browse(context, "https://play.google.com/store/apps/details?id=" + context.getPackageName());
    }

    public static void showAmazonStore(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("amzn://apps/android?p=" + context.getPackageName()));
        if (isAvailable(context, intent)) {
            context.startActivity(intent);
            return;
        }
        browse(context, "http://www.amazon.com/gp/mas/dl/android?p=" + context.getPackageName());
    }

    public static void playVideo(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "video/*");
        context.startActivity(intent);
    }

    // === View location
    public static void viewLocation(Context context, float latitude, float longitude) {
        viewLocation(context, latitude, longitude, null);
    }

    public static void viewLocation(Context context, float latitude, float longitude, String label) {
        String uri = "geo:" + latitude + "," + longitude;
        if (label != null) {
            uri += "?q=" + latitude + "," + longitude + "(" + label + ")&z=16";
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        context.startActivity(intent);
    }

    // === Send email
    public static void sendEmail(Context context, String address) {
        sendEmail(context, address, null, null);
    }

    public static void sendEmail(Context context, String address, String subject) {
        sendEmail(context, address, subject, null);
    }

    public static void sendEmail(Context context, String address, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        if (subject != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (body != null) {
            intent.putExtra(Intent.EXTRA_TEXT, body);
        }
        context.startActivity(intent);
    }

    // === Share text
    public static void shareText(Context context, String text) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("plain/text");
        context.startActivity(Intent.createChooser(shareIntent, null));
    }

    // === Share image
    public static void shareImage(Context context, BitmapDrawable bitmapDrawable) {
        shareImage(context, bitmapDrawable, null);
    }

    public static void shareImage(Context context, BitmapDrawable bitmapDrawable, String imageTitle) {
        Uri photoUri = Uri.parse(Images.Media.insertImage(context.getContentResolver(), bitmapDrawable.getBitmap(), imageTitle, null));
        shareImage(context, photoUri, imageTitle);
    }

    public static void shareImage(Context context, Uri filepath) {
        shareImage(context, filepath, null);
    }

    public static void shareImage(Context context, Uri filepath, String imageTitle) {
//        if (imageTitle == null) {
//            imageTitle = "Image";
//        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, filepath);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, null));
    }

    // Share through target application
    public static void shareTextToApplication(Context context, String packageName, String text) {
        shareWithPackage(context, packageName, null, text, null);
    }

    public static void shareTextToApplication(Context context, String packageName, String subject, String text) {
        shareWithPackage(context, packageName, subject, text, null);
    }

    public static void shareImageToApplication(Context context, String packageName, Uri filepath) {
        shareWithPackage(context, packageName, null, null, filepath);
    }

    public static void shareWithPackage(Context context, String packageName, String subject, String text, Uri filepath) {
        try {
            PackageManager packageMgr = context.getPackageManager();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/*");
            if (filepath != null) {
                intent.setType("image/*");
            }
            List<ResolveInfo> activityList = packageMgr.queryIntentActivities(intent, 0);
            ActivityInfo activityInfo = null;
            for (ResolveInfo app : activityList) {
                if (app.activityInfo.name.startsWith(packageName)) {
                    activityInfo = app.activityInfo;
                }
            }
            if (activityInfo == null) {
                Log.d("Intent Share", "Package not found.");
                throw new Exception("Package not found on this phone.");
            }
            if (subject != null) {
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            }
            if (text != null) {
                intent.putExtra(Intent.EXTRA_TEXT, text);
            }
            if (filepath != null) {
                intent.putExtra(Intent.EXTRA_STREAM, filepath);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            ComponentName name = new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
            intent.setComponent(name);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, context.getResources().getString(R.string.app_not_found), Toast.LENGTH_LONG).show();
        }
    }

}
