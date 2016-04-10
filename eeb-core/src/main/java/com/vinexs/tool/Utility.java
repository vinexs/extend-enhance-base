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

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.vinexs.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

@SuppressWarnings("unused")
public class Utility {

    // ========== String utilities ==========
    public static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String spaceEncode(String str) {
        return str.replaceAll("\\s+", "+");
    }

    public static String nl2br(String str) {
        return str.replaceAll("\\n", "<br />");
    }

    public static String trim(String str) {
        return trim(str, "B");
    }

    public static String trim(String str, String pos) {
        switch (pos) {
            case "L":
                return str.replaceAll("(^\\s*)", "");
            case "R":
                return str.replaceAll("(\\s*$)", "");
            default:
                return str.replaceAll("(^\\s*)|(\\s*$)", "");
        }
    }

    public static String leftPadding(String pad, int length, String str) {
        while (str.length() < length) {
            str = pad + str;
        }
        return str;
    }

    public static String rightPadding(String pad, int length, String str) {
        while (str.length() < length) {
            str = str + pad;
        }
        return str;
    }

    public static String getCurrentTime() {
        return getCurrentTime("yyyy-MM-dd HH:mm:ss");
    }

    public static String getCurrentTime(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(new Date());
    }

    // ========== Android Application utilities ==========
    /* ------------- Package ------------- */

