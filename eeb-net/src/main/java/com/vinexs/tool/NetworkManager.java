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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtilsHC4;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@SuppressWarnings("unused")
public class NetworkManager {

    public static boolean haveNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return (netInfo!=null && netInfo.isConnected());
    }

    public static void haveInternet(Context context, final OnInternetResponseListener listener) {
        if (!haveNetwork(context)) {
            listener.onResponsed(false);
            return;
        }
        Log.d("Network", "Check internet is reachable ...");
        new AsyncTask<Void,Integer, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    InetAddress ipAddr = InetAddress.getByName("google.com");
                    if (ipAddr.toString().equals("")) {
                        throw new Exception("Cannot resolve host name, no internet behind network.");
                    }
                    HttpURLConnection conn = (HttpURLConnection) new URL("http://google.com/").openConnection();
                    conn.setInstanceFollowRedirects(false);
                    conn.setConnectTimeout(3500);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    DataOutputStream postOutputStream = new DataOutputStream(conn.getOutputStream());
                    postOutputStream.write('\n');
                    postOutputStream.close();
                    conn.disconnect();
                    Log.d("Network", "Internet is reachable.");
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("Network", "Internet is unreachable.");
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                listener.onResponsed(result);
            }
        }.execute();
    }

    public static boolean isWifiNetwork(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static InetAddress getInetAddress(Context context) {
        if (haveNetwork(context)) {
            return null;
        }
        if (isWifiNetwork(context)) {
            int ipAddress = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getIpAddress();
            if (ipAddress == 0) {
                return null;
            }
            return intToInet(ipAddress);
        }
        try {
            Enumeration<NetworkInterface> netinterfaces = NetworkInterface.getNetworkInterfaces();
            while (netinterfaces.hasMoreElements()) {
                NetworkInterface netinterface = netinterfaces.nextElement();
                Enumeration<InetAddress> adresses = netinterface.getInetAddresses();
                while (adresses.hasMoreElements()) {
                    InetAddress address = adresses.nextElement();
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                        return address;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getLocalIPAddress(){
        try {
            InetAddress meHost = InetAddress.getLocalHost();
            return meHost.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtilsHC4.isIPv4Address(sAddr);
                        if (useIPv4) {
                            if (isIPv4) {
                                return sAddr;
                            }
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    public static String getSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo ();
        return info.getBSSID ();
    }

    public static InetAddress intToInet(int value) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = byteOfInt(value, i);
        }
        try {
            return InetAddress.getByAddress(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte byteOfInt(int value, int which) {
        int shift = which * 8;
        return (byte) (value >> shift);
    }

    public interface OnInternetResponseListener {
        void onResponsed(boolean haveInternet);
    }

}
