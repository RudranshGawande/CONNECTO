//package com.megaproject.urbanspace.Database;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//
//public class DBHelper extends SQLiteOpenHelper {
//
//    private static final String DATABASE_NAME = "UrbanSpace.db";
//    private static final int DATABASE_VERSION = 1;
//
//    private static final String TABLE_USERS = "users";
//
//    private static final String COL_ID = "id";
//    private static final String COL_FULLNAME = "full_name";
//    private static final String COL_USERNAME = "username";
//    private static final String COL_EMAIL = "email";
//    private static final String COL_DOB = "dob";
//    private static final String COL_PASSWORD = "password";
//
//    public DBHelper(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        String createTable = "CREATE TABLE " + TABLE_USERS + " ("
//                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//                + COL_FULLNAME + " TEXT NOT NULL, "
//                + COL_USERNAME + " TEXT UNIQUE NOT NULL, "
//                + COL_EMAIL + " TEXT UNIQUE NOT NULL, "
//                + COL_DOB + " TEXT, "
//                + COL_PASSWORD + " TEXT NOT NULL)";
//        db.execSQL(createTable);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
//        onCreate(db);
//    }
//
//    // Insert new user
//    public boolean insertUser(String fullName, String username, String email, String dob, String password) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        // Check if email or username already exists
//        if (checkUserExists(username, email)) {
//            return false;
//        }
//
//        ContentValues values = new ContentValues();
//        values.put(COL_FULLNAME, fullName);
//        values.put(COL_USERNAME, username);
//        values.put(COL_EMAIL, email);
//        values.put(COL_DOB, dob);
//        values.put(COL_PASSWORD, password);
//
//        long result = db.insert(TABLE_USERS, null, values);
//        return result != -1;
//    }
//
//    // Check if user exists by username or email
//    public boolean checkUserExists(String username, String email) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS +
//                        " WHERE " + COL_USERNAME + " = ? OR " + COL_EMAIL + " = ?",
//                new String[]{username, email});
//        boolean exists = cursor.getCount() > 0;
//        cursor.close();
//        return exists;
//    }
//
//    // Login check by username/email and password
//    public boolean checkLogin(String usernameOrEmail, String password) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS +
//                        " WHERE (" + COL_USERNAME + " = ? OR " + COL_EMAIL + " = ?) AND " + COL_PASSWORD + " = ?",
//                new String[]{usernameOrEmail, usernameOrEmail, password});
//        boolean loginSuccess = cursor.getCount() > 0;
//        cursor.close();
//        return loginSuccess;
//    }
//}

