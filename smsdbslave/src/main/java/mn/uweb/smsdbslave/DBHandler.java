package mn.uweb.smsdbslave;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String
            DB_NAME = "uweb",
            TABLE_NAME = "sms",
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
            "CREATE TABLE " + TABLE_NAME + " (" +
                FIELD_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FIELD_PHONE      + " TEXT, " +
                FIELD_BODY       + " TEXT, " +
                FIELD_STATUS     + " INTEGER, " +
                FIELD_SMS_ID     + " INTEGER, " +
                FIELD_SYNCED     + " INTEGER, " +
                FIELD_CREATED_AT + " INTEGER" +
            ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String backup_table = String.valueOf(System.currentTimeMillis());

        db.execSQL("ALTER TABLE " + TABLE_NAME + " RENAME TO " + backup_table);
        // TODO ability to list sms from old tables

        onCreate(db);
    }

    public void insertSMS(SMS sms) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(FIELD_PHONE, sms.getPhone());
        values.put(FIELD_BODY, sms.getBody());
        values.put(FIELD_STATUS, sms.getStatus());
        if (sms.getSMSId() != null) {
            values.put(FIELD_SMS_ID, sms.getSMSId());
        }
        values.put(FIELD_SYNCED, sms.getBody());
        values.put(FIELD_CREATED_AT, sms.getBody());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    /*
    public SMS getSMS(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE,
                new String[] { ID, PHONE, BODY },
                ID + "=?",
                new String[] { String.valueOf(id) },
                null, null, null, null
        );

        if (cursor != null)
            cursor.moveToFirst();

        SMS sms = new SMS(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2));
        db.close();
        cursor.close();
        return sms;
    }
    */

    /*
    public void deleteContact(SMS sms) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, ID + "=?", new String[] { String.valueOf(sms.getId()) });
        db.close();
    }
    */

    public int countSMS() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
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

    public List<SMS> getAllSMS() {
        List<SMS> sms_list = new ArrayList<SMS>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        SMS sms;

        if (cursor.moveToFirst()) {
            do {
                sms = new SMS();
                sms.setId(Integer.parseInt(cursor.getString(0)));
                sms.setPhone(cursor.getString(1));
                sms.setBody(cursor.getString(2));
                sms.setStatus(Integer.parseInt(cursor.getString(3)));
                sms.setSMSId(Integer.parseInt(cursor.getString(4)));
                sms.setSynced(Integer.parseInt(cursor.getString(5)));
                sms.setCreatedAt(Integer.parseInt(cursor.getString(6)));
                sms_list.add(sms);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sms_list;
    }
}
