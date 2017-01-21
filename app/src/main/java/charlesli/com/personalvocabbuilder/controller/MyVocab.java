package charlesli.com.personalvocabbuilder.controller;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;
import charlesli.com.personalvocabbuilder.ui.CopyVocabDialog;


public class MyVocab extends AppCompatActivity {

    private final String DATE_ASC = VocabDbContract._ID + " ASC";
    private final String DATE_DESC = VocabDbContract._ID + " DESC";
    private final String VOCAB_ASC = VocabDbContract.COLUMN_NAME_VOCAB + " COLLATE NOCASE ASC";
    private final String VOCAB_DESC = VocabDbContract.COLUMN_NAME_VOCAB + " COLLATE NOCASE DESC";
    private VocabCursorAdapter mVocabAdapter;
    private VocabDbHelper mDbHelper = VocabDbHelper.getDBHelper(MyVocab.this);
    private String mCategory;
    private FloatingActionButton fab;

    // Sort Radio Button
    private RadioButton rbDateAscending;
    private RadioButton rbDateDescending;
    private RadioButton rbVocabAscending;
    private RadioButton rbVocabDescending;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_vocab);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Get Category Information
        Intent intent = getIntent();
        mCategory = intent.getStringExtra("Category");
        setTitle(mCategory);

        ListView mVocabListView = (ListView) findViewById(R.id.mVocabList);
        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mVocabListView.setEmptyView(emptyTextView);

        SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
        String orderBy = sharedPreferences.getString(mCategory, DATE_ASC);

        Cursor cursor = mDbHelper.getVocabCursor(mCategory, orderBy);
        mVocabAdapter = new VocabCursorAdapter(this, cursor, 0);
        mVocabListView.setAdapter(mVocabAdapter);
        mVocabListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedVocab = (String) ((TextView) view.findViewById(R.id.vocabName)).getText();
                String selectedDefinition = (String) ((TextView) view.findViewById(R.id.vocabDefinition)).getText();
                editVocabAlertDialog(selectedVocab, selectedDefinition, id, mDbHelper,
                        mCategory, mVocabAdapter);
                return true;
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.vocabFAB);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addVocabAlertDialog(mDbHelper, mCategory, mVocabAdapter);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_vocab, menu);

        implementSearchBar(menu, R.id.search_my_vocab_button, mCategory,
                mVocabAdapter, mDbHelper);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.del_my_vocab_button) {
            deleteVocab(mDbHelper, mCategory, mVocabAdapter);
        }
        else if (id == R.id.label_my_vocab_button) {
            selectTableToAddVocabTo(mVocabAdapter, mDbHelper, mCategory);
        }
        else if (id == R.id.select_all_my_vocab_button) {
            selectAll(mVocabAdapter, mDbHelper, mCategory);
        }
        else if (id == R.id.sort_my_vocab_button) {
            sortVocab();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void sortVocab() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort By");

        LayoutInflater li = LayoutInflater.from(MyVocab.this);
        View promptsView = li.inflate(R.layout.alert_dialog_sort, null);
        builder.setView(promptsView);

        final AlertDialog dialog = builder.create();

        rbDateAscending = (RadioButton) promptsView.findViewById(R.id.btDateAscending);
        rbDateDescending = (RadioButton) promptsView.findViewById(R.id.btDateDescending);
        rbVocabAscending = (RadioButton) promptsView.findViewById(R.id.btVocabAscending);
        rbVocabDescending = (RadioButton) promptsView.findViewById(R.id.btVocabDescending);

        rbDateAscending.setChecked(false);
        rbDateDescending.setChecked(false);
        rbVocabAscending.setChecked(false);
        rbVocabDescending.setChecked(false);

        final SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
        String orderBy = sharedPreferences.getString(mCategory, DATE_ASC);

        if (orderBy.equals(DATE_ASC)) {
            rbDateAscending.setChecked(true);
        }
        else if (orderBy.equals(DATE_DESC)) {
            rbDateDescending.setChecked(true);
        }
        else if (orderBy.equals(VOCAB_ASC)) {
            rbVocabAscending.setChecked(true);
        }
        else if (orderBy.equals(VOCAB_DESC)) {
            rbVocabDescending.setChecked(true);
        }


        rbDateAscending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortByRadioButton(rbDateAscending, DATE_ASC, dialog);
            }
        });

        rbDateDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortByRadioButton(rbDateDescending, DATE_DESC, dialog);
            }
        });

        rbVocabAscending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortByRadioButton(rbVocabAscending, VOCAB_ASC, dialog);
            }
        });

        rbVocabDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortByRadioButton(rbVocabDescending, VOCAB_DESC, dialog);
            }
        });

        dialog.show();
    }

    private void setSortByRadioButton(RadioButton selectedButton, String orderBy, AlertDialog dialog) {
        rbDateAscending.setChecked(false);
        rbDateDescending.setChecked(false);
        rbVocabAscending.setChecked(false);
        rbVocabDescending.setChecked(false);

        selectedButton.setChecked(true);

        SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(mCategory, orderBy);
        editor.apply();

        Cursor cursor = mDbHelper.getVocabCursor(mCategory, orderBy);
        mVocabAdapter.changeCursor(cursor);
        dialog.dismiss();
    }

    protected void selectAll(VocabCursorAdapter cursorAdapter, VocabDbHelper dbHelper,
                             String category) {
        SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
        String orderBy = sharedPreferences.getString(mCategory, DATE_ASC);

        Cursor cursor = dbHelper.getVocabCursor(category, orderBy);
        int numOfRows = cursor.getCount();
        for (int i = 0; i < numOfRows; i++) {
            cursorAdapter.selectedItemsPositions.add(i);
        }
        cursorAdapter.changeCursor(cursor);
    }

    protected void deleteVocab(VocabDbHelper dbHelper, String category, VocabCursorAdapter cursorAdapter) {
        Iterator<Integer> posIt = cursorAdapter.selectedItemsPositions.iterator();
        if (cursorAdapter.selectedItemsPositions.isEmpty()) {
            Toast.makeText(this, "No words are selected", Toast.LENGTH_SHORT).show();
        }
        else {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            while (posIt.hasNext()) {
                Integer posInt = posIt.next();
                // Define 'where' part of query
                String selection = VocabDbContract._ID + " LIKE ?" + " AND " +
                        VocabDbContract.COLUMN_NAME_CATEGORY + " LIKE ?";
                // Specify arguments in placeholder order
                String[] selectionArgs = {String.valueOf(cursorAdapter.getItemId(posInt)), category};
                // Issue SQL statement
                db.delete(VocabDbContract.TABLE_NAME_MY_VOCAB, selection, selectionArgs);
            }
            SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
            String orderBy = sharedPreferences.getString(category, DATE_ASC);

            Cursor cursor = dbHelper.getVocabCursor(category, orderBy);
            cursorAdapter.changeCursor(cursor);

            cursorAdapter.selectedItemsPositions.clear();
        }
    }

    protected void selectTableToAddVocabTo(final VocabCursorAdapter cursorAdapter, final VocabDbHelper dbHelper,
                                           final String fromCategory) {
        if (cursorAdapter.selectedItemsPositions.isEmpty()) {
            Toast.makeText(this, "No words are selected", Toast.LENGTH_SHORT).show();
            return;
        }
        CopyVocabDialog dialog = new CopyVocabDialog(this, dbHelper, cursorAdapter, fromCategory);
        dialog.show();
        dialog.changeDialogButtonsColor();
    }

    protected void addVocabAlertDialog(final VocabDbHelper dbHelper, final String category,
                                       final VocabCursorAdapter cursorAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Vocab");

        LayoutInflater li = LayoutInflater.from(MyVocab.this);
        View promptsView = li.inflate(R.layout.alert_dialog_add_vocab, null);
        final EditText vocabInput = (EditText) promptsView.findViewById(R.id.vocabInput);
        final EditText definitionInput = (EditText) promptsView.findViewById(R.id.definitionInput);
        final ProgressBar progressBar = (ProgressBar) promptsView.findViewById(R.id.progressBar);
        builder.setView(promptsView);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String vocab = vocabInput.getText().toString();
                String definition = definitionInput.getText().toString();
                dbHelper.insertVocab(category, vocab, definition, 0);
                if (!category.equals(VocabDbContract.CATEGORY_NAME_MY_WORD_BANK)) {
                    dbHelper.insertVocab(VocabDbContract.CATEGORY_NAME_MY_WORD_BANK, vocab, definition, 0);
                }
                // Update Cursor
                SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(category, DATE_ASC);

                Cursor cursor = dbHelper.getVocabCursor(category, orderBy);
                cursorAdapter.changeCursor(cursor);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setNeutralButton("Translate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog dialog = builder.create();

        dialog.show();

        changeDialogButtonsColor(dialog);

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vocab = vocabInput.getText().toString();

                SharedPreferences sharedPreferences = getSharedPreferences("Translation", MODE_PRIVATE);
                int sourcePos = sharedPreferences.getInt("Source", 0); // 0 is for Detect Language
                int targetPos = sharedPreferences.getInt("Target", 19); // 19 is for English

                String source = LanguageOptions.FROM_LANGUAGE_CODE[sourcePos];
                String target = LanguageOptions.TO_LANGUAGE_CODE[targetPos];

                final AlertDialog.Builder builder = new AlertDialog.Builder(MyVocab.this);
                builder.setMessage("Network is unavailable. Please try again later.");
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();

                if (isNetworkAvailable()) {
                    String APIKey = getString(R.string.translateKey);
                    GoogleTranslate googleTranslate = new GoogleTranslate(progressBar, APIKey);
                    googleTranslate.setListener(new GoogleTranslate.Listener() {
                        @Override
                        public void onTaskResult(String string) {
                            if (string != null) {
                                JSONParser jsonParser = new JSONParser();
                                String translatedText = jsonParser.parseJSONForTranslation(string);
                                definitionInput.setText(Html.fromHtml(translatedText));
                            }
                            else {
                                Toast.makeText(MyVocab.this, "Sorry, the translation operation did not go through. Please try again.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    googleTranslate.execute(vocab, source, target);
                }
                else {
                    dialog.show();

                    changeDialogButtonsColor(dialog);
                }
            }
        });

    }

    protected void editVocabAlertDialog(final String selectedVocab, final String selectedDefinition,
                                        final long id, final VocabDbHelper dbHelper,
                                        final String category, final VocabCursorAdapter cursorAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Vocab");
        builder.setMessage(selectedVocab);
        // Set up the input
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText definitionInput = new EditText(this);
        definitionInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        definitionInput.setHint("New Definition");
        definitionInput.setText(selectedDefinition);
        layout.addView(definitionInput);

        builder.setView(layout);

        // Set up the buttons
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String definition = definitionInput.getText().toString();
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                // new value for one column
                ContentValues values = new ContentValues();
                values.put(VocabDbContract.COLUMN_NAME_DEFINITION, definition);

                // which row to update, based on the VOCAB
                String selectionMyVocab = VocabDbContract.COLUMN_NAME_VOCAB + " = ? AND " +
                        VocabDbContract.COLUMN_NAME_DEFINITION + " = ?";
                String[] selectionArgsMyVocab = {selectedVocab, selectedDefinition};

                db.update(
                        VocabDbContract.TABLE_NAME_MY_VOCAB,
                        values,
                        selectionMyVocab,
                        selectionArgsMyVocab
                );

                // Update Cursor
                SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(category, DATE_ASC);

                Cursor cursor = dbHelper.getVocabCursor(category, orderBy);
                cursorAdapter.changeCursor(cursor);
            }
        });
        builder.setNegativeButton("Delete Vocab", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete Vocab from Database*****************************************
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                // Define 'where' part of query
                String selection = VocabDbContract._ID + " LIKE ?";
                // Specify arguments in placeholder order
                String[] selectionArgs = {String.valueOf(id)};
                // Issue SQL statement
                db.delete(VocabDbContract.TABLE_NAME_MY_VOCAB, selection, selectionArgs);

                // Update Cursor
                SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(category, DATE_ASC);

                Cursor cursor = dbHelper.getVocabCursor(category, orderBy);
                cursorAdapter.changeCursor(cursor);
            }
        });

        final AlertDialog dialog = builder.create();

        dialog.show();

        changeDialogButtonsColor(dialog);

    }

    protected void implementSearchBar(Menu menu, int menuItemId, final String category,
                                      final VocabCursorAdapter cursorAdapter, final VocabDbHelper dbHelper) {
        MenuItem search = menu.findItem(menuItemId);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(mCategory, DATE_ASC);
                Cursor cursor = dbHelper.getVocabCursorWithStringPattern(category, s, orderBy);
                cursorAdapter.changeCursor(cursor);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(mCategory, DATE_ASC);
                Cursor cursor = dbHelper.getVocabCursorWithStringPattern(category, s, orderBy);
                cursorAdapter.changeCursor(cursor);
                return true;
            }
        });

        // Detect when search bar collapses
        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                fab.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                fab.setVisibility(View.VISIBLE);
                SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(mCategory, DATE_ASC);
                cursorAdapter.changeCursor(dbHelper.getVocabCursor(category, orderBy));
            }
        });
    }

    private void changeDialogButtonsColor(AlertDialog dialog) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.app_icon_color));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.app_icon_color));
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(this, R.color.app_icon_color));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

}
