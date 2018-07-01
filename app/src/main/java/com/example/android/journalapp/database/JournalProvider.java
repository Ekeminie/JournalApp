package com.example.android.journalapp.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.provider.BaseColumns._ID;
import static com.example.android.journalapp.database.JournalContract.CONTENT_AUTHORITY;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.CONTENT_ITEM_TYPE;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.CONTENT_LIST_TYPE;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_CONTENT;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_DATE;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_TITLE;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.TABLE_NAME;
import static com.example.android.journalapp.database.JournalContract.PATH_JOURNAL;

/**
 * Created by Delight on 30/06/2018.
 */

public class JournalProvider extends ContentProvider {

    private static final int JOURNAL  = 100;
    private static final int JOURNAL_ID = 101;

    /** Tag for the log messages */
    public static final String LOG_TAG = JournalProvider.class.getSimpleName();


    private JournalDbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_JOURNAL, JOURNAL);


        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_JOURNAL + "/#", JOURNAL_ID);

    }

    @Override
    public boolean onCreate() {
        //initializzing the JournalDbHelper to ensure that we have access to the database in onCreate
        mDbHelper = new JournalDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case JOURNAL:
                // For the journal code, query the journal table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the journal table. String.valueOf(ContentUris.parseId(uri))


                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case JOURNAL_ID:
                // For the Journal code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.journalapp/journal/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the journal table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case JOURNAL:
                return insertJournal(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }


    }

    private Uri insertJournal(Uri uri, ContentValues contentValues) {
        // Check that the name is not null
        String title = contentValues.getAsString(JOURNAL_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Note requires a title");
        }

        String content = contentValues.getAsString(JOURNAL_CONTENT);
        if (content == null) {
            throw new IllegalArgumentException("Note requires content");
        }

        String date = contentValues.getAsString(JOURNAL_CONTENT);
        if (date == null) {
            throw new IllegalArgumentException("Note requires a date");
        }



        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new note with the given values
        long id = database.insert(TABLE_NAME, null, contentValues);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;}

        // Notify all listeners that the data has changed for the note content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case JOURNAL:
                return CONTENT_LIST_TYPE;
            case JOURNAL_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }

    }


    /**
     * Insert new data into the provider with the given ContentValues.
     */
    /**
     * Insert a note into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_NAME);

        Cursor cursor = builder.query(mDbHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case JOURNAL:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case JOURNAL_ID:
                // Delete a single row given by the ID in the URI
                selection = _ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }


    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case JOURNAL:
                return updateNote(uri, contentValues, selection, selectionArgs);
            case JOURNAL_ID:
                // For the JOURNAL_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = _ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateNote(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update a note in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more notes).
     * Return the number of rows that were successfully updated.
     */
    private int updateNote(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link JOURNAL_TITLE} key is present,
        // check that the title value is not null.
        if (values.containsKey(JOURNAL_TITLE)) {
            String name = values.getAsString(JOURNAL_TITLE);
            if (name == null) {
                throw new IllegalArgumentException("Note requires a title");
            }
        }

        // If the {@link JOURNAL_CONTENT} key is present,
        // check that the content is not empty.
        if (values.containsKey(JOURNAL_CONTENT)) {
            String content = values.getAsString(JOURNAL_CONTENT);
            if (content== null) {
                throw new IllegalArgumentException("Note requires a content");
            }
        }

        // If the {@link JOURNAL_DATE} key is present,
        if (values.containsKey(JOURNAL_DATE)) {
            // Check that the date is not empty
            String date = values.getAsString(JOURNAL_DATE);
            if (date == null) {
                throw new IllegalArgumentException("Note requires valid date");
            }
        }


        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

}
