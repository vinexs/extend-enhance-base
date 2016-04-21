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
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("unused")
public class FileIO {

    public static String getPathFromUri(Context context, Uri uri) {
        String uploadFileLocation = "";
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor == null) {
                throw new Exception("Uri cursor return null.");
            }
            if (cursor.moveToFirst()) {
                uploadFileLocation = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            }
            cursor.close();
            return uploadFileLocation;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean decompress(File zipFile, String decompressTo, OnDecompressListener listener) {
        try {
            return decompress(new FileInputStream(zipFile), decompressTo, listener);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean decompress(InputStream zipFileInputStream, String decompressTo, OnDecompressListener listener) {
        return decompress(new ZipInputStream(zipFileInputStream), decompressTo, listener);
    }

    public static boolean decompress(FileInputStream zipFileInputStream, String decompressTo, OnDecompressListener listener) {
        return decompress(new ZipInputStream(zipFileInputStream), decompressTo, listener);
    }

    public static boolean decompress(ZipInputStream zipInputStream, String decompressTo, OnDecompressListener listener) {
        File decompressPath = new File(decompressTo);
        if (decompressPath.exists() && decompressPath.isFile()) {
            Log.d("FileIO.decompress", decompressPath + " is a file, aborted. ");
            return false;
        }
        if (!decompressPath.exists()) {
            Log.d("FileIO.decompress", decompressPath + " not exists, create directory. ");
            //noinspection ResultOfMethodCallIgnored
            decompressPath.mkdirs();
        }
        ZipEntry zipEntry;
        FileOutputStream fileOutputStream;
        try {
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                if (listener != null) {
                    listener.onDecompress(decompressTo, entryName);
                }
                if (zipEntry.isDirectory()) {
                    File dir = new File(decompressTo + entryName);
                    if (!dir.isDirectory()) {
                        Log.d("FileIO.decompress", "Create directory: " + entryName);
                        //noinspection ResultOfMethodCallIgnored
                        dir.mkdirs();
                    }
                } else {
                    Log.d("FileIO.decompress", "Extract file: " + entryName);
                    fileOutputStream = new FileOutputStream(new File(decompressTo, entryName));
                    byte[] buffer = new byte[4096 * 10];
                    int length;
                    while ((length = zipInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    zipInputStream.closeEntry();
                    fileOutputStream.close();
                }
            }
            zipInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean appendDataToFile(File source, File dist, String[] lines) {
        try {
            List<String> fileLines = getLines(source);
            if (fileLines == null) {
                throw new Exception("File content is empty.");
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(dist));
            Collections.addAll(fileLines, lines);
            writeLines(writer, fileLines);
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean prependDataToFile(File source, File dist, String[] lines) {
        try {
            List<String> fileLines = new ArrayList<>();
            Collections.addAll(fileLines, lines);
            List<String> oriLines = getLines(source);
            if (oriLines != null) {
                for (String line : oriLines) {
                    fileLines.add(line);
                }
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(dist));
            writeLines(writer, fileLines);
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean fileContentReplace(File source, File dist, Map<String, String> keyVal) {
        return fileContentReplace(source, dist, keyVal, "{%");
    }

    public static boolean fileContentReplace(File source, File dist, Map<String, String> keyVal, String startWithPatten) {
        try {
            for (Entry<String, String> entry : keyVal.entrySet()) {
                if (!entry.getKey().startsWith(startWithPatten)) {
                    throw new Exception("Replace key must start with [ " + startWithPatten + " ].");
                }
            }
            List<String> fileLines = new ArrayList<>();
            List<String> oriLines = getLines(source);
            if (oriLines != null) {
                for (String line : oriLines) {
                    if (!line.contains(startWithPatten)) {
                        fileLines.add(line);
                    } else {
                        for (Entry<String, String> entry : keyVal.entrySet()) {
                            line = line.replace(entry.getKey(), entry.getValue());
                        }
                        fileLines.add(line);
                    }
                }
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(dist));
            writeLines(writer, fileLines);
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getLines(File file) {
        List<String> lines = new ArrayList<>();
        Scanner scanner;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
            scanner.close();
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean writeLines(BufferedWriter writer, List<String> lines) {
        try {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Do NOT use this method to extract large size of text, it cost lot of time.
     */
    public static String getContentFromAssets(Context context, String filepath) {
        StringBuilder returnString = new StringBuilder();
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferReader = null;
        try {
            inputStream = context.getResources().getAssets().open(filepath);
            inputStreamReader = new InputStreamReader(inputStream);
            bufferReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferReader.readLine()) != null) {
                returnString.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferReader != null) {
                    bufferReader.close();
                }
            } catch (Exception ignored) {
            }
        }
        return returnString.toString();
    }

    public static void copyAssetsToLocation(Context context, File distFolder) {
        copyAssetsToLocation(context, distFolder, "");
    }

    public static void copyAssetsToLocation(Context context, File distFolder, String assetsSubFolder) {
        if (!distFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            distFolder.mkdirs();
        }
        String assets[];
        try {
            assets = context.getAssets().list(assetsSubFolder);
            if (assets.length == 0) {
                if (!assetsSubFolder.equals("")) {
                    copyFile(context.getAssets().open(assetsSubFolder), new File(distFolder, assetsSubFolder));
                }
            } else {
                File distSubFolder = new File(distFolder, assetsSubFolder);
                if (!distSubFolder.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    distSubFolder.mkdirs();
                }
                for (String asset : assets) {
                    copyAssetsToLocation(context, distFolder, assetsSubFolder + "/" + asset);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyAssetsToInternal(Context context) {
        copyAssetsToInternal(context, "");
    }

    public static void copyAssetsToInternal(Context context, String subFolder) {
        String assets[];
        String internalStorage;
        try {
            internalStorage = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir;
            assets = context.getAssets().list(subFolder);
            if (assets.length == 0) {
                if (!subFolder.equals("")) {
                    copyFile(context.getAssets().open(subFolder), new File(internalStorage, subFolder));
                }
            } else {
                String fullPath = internalStorage + "/" + subFolder;
                File dir = new File(fullPath);
                if (!dir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdirs();
                }
                for (String asset : assets) {
                    copyAssetsToInternal(context, fullPath + "/" + asset);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFileOrDir(File source, File dest) {
        String files[];
        try {
            files = source.list();
            if (files == null) {
                copyFile(source, dest);
            } else {
                if (!dest.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    dest.mkdirs();
                }
                for (String file : files) {
                    copyFileOrDir(new File(file), new File(dest, file));
                }
            }
        } catch (Exception e) {
            Log.e("CopyFile ", "Cannot copy " + source.toString());
            e.printStackTrace();
        }
    }

    public static void cleanDirectory(File folder) {
        if (!folder.exists()) {
            return;
        }
        for (File file : folder.listFiles()) {
            if (file != null) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

    public static void copyFile(File source, File dest) {
        try {
            copyFile(new FileInputStream(source), dest);
        } catch (Exception e) {
            Log.e("CopyFile ", "Cannot copy " + source.toString());
            e.printStackTrace();
        }
    }

    public static void copyFile(InputStream source, File dest) {
        try {
            OutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = source.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            source.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e("CopyFile ", "Cannot copy " + source.toString());
            e.printStackTrace();
        }
    }


    public interface OnDecompressListener {
        void onDecompress(String path, String name);
    }

}
