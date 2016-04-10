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

import android.util.Patterns;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Determinator {

    // = = = = Determine Number = = = =
    public static boolean isInteger(String str) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isNumeric(String str) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    // = = = = Determine String = = = =
    public static boolean isHex(String hexCode) {
        Pattern hexPattern = Pattern.compile("^(#|)[0-9A-Fa-f]+$");
        return hexPattern.matcher(hexCode).matches();
    }

    public static boolean isPhoneNumber(String phoneNumber) {
        return Patterns.PHONE.matcher(phoneNumber).matches();
    }

    public static boolean isIPAddress(String ip) {
        return Patterns.IP_ADDRESS.matcher(ip).matches();
    }

    public static boolean isDomain(String domain) {
        return Patterns.DOMAIN_NAME.matcher(domain).matches();
    }

    public static boolean isUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }

    public static boolean isSecureUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches() && url.startsWith("https://");
    }

    public static boolean isEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
