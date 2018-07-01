package com.example.android.journalapp.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Delight on 30/06/2018.
 */

public class JournalContract {
    public JournalContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.journalapp";
    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /*Possible path (appended to base content URI for possible URI's)
    * For instance, content://com.android.journalapp/journal/ is a valid path for
    * looking at journal data. content://com.android.journallapp/staff/ will fail.
    * as the ContentProvider hasn't been given any information on what to do with staff
    * */


    public static final String PATH_JOURNAL = "journal";

    public final static class JournalEntry implements BaseColumns {

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_JOURNAL;

        /**
         + * The MIME type of the {@link #CONTENT_URI} for a single journal item.
         + */

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_JOURNAL;


        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_JOURNAL);


        public static final String TABLE_NAME = "journal";
        public static final String JOURNAL_TITLE = "title";
        public static final String JOURNAL_CONTENT = "content";
        public static final String JOURNAL_DATE = "date";

    }
}



