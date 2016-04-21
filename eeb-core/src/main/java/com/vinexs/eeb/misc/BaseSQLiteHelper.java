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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import com.vinexs.tool.ArrayManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public abstract class BaseSQLiteHelper extends SQLiteOpenHelper {

    public boolean debug = false;

    private final String TAG = "SQLiteHelper";

    private HashMap<String, Object> sqlOptions = new HashMap<>();

    private String rawSql = "";
    private String[] rawSqlParam = {};

    public static final int SELECT = 0;
    public static final int UPDATE = 1;
    public static final int INSERT = 2;
    public static final int DELETE = 3;

    public BaseSQLiteHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * This method will call when database is not exist.
     * Use db.execSQL(String sql) to create table.
     */
    @Override
    public abstract void onCreate(SQLiteDatabase db);

    /**
     * This method will call when database is already exist.
     * Use db.execSQL(String sql) to alter table.
     */
    @Override
    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public void clearLastSql() {
        sqlOptions = new HashMap<String, Object>();
        rawSql = "";
    }

    // ================  Set value options =========================

    public BaseSQLiteHelper select(String selectFields) {
        Object selectObj = sqlOptions.get("select");
        ArrayList<String> select = (ArrayList<String>) sqlOptions.get("select");
        if (select == null) {
            select = new ArrayList<String>();
        }
        if (selectFields.contains(",")) {
            String[] fields = selectFields.split(",");
            for (String field : fields) {
                String trimedField = field.trim().replaceAll("`", "");
                if (!trimedField.isEmpty()) {
                    select.add(trimedField);
                }
            }
        } else {
            String trimedField = selectFields.trim().replaceAll("`", "");
            select.add(trimedField);
        }
        sqlOptions.put("select", select);
        return this;
    }


    public BaseSQLiteHelper table(String tableName) {
        return from(tableName);
    }

    public BaseSQLiteHelper from(String tableName) {
        sqlOptions.put("from", tableName);
        return this;
    }

    public BaseSQLiteHelper where(String fieldName, Object fieldValue) {
        LinkedHashMap<String, Object> where = (LinkedHashMap<String, Object>) sqlOptions.get("where");
        if (where == null) {
            where = new LinkedHashMap<String, Object>();
        }
        where.put(fieldName, fieldValue);
        sqlOptions.put("where", where);
        return this;
    }

    public BaseSQLiteHelper set(String fieldName, Object fieldValue) {
        LinkedHashMap<String, Object> set = (LinkedHashMap<String, Object>) sqlOptions.get("set");
        if (set == null) {
            set = new LinkedHashMap<String, Object>();
        }
        set.put(fieldName, fieldValue);
        sqlOptions.put("set", set);
        return this;
    }

    public BaseSQLiteHelper insert(String fieldName, Object fieldValue) {
        ContentValues insert = (ContentValues) sqlOptions.get("insert");
        if (insert == null) {
            insert = new ContentValues();
        }
        if (fieldValue instanceof String) {
            insert.put(fieldName, (String) fieldValue);
        } else if (fieldValue instanceof Integer) {
            insert.put(fieldName, (Integer) fieldValue);
        } else if (fieldValue instanceof Long) {
            insert.put(fieldName, (Long) fieldValue);
        } else if (fieldValue instanceof Float) {
            insert.put(fieldName, (Float) fieldValue);
        } else if (fieldValue instanceof Double) {
            insert.put(fieldName, (Double) fieldValue);
        } else if (fieldValue instanceof Boolean) {
            insert.put(fieldName, (Boolean) fieldValue);
        } else if (fieldValue instanceof Short) {
            insert.put(fieldName, (Short) fieldValue);
        } else if (fieldValue instanceof Byte) {
            insert.put(fieldName, (Byte) fieldValue);
        } else if (fieldValue instanceof byte[]) {
            insert.put(fieldName, (byte[]) fieldValue);
        }
        sqlOptions.put("insert", insert);
        return this;
    }

    public BaseSQLiteHelper insert(String[] fieldName, String[] fieldValue) {
        if (fieldName.length != fieldValue.length) {
            Log.e(TAG, "Insert field length do not match with value.");
            return this;
        }
        LinkedHashMap<String, String> insert = (LinkedHashMap<String, String>) sqlOptions.get("insert");
        if (insert == null) {
            insert = new LinkedHashMap<String, String>();
        }
        for (int i = 0; i < fieldName.length; i++) {
            insert.put(fieldName[i], fieldValue[i]);
        }
        sqlOptions.put("insert", insert);
        return this;
    }

    public BaseSQLiteHelper limit(int limitRow) {
        limit(0, limitRow);
        return this;
    }

    public BaseSQLiteHelper limit(int limitFrom, int limitTo) {
        int limit[] = {limitFrom, limitTo};
        sqlOptions.put("limit", limit);
        return this;
    }

    public BaseSQLiteHelper order(String orderStr) {
        ArrayList<String> order = (ArrayList<String>) sqlOptions.get("order");
        if (order == null) {
            order = new ArrayList<String>();
        }
        if (orderStr.contains(",")) {
            String[] fields = orderStr.split(",");
            for (String field : fields) {
                String trimedField = field.trim();
                if (!trimedField.isEmpty()) {
                    order.add(trimedField);
                }
            }
        } else {
            String trimedField = orderStr.trim();
            order.add(trimedField);
        }
        sqlOptions.put("order", order);
        return this;
    }

    public BaseSQLiteHelper group(String groupStr) {
        ArrayList<String> group = (ArrayList<String>) sqlOptions.get("group");
        if (group == null) {
            group = new ArrayList<String>();
        }
        if (groupStr.contains(",")) {
            String[] fields = groupStr.split(",");
            for (String field : fields) {
                String trimedField = field.trim();
                if (!trimedField.isEmpty()) {
                    group.add(trimedField);
                }
            }
        } else {
            String trimedField = groupStr.trim();
            group.add(trimedField);
        }
        sqlOptions.put("order", group);
        return this;
    }

    // ================  Perform actions =========================

    public ArrayList<Bundle> query(String rawSql) {
        this.rawSql = rawSql;
        return query();
    }

    public ArrayList<Bundle> query() {
        ArrayList<Bundle> result = new ArrayList<>();
        if (rawSql.isEmpty()) {
            Sql sql = buildSelectSQL();
            if (sql == null) {
                Log.e(TAG, "Query action unable to build sql.");
                return result;
            }
            rawSql = sql.sql;
            rawSqlParam = sql.params;
        }
        if (debug) {
            Log.d(TAG, "SQL: " + rawSql);
            Log.d(TAG, "VAR: " + ArrayManager.join(rawSqlParam, ","));
        }
        try {
            Cursor cursor = getReadableDatabase().rawQuery(rawSql, rawSqlParam);
            cursor.moveToFirst();
            Integer colCount = cursor.getColumnCount();
            while (!cursor.isAfterLast()) {
                Bundle record = new Bundle();
                for (int i = 0; i < colCount; i++) {
                    record.putString(cursor.getColumnName(i), cursor.getString(i));
                }
                result.add(record);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "SQL: " + rawSql);
            Log.e(TAG, "VAR: " + ArrayManager.join(rawSqlParam, ","));
        } finally {
            close();
            clearLastSql();
        }
        return result;
    }

    public boolean execUpdate(String rawSql) {
        this.rawSql = rawSql;
        return execUpdate();
    }

    public boolean execUpdate() {
        if (rawSql.isEmpty()) {
            Sql sql = buildUpdateSQL();
            if (sql == null) {
                Log.e(TAG, "Update action unable to build sql.");
                return false;
            }
            rawSql = sql.sql;
            rawSqlParam = sql.params;
        }
        if (debug) {
            Log.d(TAG, "SQL: " + rawSql);
            Log.d(TAG, "VAR: " + ArrayManager.join(rawSqlParam, ","));
        }
        Boolean result = false;
        try {
            getWritableDatabase().execSQL(rawSql);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "SQL: " + rawSql);
            Log.e(TAG, "VAR: " + ArrayManager.join(rawSqlParam, ","));
        } finally {
            close();
            clearLastSql();
        }
        return result;
    }

    public long execInsert(String rawSql) {
        this.rawSql = rawSql;
        return execInsert();
    }

    public long execInsert() {
        if (rawSql.isEmpty()) {
            Sql sql = buildInsertSQL();
            if (sql == null) {
                Log.e(TAG, "Insert action unable to build sql.");
                return 0;
            }
            rawSql = sql.sql;
            rawSqlParam = sql.params;
        }
        if (debug) {
            Log.d(TAG, "SQL: " + rawSql);
            Log.d(TAG, "VAR: " + ArrayManager.join(rawSqlParam, ","));
        }
        long id = 0;
        try {
            ContentValues values = (ContentValues) sqlOptions.get("insert");
            id = getWritableDatabase().insert((String) sqlOptions.get("from"), null, values);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "SQL: " + rawSql);
            Log.e(TAG, "VAR: " + ArrayManager.join(rawSqlParam, ","));
        } finally {
            close();
            clearLastSql();
        }
        return id;
    }

    public int execDelete(String rawSql) {
        this.rawSql = rawSql;
        return execDelete();
    }

    public int execDelete() {
        if (rawSql.isEmpty()) {
            Sql sql = buildDeleteSQL();
            if (sql == null) {
                Log.e(TAG, "Delete action unable to build sql.");
                return 0;
            }
            rawSql = sql.sql;
            rawSqlParam = sql.params;
        }
        if (debug) {
            Log.d(TAG, "SQL: " + rawSql);
            Log.d(TAG, "VAR: " + ArrayManager.join(rawSqlParam, ","));
        }
        int rowAffected = 0;
        try {
            String[] sqlSplit = rawSql.split(" WHERE ");
            if (sqlSplit.length == 1) {
                // No where case
                rowAffected = getWritableDatabase().delete((String) sqlOptions.get("from"), null, null);
            } else {
                String whereCase = sqlSplit[1].trim();
                rowAffected = getWritableDatabase().delete((String) sqlOptions.get("from"), whereCase, rawSqlParam);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "SQL: " + rawSql);
            Log.e(TAG, "VAR: " + ArrayManager.join(rawSqlParam, ","));
        } finally {
            close();
            clearLastSql();
        }
        return rowAffected;
    }

    public Sql buildSelectSQL() {
        Sql sql = new Sql();
        if (!sqlOptions.containsKey("from")) {
            Log.e(TAG, "Query action require [form] value.");
            return null;
        }
        // Select
        sql.sql = "SELECT ";
        if (!sqlOptions.containsKey("select")) {
            sql.sql += "* ";
        } else {
            ArrayList<String> select = (ArrayList<String>) sqlOptions.get("select");
            String selectStr = ArrayManager.join(select, ", ");
            sql.sql += selectStr + " ";
        }
        // From
        sql.sql += "FROM `" + (String) sqlOptions.get("from") + "` ";
        // Where
        if (sqlOptions.containsKey("where")) {
            LinkedHashMap<String, Object> where = (LinkedHashMap<String, Object>) sqlOptions.get("where");
            ArrayList<String> whereSql = new ArrayList<String>();
            for (Map.Entry<String, Object> whereOpt : where.entrySet()) {
                Object value = whereOpt.getValue();
                if (value instanceof String || value instanceof CharSequence) {
                    if (whereOpt.getKey().startsWith("@")) {
                        whereSql.add((String) value);
                    } else {
                        whereSql.add("`" + whereOpt.getKey() + "` = ?");
                        sql.params = ArrayManager.push(sql.params, (String) value);
                    }
                } else if (value instanceof Integer || value instanceof Long) {
                    if (whereOpt.getKey().startsWith("@")) {
                        whereSql.add((String) value);
                    } else {
                        whereSql.add("`" + whereOpt.getKey() + "` = ?");
                        sql.params = ArrayManager.push(sql.params, "" + value);
                    }
                } else if (value instanceof String[] || value instanceof CharSequence[]) {
                    String[] strArr = (String[]) value;
                    if (strArr.length <= 1) {
                        continue;
                    }
                    if (whereOpt.getKey().startsWith("@")) {
                        whereSql.add((String) strArr[0]);
                        sql.params = ArrayManager.push(sql.params, Arrays.copyOfRange(strArr, 1, strArr.length));
                    } else {
                        whereSql.add("`" + whereOpt.getKey() + "` = ?");
                        sql.params = ArrayManager.push(sql.params, Arrays.copyOfRange(strArr, 1, strArr.length));
                    }
                }
            }
            sql.sql += "WHERE (" + ArrayManager.join(whereSql, ") AND (") + ") ";
        }
        // Order & Group
        if (sqlOptions.containsKey("order") || sqlOptions.containsKey("group")) {
            if (sqlOptions.containsKey("order")) {
                // Order
                ArrayList<String> order = (ArrayList<String>) sqlOptions.get("order");
                String orderStr = ArrayManager.join(order, "`, `");
                sql.sql += "ORDER BY " + orderStr + " ";
            } else {
                // Group
                ArrayList<String> group = (ArrayList<String>) sqlOptions.get("group");
                String groupStr = ArrayManager.join(group, "`, `");
                sql.sql += "ORDER BY " + groupStr + " ";
            }
        }
        // Limit
        if (sqlOptions.containsKey("limit")) {
            int[] limit = (int[]) sqlOptions.get("limit");
            sql.sql += "LIMIT " + limit[0] + "," + limit[1];
        }
        return sql;
    }

    public Sql buildUpdateSQL() {
        Sql sql = new Sql();
        if (!sqlOptions.containsKey("from")) {
            Log.e(TAG, "Update action require [form] value.");
            return null;
        }
        if (!sqlOptions.containsKey("set")) {
            Log.e(TAG, "Update action require [set] value.");
            return null;
        }
        // From
        sql.sql = "UPDATE `" + (String) sqlOptions.get("from") + "` ";
        // Set
        LinkedHashMap<String, Object> set = (LinkedHashMap<String, Object>) sqlOptions.get("set");
        ArrayList<String> setSql = new ArrayList<String>();
        for (Map.Entry<String, Object> setOpt : set.entrySet()) {
            Object value = setOpt.getValue();
            if (value instanceof String || value instanceof CharSequence) {
                if (setOpt.getKey().startsWith("@")) {
                    setSql.add((String) value);
                } else {
                    setSql.add("`" + setOpt.getKey() + "` = ?");
                    sql.params = ArrayManager.push(sql.params, (String) value);
                }
            } else if (value instanceof Integer || value instanceof Long) {
                if (setOpt.getKey().startsWith("@")) {
                    setSql.add((String) value);
                } else {
                    setSql.add("`" + setOpt.getKey() + "` = ?");
                    sql.params = ArrayManager.push(sql.params, "" + value);
                }
            } else if (value instanceof String[] || value instanceof CharSequence[]) {
                String[] strArr = (String[]) value;
                if (strArr.length <= 1) {
                    continue;
                }
                if (setOpt.getKey().startsWith("@")) {
                    setSql.add((String) strArr[0]);
                    sql.params = ArrayManager.push(sql.params, Arrays.copyOfRange(strArr, 1, strArr.length));
                } else {
                    setSql.add("`" + setOpt.getKey() + "` = ?");
                    sql.params = ArrayManager.push(sql.params, Arrays.copyOfRange(strArr, 1, strArr.length));
                }
            }
        }
        sql.sql += "SET " + ArrayManager.join(setSql, ", ") + " ";
        // Where
        if (sqlOptions.containsKey("where")) {
            LinkedHashMap<String, Object> where = (LinkedHashMap<String, Object>) sqlOptions.get("where");
            ArrayList<String> whereSql = new ArrayList<String>();
            for (Map.Entry<String, Object> whereOpt : where.entrySet()) {
                Object value = whereOpt.getValue();
                if (value instanceof String || value instanceof CharSequence) {
                    if (whereOpt.getKey().startsWith("@")) {
                        whereSql.add((String) value);
                    } else {
                        whereSql.add("`" + whereOpt.getKey() + "` = ?");
                        sql.params = ArrayManager.push(sql.params, (String) value);
                    }
                } else if (value instanceof Integer || value instanceof Long) {
                    if (whereOpt.getKey().startsWith("@")) {
                        whereSql.add((String) value);
                    } else {
                        whereSql.add("`" + whereOpt.getKey() + "` = ?");
                        sql.params = ArrayManager.push(sql.params, "" + value);
                    }
                } else if (value instanceof String[] || value instanceof CharSequence[]) {
                    String[] strArr = (String[]) value;
                    if (strArr.length <= 1) {
                        continue;
                    }
                    if (whereOpt.getKey().startsWith("@")) {
                        whereSql.add((String) strArr[0]);
                        sql.params = ArrayManager.push(sql.params, Arrays.copyOfRange(strArr, 1, strArr.length));
                    } else {
                        whereSql.add("`" + whereOpt.getKey() + "` = ?");
                        sql.params = ArrayManager.push(sql.params, Arrays.copyOfRange(strArr, 1, strArr.length));
                    }
                }
            }
            sql.sql += "WHERE (" + ArrayManager.join(whereSql, ") AND (") + ") ";
        }
        // Order
        if (sqlOptions.containsKey("order")) {
            ArrayList<String> order = (ArrayList<String>) sqlOptions.get("order");
            String orderStr = ArrayManager.join(order, "`, `");
            sql.sql += "ORDER BY " + orderStr + " ";
        }
        // Limit
        if (sqlOptions.containsKey("limit")) {
            int[] limit = (int[]) sqlOptions.get("limit");
            sql.sql += "LIMIT " + limit[0] + "," + limit[1];
        }
        return sql;
    }

    public Sql buildInsertSQL() {
        Sql sql = new Sql();
        if (!sqlOptions.containsKey("from")) {
            Log.e(TAG, "Insert action require [form] value.");
            return null;
        }
        if (!sqlOptions.containsKey("insert")) {
            Log.e(TAG, "Insert action require [insert] value.");
            return null;
        }
        // From
        sql.sql = "INSERT INTO `" + (String) sqlOptions.get("from") + "` ";
        // Insert value
        ContentValues insert = (ContentValues) sqlOptions.get("insert");
        ArrayList<String> fieldName = new ArrayList<String>();
        ArrayList<String> BindValue = new ArrayList<String>();
        ArrayList<String> value = new ArrayList<String>();
        Set<Map.Entry<String, Object>> contValue = insert.valueSet();
        for (Object aContValue : contValue) {
            Map.Entry entry = (Map.Entry) aContValue;
            fieldName.add(entry.getKey().toString());
            value.add(entry.getValue().toString());
            BindValue.add("?");
        }
        sql.sql += "(" + ArrayManager.join(fieldName, ", ") + ") VALUES (" + ArrayManager.join(BindValue, ", ") + ");";
        sql.params = ArrayManager.push(sql.params, value);
        return sql;
    }

    public Sql buildDeleteSQL() {
        Sql sql = new Sql();
        if (!sqlOptions.containsKey("from")) {
            Log.e(TAG, "Insert action require [form] value.");
            return null;
        }
        // From
        sql.sql = "DELETE FROM `" + (String) sqlOptions.get("from") + "` ";
        // Where
        if (sqlOptions.containsKey("where")) {
            LinkedHashMap<String, Object> where = (LinkedHashMap<String, Object>) sqlOptions.get("where");
            ArrayList<String> whereSql = new ArrayList<String>();
            for (Map.Entry<String, Object> whereOpt : where.entrySet()) {
                Object value = whereOpt.getValue();
                if (value instanceof String || value instanceof CharSequence) {
                    if (whereOpt.getKey().startsWith("@")) {
                        whereSql.add((String) value);
                    } else {
                        whereSql.add("`" + whereOpt.getKey() + "` = ?");
                        sql.params = ArrayManager.push(sql.params, (String) value);
                    }
                } else if (value instanceof Integer || value instanceof Long) {
                    if (whereOpt.getKey().startsWith("@")) {
                        whereSql.add((String) value);
                    } else {
                        whereSql.add("`" + whereOpt.getKey() + "` = ?");
                        sql.params = ArrayManager.push(sql.params, "" + value);
                    }
                } else if (value instanceof String[] || value instanceof CharSequence[]) {
                    String[] strArr = (String[]) value;
                    if (strArr.length <= 1) {
                        continue;
                    }
                    if (whereOpt.getKey().startsWith("@")) {
                        whereSql.add((String) strArr[0]);
                        sql.params = ArrayManager.push(sql.params, Arrays.copyOfRange(strArr, 1, strArr.length));
                    } else {
                        whereSql.add("`" + whereOpt.getKey() + "` = ?");
                        sql.params = ArrayManager.push(sql.params, Arrays.copyOfRange(strArr, 1, strArr.length));
                    }
                }
            }
            sql.sql += "WHERE (" + ArrayManager.join(whereSql, ") AND (") + ") ";
        }
        return sql;
    }

    public static class Sql {
        public String sql = "";
        public String[] params = {};
    }

}