package com.example.android.journalapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.journalapp.database.JournalCursorAdapter;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.provider.BaseColumns._ID;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.CONTENT_URI;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_CONTENT;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_DATE;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_TITLE;
import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

  //  Adapter for the ListView
    JournalCursorAdapter mCursorAdapter;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private static final int JOURNAL_LOADER = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth= FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    return;
                }else{
                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                    .setProviders(AuthUI.GOOGLE_PROVIDER)
                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        //Find the ListView which will be populated with the pet data
        ListView noteListView = findViewById(R.id.list);

        //Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        noteListView.setEmptyView(emptyView);

        //Set up an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes ) so pass in null for the Cursor.
        mCursorAdapter = new JournalCursorAdapter(this, null);
        noteListView.setAdapter(mCursorAdapter);

        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" ( passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}
                // For example, the URI would be "content://com.example.android.pets/pets/2"
                // if the pet with ID 2 was clicked on.
                Uri currentNoteUri =  ContentUris.withAppendedId(CONTENT_URI, id);

                //Set the URI on the data field of the intent
                intent.setData(currentNoteUri);

                //Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });

        // Kick off the Loader
        getLoaderManager().initLoader(JOURNAL_LOADER, null, this);
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle){
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {_ID,
                JOURNAL_TITLE,
                JOURNAL_CONTENT,
        JOURNAL_DATE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                CONTENT_URI,    // Provider content URI to query
                projection,     // Columns to include in the resulting Cursor
                null,           // No selection clause
                null,           // No selection arguments
                null);           // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,Cursor data) {
        // Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.catalog_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.delete_all:
                deleteAllNotes();
                return true;

            case R.id.sign_out:
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    // Helper method to delete all pets in the database
    private void deleteAllNotes(){
        int rowsDeleted = getContentResolver().delete(CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from note database");

    }













}
