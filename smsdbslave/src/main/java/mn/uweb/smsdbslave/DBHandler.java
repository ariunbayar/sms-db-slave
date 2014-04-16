package mn.uweb.smsdbslave;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String
            DB_NAME = "uweb",
            TABLE_SMS = "sms",
            FIELD_ID = "id",
            FIELD_PHONE = "phone",
            FIELD_BODY = "body",
            FIELD_STATUS = "status",
            FIELD_SMS_ID = "sms_id",
            FIELD_SYNCED = "synced",
            FIELD_CREATED_AT = "created_at";

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_SMS + " (" +
                        FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FIELD_PHONE + " TEXT, " +
                        FIELD_BODY + " TEXT, " +
                        FIELD_STATUS + " INTEGER, " +
                        FIELD_SMS_ID + " INTEGER, " +
                        FIELD_SYNCED + " INTEGER, " +
                        FIELD_CREATED_AT + " INTEGER" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String backup_table = String.valueOf(System.currentTimeMillis());

        db.execSQL("ALTER TABLE " + TABLE_SMS + " RENAME TO " + backup_table);
        // TODO ability to list sms from old tables

        onCreate(db);
    }

    public boolean insert(SMS sms) {
        ContentValues values = new ContentValues();

        values.put(FIELD_PHONE, sms.getPhone());
        values.put(FIELD_BODY, sms.getBody());
        values.put(FIELD_STATUS, sms.getStatus());
        if (sms.getSMSId() != null) {
            values.put(FIELD_SMS_ID, sms.getSMSId());
        }
        values.put(FIELD_SYNCED, sms.getSynced());
        values.put(FIELD_CREATED_AT, sms.getCreatedAt());

        SQLiteDatabase db = getWritableDatabase();
        if (db == null) {
            Exception e = new Exception("Couldn't get writable database!");
            ACRA.getErrorReporter().handleException(e);
            return false;
        }

        db.insert(TABLE_SMS, null, values);
        db.close();
        return true;
    }

    public Boolean has(int sms_id) {
        String query = "SELECT * FROM " + TABLE_SMS + " WHERE " + FIELD_SMS_ID + "=?";
        String[] args = new String[] { String.valueOf(sms_id) };

        SQLiteDatabase db = getReadableDatabase();
        if (db == null) {
            Exception e = new Exception("Couldn't get readable database!");
            ACRA.getErrorReporter().handleException(e);
            return null;
        }

        Cursor cursor = db.rawQuery(query, args);
        int count = cursor.getCount();
        db.close();
        cursor.close();

        return count > 0;
    }

    public Integer count() {
        SQLiteDatabase db = getReadableDatabase();
        if (db == null) {
            Exception e = new Exception("Couldn't get readable database!");
            ACRA.getErrorReporter().handleException(e);
            return null;
        }

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SMS, null);
        int count = cursor.getCount();
        db.close();
        cursor.close();

        return count;
    }

    /*
    public int updateContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PHONE, contact.getPhone());
        values.put(KEY_EMAIL, contact.getEmail());
        values.put(KEY_ADDRESS, contact.getAddress());
        values.put(KEY_IMAGEURI, contact.getImageURI().toString());

        int rowsAffected = db.update(TABLE_CONTACTS, values, KEY_ID + "=?", new String[] { String.valueOf(contact.getId()) });
        db.close();

        return rowsAffected;
    }
    */

    /*
    public void deleteContact(SMS sms) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, ID + "=?", new String[] { String.valueOf(sms.getId()) });
        db.close();
    }
    */


    public List<SMS> getAll() {
        List<SMS> sms_list = new ArrayList<SMS>();

        SQLiteDatabase db = getReadableDatabase();
        if (db == null) {
            Exception e = new Exception("Couldn't get readable database!");
            ACRA.getErrorReporter().handleException(e);
            return null;
        }
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SMS, null);
        SMS sms;

        if (cursor.moveToFirst()) {
            do {
                sms = new SMS();
                sms.setId(cursor.getInt(0));
                sms.setPhone(cursor.getString(1));
                sms.setBody(cursor.getString(2));
                sms.setStatus(cursor.getInt(3));
                sms.setSMSId(cursor.getInt(4));
                sms.setSynced(cursor.getInt(5));
                sms.setCreatedAt(cursor.getInt(6));
                sms_list.add(sms);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sms_list;
    }

    public Integer getLastPendingSMSId(){
        SQLiteDatabase db = getReadableDatabase();
        if (db == null) {
            Exception e = new Exception("Couldn't get readable database!");
            ACRA.getErrorReporter().handleException(e);
            return null;
        }
        Cursor cursor = db.query(
            TABLE_SMS,
            new String[]{FIELD_SMS_ID},
            FIELD_STATUS + "=? AND " + FIELD_SYNCED + "!=?",
            new String[] { String.valueOf(SMS.STATUS_TO_SEND), "1" },
            null,
            null,
            FIELD_CREATED_AT + " DESC, " + FIELD_SMS_ID + " DESC",
            "0,1"  // <offset>,<limit>
        );

        Integer last_id = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            last_id = cursor.getInt(0);
            cursor.close();
        }

        db.close();

        return last_id;
    }
}
