package com.example.android.journalapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.journalapp.database.JournalDbHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.provider.BaseColumns._ID;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.CONTENT_URI;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_CONTENT;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_DATE;
import static com.example.android.journalapp.database.JournalContract.JournalEntry.JOURNAL_TITLE;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    // Identifier for the pet data loader
    private static final int EXISTING_NOTE_LOADER = 0;

    private Uri mCurrentNoteUri;


    private JournalDbHelper mDbHelper;

    /** EditText field to enter the note's name */
    private EditText mNameEditText;

    /** EditText field to enter the note's breed */
    private EditText mContentEditText;

    private boolean mNoteHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent){
            mNoteHasChanged = true;
            return  false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity
        // in order to figure otu if we're creating a new pet or editing an existing one
        Intent intent = getIntent();
        mCurrentNoteUri = intent.getData();

        // If the intent DOES NOT contain a pet content URI, then we know that we are
        // creating a new pet
        if (mCurrentNoteUri == null){
            // This is a new pet, so change the app bar to say "Add a pet"
            setTitle(getString(R.string.editor_activity_title_new_note));

            //Invalidate the options menu, so the "Delete menu option can be hidden.
            // It doesn't make sense to delete a pet that hasn't been created yet. }
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle(getString(R.string.editor_activity_title_edit_note));

            //Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_NOTE_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.title);
        mContentEditText = (EditText) findViewById(R.id.content);

        mNameEditText.setOnTouchListener(mTouchListener);
        mContentEditText.setOnTouchListener(mTouchListener);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }


    /*
* This method is called after invalidateOptionsMenu(), so that the
* menu can be updated ( some menu items can be hidden or made visible)
* */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentNoteUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveNote();
                // Do nothing for now
                //save pet when clicked on and return to main activity

                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
//                // Navigate back to parent activity (CatalogActivity)
//                NavUtils.navigateUpFromSameTask(this);
                //If the pet hasn't changed, continue with navigating up to a parent activity
                // which is the {@link CatalogActivity}
                if (!mNoteHasChanged){
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                //Otherwise if there are unsaved changes, set up a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i){
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    private void showDeleteConfirmationDialog(){
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                // User clicked the "Delete button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                //User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        // Create and show the ALertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showUnsavedChangesDialog
            (DialogInterface.OnClickListener discardButtonClickListener){
        //Create an ALertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                // USER clicked the "Keep Editing" button, so dismiss this dialog
                // and continue editing the pet.
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

    }
    public String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy/MM/dd");
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }

    private void saveNote() {
        //Read from input fields
        // Use trim to eliminate leading or trailing white space
        String titleString = mNameEditText.getText().toString().trim();
        String contentString = mContentEditText.getText().toString().trim();
        String date = getCurrentDate();




        // Check if this is supposed to be a new note
        // and check if all the editor are blank
        if (mCurrentNoteUri == null && TextUtils.isEmpty(titleString) && TextUtils.isEmpty(contentString)){
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        //Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(JOURNAL_TITLE, titleString);
        values.put(JOURNAL_CONTENT, contentString);
        values.put(JOURNAL_DATE, date);


        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
        if (mCurrentNoteUri == null){
            //This is a NEW pet, so insert a new pet into the provider.
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(CONTENT_URI, values);

            //Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.editor_insert_note_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_note_successful) + newUri, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify
            int rowsAffected = getContentResolver().update(mCurrentNoteUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0){
                // if no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast
                Toast.makeText(this, getString(R.string.editor_update_note_successful), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void deletePet(){
        // Only perform the delete if this is an existing pet.
        if(mCurrentNoteUri != null){
            // Call the ContentResolver to delete the pet at the given content URI
            // Pass in null for the selection adn selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want
            int rowsDeleted = getContentResolver().delete(mCurrentNoteUri, null, null);

            // Show a toast message depending on whether or not the delete was successful
            if (rowsDeleted == 0){
                // if no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_note_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_note_sucessful), Toast.LENGTH_SHORT).show();
            }
        }

        // CLose the activity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns form the pet table
        String[] projection = {
          _ID,
        JOURNAL_TITLE,
        JOURNAL_CONTENT,
         JOURNAL_DATE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, // Parent activity context
                mCurrentNoteUri,       // Query the content URI for the current pet
                projection,            // Columns to include in the resulting Cursor
                null,                   //No selection clause
                null,                   //No selection arguments
                null);                  //Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1){
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if(cursor.moveToFirst()){
            //Find the columns of pet attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(JOURNAL_TITLE);

            int contentColumnIndex = cursor.getColumnIndex(JOURNAL_CONTENT);
           // int dateColumnIndex = cursor.getColumnIndex(JOURNAL_DATE);

            // eXTRACT OUT THE VALUE FROM THE cURSOR for the given column index
            String title = cursor.getString(titleColumnIndex);
            String content = cursor.getString(contentColumnIndex);
            //int date = cursor.getInt(dateColumnIndex);

            //Update the views on the screen with the values from the database
            mNameEditText.setText(title);
            mContentEditText.setText(content);

            }
        }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mContentEditText.setText("");
    }

















}
