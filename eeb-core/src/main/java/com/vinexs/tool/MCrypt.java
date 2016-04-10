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
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings("unused")
public class MCrypt {

    @SuppressLint("TrulyRandom")
    public static String base64_encrypt(String input, String secretKey, String initializationVector) {
        if (input == null || input.length() == 0) {
            Log.d("MCrypt", "Empty input.");
            return null;
        }
        byte[] encrypted = null;
        SecretKeySpec keyspec = new SecretKeySpec(secretKey.getBytes(), "AES");
        IvParameterSpec ivspec = new IvParameterSpec(initializationVector.getBytes());
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

            encrypted = cipher.doFinal(padString(input));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (encrypted != null) ? android.util.Base64.encodeToString(encrypted, 0) : null;
    }


    public static String base64_decrypt(String code, String secretKey, String initializationVector) {
        if (code == null || code.length() == 0) {
            Log.d("MCrypt", "Empty input.");
            return null;
        }
        byte[] decrypted = null;
        SecretKeySpec keyspec = new SecretKeySpec(secretKey.getBytes(), "AES");
        IvParameterSpec ivspec = new IvParameterSpec(initializationVector.getBytes());
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            decrypted = cipher.doFinal(android.util.Base64.decode(code, 0));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (decrypted != null) ? new String(decrypted).trim() : null;
    }

    public static String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        }
        int len = data.length;
        String str = "";
        for (byte aData : data) {
            if ((aData & 0xFF) < 16) {
                str = str + "0" + Integer.toHexString(aData & 0xFF);
            } else {
                str = str + Integer.toHexString(aData & 0xFF);
            }
        }
        return str;
    }

    private static byte[] padString(String source) {
        byte[] sourcebyte = source.getBytes();
        int blocksize = 16;
        int padding_size = blocksize - sourcebyte.length % blocksize;
        if (padding_size == blocksize) padding_size = 0;
        byte[] newbyte = new byte[sourcebyte.length + padding_size];
        System.arraycopy(sourcebyte, 0, newbyte, 0, sourcebyte.length);
        return newbyte;
    }

}
