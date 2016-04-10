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

package com.vinexs.eeb.misc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.vinexs.tool.FileIO;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused")
public abstract class BaseAssetSQLiteHelper extends BaseSQLiteHelper {

    private Context context;
    private String name;

    public BaseAssetSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
        this.name = name;
        File database = new File(new File(context.getCacheDir().getParentFile(), "databases"), name);
        if (!database.exists()) {
            SQLiteDatabase db = getReadableDatabase();
            db.close();
            try {
                copyDatabaseFromAssets(database);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void copyDatabaseFromAssets(File database) {
        try {
            Log.d("BaseAssetSQLiteHelper", "Coping database [" + name + "] to internal storage.");
            FileIO.copyFile(context.getAssets().open(name), database);
            Log.d("BaseAssetSQLiteHelper", "Database [" + name + "] was copied.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

}