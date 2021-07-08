package com.example.proyecto_foni;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DataBase extends SQLiteOpenHelper {

    private Context context;
    public static final String DATABASE_NAME = "saved_recordings.db";
    private static final int DATABASE_VERSION= 1;
    private static final String TABLE_NAME = "saved_recording_table";

    private static OnDatabaseChangedListener mOnDatabaseChangedListener;

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PATH = "path";
    public static final String COLUMN_LENGTH = "length";
    public static final String COLUMN_TIME_ADDED = "time_added";

    public static final String COMA_SEP = ",";

    public static final String SQLITE_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + "id INTEGER PRIMARY KEY" +
            " AUTOINCREMENT" + COMA_SEP +
            COLUMN_NAME + " TEXT" + COMA_SEP +
            COLUMN_PATH + " TEXT" + COMA_SEP +
            COLUMN_LENGTH + " INTEGER" + COMA_SEP +
            COLUMN_TIME_ADDED + " INTEGER" + ")";

    public DataBase (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLITE_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public boolean addRecording(RecordingAudio recordingAudio) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_NAME, recordingAudio.getName());
            contentValues.put(COLUMN_PATH, recordingAudio.getPath());
            contentValues.put(COLUMN_LENGTH, recordingAudio.getLength());
            contentValues.put(COLUMN_TIME_ADDED, recordingAudio.getTime_added());

            db.insert(TABLE_NAME, null, contentValues);
            if(mOnDatabaseChangedListener != null){
                mOnDatabaseChangedListener.onNewDatabaseEntryAdded(recordingAudio);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteRecording(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME+"", "name=?",new String[]{name+""});

    }

    public ArrayList<RecordingAudio> getAllAudios() {
        ArrayList<RecordingAudio> arrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(1);
                String path = cursor.getString(2);
                int length = (int) cursor.getLong(3);
                long timeAdder = cursor.getLong(4);

                RecordingAudio recordingAudio = new RecordingAudio(name, path, length, timeAdder);
                arrayList.add(recordingAudio);
            }
            cursor.close();
            return arrayList;
        } else {
            return null;
        }
    }

    public static void setOnDatabaseChangedListener(OnDatabaseChangedListener listener){
        mOnDatabaseChangedListener = listener;
    }
}