    public static String getAppName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        if (stringId == 0) {
            return "";
        }
        return context.getString(stringId);
    }


    public static String getAppName(Activity activity) {
        try {
            PackageManager packageMgr = activity.getPackageManager();
            ActivityInfo activityInfo = packageMgr.getActivityInfo(activity.getComponentName(), 0);
            return activityInfo.loadLabel(packageMgr).toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getPackageName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).packageName;
        } catch (Exception e) {
            Log.e("Package", "Fail to get application name.", e);
            return null;
        }
    }

    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            Log.e("Package", "Fail to get version name.", e);
            return null;
        }
    }

    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            Log.e("Package", "Fail to get version code.", e);
            return 1;
        }
    }

	/* ------------- User ------------- */

    /**
     * <p>Get user gmail address.</p>
     * Required Permission &lt;uses-permission android:name="android.permission.GET_ACCOUNTS"/&gt;
     *
     * @param context Application context.
     * @return Phone number in string.
     */
    public static String getUserGmailAddress(Context context) {
        try {
            if (!hasPermission(context, Manifest.permission.GET_ACCOUNTS)) {
                throw new Exception("User did not grant permission.");
            }
            Pattern emailPattern = Patterns.EMAIL_ADDRESS;
            Account[] accounts = AccountManager.get(context).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches() && account.name.endsWith("gmail.com")) {
                    return account.name;
                }
            }
            return null;
        } catch (Exception e) {
            Log.e("AccountManager", e.getMessage(), e);
            return null;
        }
    }

    /**
     * <p>Get user phone number.</p>
     * Required Permission &lt;uses-permission android:name="android.permission.READ_PHONE_STATE"/&gt;
     *
     * @param context Application context.
     * @return Phone number in string.
     */
    public static String getUserTelNo(Context context) {
        try {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tMgr.getLine1Number();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <p>Get user deivce id.</p>
     * Required Permission &lt;uses-permission android:name="android.permission.READ_PHONE_STATE"/&gt;
     *
     * @param context Application context
     * @return Deivce id in string.
     */
    public static String getUserDeviceId(Context context) {
        try {
            if (!hasPermission(context, "android.permission.READ_PHONE_STATE")) {
                throw new Exception();
            }
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getDeviceId();
        } catch (Exception e) {
            return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        }
    }

    public static String getUserDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }


    /* ------------- Security ------------- */
    public static String getKeyHash(Context context) {
        String keyHash = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (Exception e) {
            Log.e("Data Encryption", "Fail to get key hash.", e);
        }
        return keyHash;
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasPermission(Context context, ArrayList<String> permissionsRequired) {
        Boolean hasPermission = true;
        for (String permissionRequired : permissionsRequired) {
            if (ContextCompat.checkSelfPermission(context, permissionRequired) != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                Log.e("PERMISSION", "Permission " + permissionsRequired + " denied.");
            }
        }
        return hasPermission;
    }

    public static boolean hasRootPermission() {
        return new File("/system/bin/su").exists() && new File("/system/xbin/su").exists();
    }

    public static String getRandomString(int length) {
        String result = "";
        String chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz!@#-_";
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            result += chars.charAt(rnd.nextInt(60));
        }
        return result;
    }

    /* ------------- Shared preferences ------------- */

    /**
     * On Lollipop, grab system colorAccent attribute
     * pre-Lollipop, grab AppCompat colorAccent attribute
     * finally, check for custom mp_colorAccent attribute
     *
     * @param context Application context
     * @return accent color of current theme
     */
    @TargetApi(LOLLIPOP)
    public static int resolveAccentColor(Context context) {
        Resources.Theme theme = context.getTheme();
        int attr = SDK_INT >= LOLLIPOP ? android.R.attr.colorAccent : R.attr.colorAccent;
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{attr, R.attr.material_pref_color_accent});

        int accentColor = typedArray.getColor(0, Color.parseColor("#009688"));
        accentColor = typedArray.getColor(1, accentColor);
        typedArray.recycle();

        return accentColor;
    }

    /* ------------- Shared preferences ------------- */

    public static void showSharedPreferences(Context context) {
        showSharedPreferences(context, null);
    }

    public static void showSharedPreferences(Context context, String part) {
        Map<String, ?> keys;
        if (part == null) {
            keys = PreferenceManager.getDefaultSharedPreferences(context).getAll();
        } else {
            keys = context.getSharedPreferences(part, 0).getAll();
        }
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            try {
                Log.d("SharedPreferences", entry.getKey() + ": " + entry.getValue().toString());
            } catch (Exception e) {
                Log.e("SharedPreferences", entry.getKey() + ": catched exception -> " + e.toString());
            }
        }
    }

    /* ------------- Virtual keybroad ------------- */
    public static void showKeyBroad(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(activity.getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception ignored) {
        }
    }

    public static void hideKeyBroad(Activity activity) {
        try {
            View currentView = activity.getCurrentFocus();
            if (currentView == null) {
                throw new Exception("Cannot get current focus");
            }
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(currentView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception ignored) {
        }
    }

    /* ------------- Data parser ------------- */
    public static Bundle queryToBundle(String str) {
        Bundle args = new Bundle();
        String[] varPair = str.split("&");
        for (String var : varPair) {
            String[] thisVar = var.split("=");
            if (thisVar.length < 2) {
                args.putString(thisVar[0], "");
            } else {
                args.putString(thisVar[0], urlDecode(thisVar[1]));
            }
        }
        return args;
    }

    public static Map<String, String> queryToArrayMap(String str) {
        Map<String, String> args;
        args = new LinkedHashMap<String, String>();
        String[] varPair = str.split("&");
        for (String var : varPair) {
            String[] thisVar = var.split("=");
            if (thisVar.length < 2) {
                args.put(thisVar[0], "");
            } else {
                args.put(thisVar[0], urlDecode(thisVar[1]));
            }
        }
        return args;
    }

    /* ------------- Convert metrics ------------- */
    public static float sp2px(Context context, int scaledPixelSize) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, scaledPixelSize, context.getResources().getDisplayMetrics());
    }

    public static float dp2px(Context context, int densityIndependentPixel) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, densityIndependentPixel, context.getResources().getDisplayMetrics());
    }

    public static float px2dp(Context context, int pixel) {
        return (int) (pixel / context.getResources().getDisplayMetrics().density + 0.5f * (pixel >= 0 ? 1 : -1));
    }

    public static float px2sp(Context context, int pixel) {
        return (int) (pixel / context.getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }

}
