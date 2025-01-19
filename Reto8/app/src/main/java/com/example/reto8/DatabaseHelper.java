package com.example.reto8;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2; // Increment version
    private static final String DATABASE_NAME = "MyDatabase.db";
    private static final String TABLE_NAME = "Empresas";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "Nombre";
    private static final String COL_3 = "Categoria";
    private static final String COL_4 = "URL";
    private static final String COL_5 = "Telefono";
    private static final String COL_6 = "Email";
    private static final String COL_7 = "ProductosServicios";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Empresas (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Nombre TEXT, " +
                "Telefono TEXT, " + // No accent here
                "Categoria TEXT, " +
                "ProductosServicios TEXT, " +
                "URL TEXT, " +
                "Email TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String name, String url, int phone, String email, String pys, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, category);
        contentValues.put(COL_4, url);
        contentValues.put(COL_5, phone);
        contentValues.put(COL_6,email);
        contentValues.put(COL_7,pys);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public boolean updateData(String id, String name, String url, int phone, String email,
        String pys, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, category);
        contentValues.put(COL_4,url);
        contentValues.put(COL_5,phone);
        contentValues.put(COL_6,email);
        contentValues.put(COL_7,pys);
        int result = db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{id});
        return result > 0;
    }

    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[]{id});
    }
    public Cursor searchUser(String name, Integer age) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE NAME LIKE ?";
        if (age != null) {
            query += " AND AGE = ?";
            return db.rawQuery(query, new String[]{"%" + name + "%", String.valueOf(age)});
        }
        return db.rawQuery(query, new String[]{"%" + name + "%"});
    }
    public Cursor searchUserWithMultipleAges(String name, List<String> ageCategories) {
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE 1=1");

        // Add name filter if provided
        if (name != null && !name.isEmpty()) {
            queryBuilder.append(" AND Nombre LIKE '%").append(name).append("%'");
        }

        // Add category filter if provided
        if (ageCategories != null && !ageCategories.isEmpty()) {
            queryBuilder.append(" AND (");
            for (int index = 0; index < ageCategories.size(); index++) {
                String category = ageCategories.get(index);
                if (index > 0) queryBuilder.append(" OR ");
                queryBuilder.append("Categoria LIKE '%").append(category).append("%'");
            }
            queryBuilder.append(")");
        }

        return db.rawQuery(queryBuilder.toString(), null);
    }


}
