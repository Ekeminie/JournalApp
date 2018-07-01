package com.example.android.journalapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_CONTENT;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_DATE;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_TITLE;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.TABLE_NAME;

/**
 * Created by Delight on 30/06/2018.
 */

public class JournalDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "journal.db";

    private static final int DATABASE_VERSION = 1;


    public JournalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_JOURNAL_TABLE = "CREATE TABLE "
                + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + JOURNAL_TITLE + " TEXT NOT NULL, "
                + JOURNAL_CONTENT + " TEXT NOT NULL, "
                + JOURNAL_DATE + " TEXT NOT NULL "
                + "); ";
        sqLiteDatabase.execSQL(SQL_CREATE_JOURNAL_TABLE);


    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
