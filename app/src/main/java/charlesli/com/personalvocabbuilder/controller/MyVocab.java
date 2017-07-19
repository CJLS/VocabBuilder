package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;
import charlesli.com.personalvocabbuilder.ui.AddVocabDialog;
import charlesli.com.personalvocabbuilder.ui.CopyVocabDialog;
import charlesli.com.personalvocabbuilder.ui.EditVocabDialog;
import charlesli.com.personalvocabbuilder.ui.SortVocabDialog;
import charlesli.com.personalvocabbuilder.ui.SpeechSettingsDialog;

import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.DATE_ASC;


public class MyVocab extends AppCompatActivity {

    public static String searchPattern = "";
    private VocabCursorAdapter mVocabAdapter;
    private VocabDbHelper mDbHelper = VocabDbHelper.getDBHelper(MyVocab.this);
    private String categoryName;
    private FloatingActionButton addVocabFAB;
    private CustomTTS textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_vocab);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        categoryName = intent.getStringExtra("Category");
        setTitle(categoryName);

        textToSpeech = new CustomTTS(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    String selectedLocaleDisplayName = mDbHelper.getCategoryLocaleDisplayName(categoryName);
                    HashMap<String, Locale> displayNameToLocaleMapping = textToSpeech.getSupportedDisplayNameToLocaleMapping();

                    int result = textToSpeech.setLanguage(displayNameToLocaleMapping.get(selectedLocaleDisplayName));
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MyVocab.this, "Please enable internet to download the selected language voice data", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String s) {

                            }

                            @Override
                            public void onDone(String s) {

                            }

                            @Override
                            public void onError(String s) {
                                Toast.makeText(MyVocab.this, "Language voice data might not be downloaded yet. Please enable internet when a new language is selected", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }, "com.google.android.tts");


        ListView mVocabListView = (ListView) findViewById(R.id.mVocabList);
        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mVocabListView.setEmptyView(emptyTextView);

        SharedPreferences sharedPreferencesSortOrder = getSharedPreferences(getString(R.string.sharedPrefSortFile), MODE_PRIVATE);
        String orderBy = sharedPreferencesSortOrder.getString(categoryName, DATE_ASC);

        Cursor cursor = mDbHelper.getVocabCursor(categoryName, orderBy);
        mVocabAdapter = new VocabCursorAdapter(this, cursor, textToSpeech);
        mVocabListView.setAdapter(mVocabAdapter);
        mVocabListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedVocab = (String) ((TextView) view.findViewById(R.id.vocabName)).getText();
                String selectedDefinition = (String) ((TextView) view.findViewById(R.id.vocabDefinition)).getText();
                editVocabAlertDialog(selectedVocab, selectedDefinition, id, mDbHelper,
                        categoryName, mVocabAdapter);
                return true;
            }
        });

        addVocabFAB = (FloatingActionButton) findViewById(R.id.vocabFAB);
        if (addVocabFAB != null) {
            addVocabFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addVocabAlertDialog(mDbHelper, categoryName, mVocabAdapter);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_vocab, menu);

        implementSearchBar(menu, R.id.search_my_vocab_button);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.del_my_vocab_button) {
            deleteVocab();
        }
        else if (id == R.id.label_my_vocab_button) {
            selectTableToAddVocabTo();
        }
        else if (id == R.id.select_all_my_vocab_button) {
            selectAll();
        }
        else if (id == R.id.sort_my_vocab_button) {
            sortVocab();
        }
        else if (id == R.id.speaker_my_vocab_button) {
            setSpeakerSettings();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setSpeakerSettings() {
        if (textToSpeech != null) {
            SpeechSettingsDialog speechSettingsDialog = new SpeechSettingsDialog(this, categoryName, textToSpeech);
            speechSettingsDialog.show();
            speechSettingsDialog.changeButtonsToAppIconColor();
        }
        else {
            Toast.makeText(this, "Sorry, the speech engine is currently unavailable. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sortVocab() {
        SortVocabDialog dialog = new SortVocabDialog(this, categoryName, mDbHelper, mVocabAdapter);
        dialog.show();
    }

    private void selectAll() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedPrefSortFile), MODE_PRIVATE);
        String orderBy = sharedPreferences.getString(categoryName, DATE_ASC);

        Cursor cursor = mDbHelper.getVocabCursor(categoryName, orderBy);
        int numOfRows = cursor.getCount();
        for (int i = 0; i < numOfRows; i++) {
            mVocabAdapter.selectedItemsPositions.add(i);
        }
        mVocabAdapter.changeCursor(cursor);
    }

    private void deleteVocab() {
        Iterator<Integer> posIt = mVocabAdapter.selectedItemsPositions.iterator();
        if (mVocabAdapter.selectedItemsPositions.isEmpty()) {
            Toast.makeText(this, "No words are selected", Toast.LENGTH_SHORT).show();
        }
        else {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            while (posIt.hasNext()) {
                Integer posInt = posIt.next();
                String selection = VocabDbContract._ID + " LIKE ?" + " AND " +
                        VocabDbContract.COLUMN_NAME_CATEGORY + " LIKE ?";
                String[] selectionArgs = {String.valueOf(mVocabAdapter.getItemId(posInt)), categoryName};
                db.delete(VocabDbContract.TABLE_NAME_MY_VOCAB, selection, selectionArgs);
            }
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedPrefSortFile), MODE_PRIVATE);
            String orderBy = sharedPreferences.getString(categoryName, DATE_ASC);

            Cursor cursor = mDbHelper.getVocabCursor(categoryName, orderBy);
            mVocabAdapter.changeCursor(cursor);

            mVocabAdapter.selectedItemsPositions.clear();
        }
    }

    private void selectTableToAddVocabTo() {
        if (mVocabAdapter.selectedItemsPositions.isEmpty()) {
            Toast.makeText(this, "No words are selected", Toast.LENGTH_SHORT).show();
            return;
        }
        CopyVocabDialog dialog = new CopyVocabDialog(this, mDbHelper, mVocabAdapter, categoryName);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void addVocabAlertDialog(final VocabDbHelper dbHelper, final String category,
                                       final VocabCursorAdapter cursorAdapter) {
        AddVocabDialog dialog = new AddVocabDialog(this, dbHelper, cursorAdapter, category);
        dialog.show();
        dialog.setUpTranslationButtonAfterShowDialog();
        dialog.changeButtonsToAppIconColor();
    }

    private void editVocabAlertDialog(final String selectedVocab, final String selectedDefinition,
                                        final long id, final VocabDbHelper dbHelper,
                                        final String category, final VocabCursorAdapter cursorAdapter) {
        EditVocabDialog dialog = new EditVocabDialog(this, selectedVocab, selectedDefinition,
                id, dbHelper, category, cursorAdapter);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void implementSearchBar(Menu menu, int menuItemId) {
        final MenuItem search = menu.findItem(menuItemId);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchPattern = s;
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedPrefSortFile), MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(categoryName, DATE_ASC);
                Cursor cursor = mDbHelper.getVocabCursorWithStringPattern(categoryName, s, orderBy);
                mVocabAdapter.changeCursor(cursor);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchPattern = s;
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedPrefSortFile), MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(categoryName, DATE_ASC);
                Cursor cursor = mDbHelper.getVocabCursorWithStringPattern(categoryName, s, orderBy);
                mVocabAdapter.changeCursor(cursor);
                return true;
            }
        });

        // Detect when search bar collapses
        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                addVocabFAB.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                addVocabFAB.setVisibility(View.VISIBLE);
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedPrefSortFile), MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(categoryName, DATE_ASC);
                mVocabAdapter.changeCursor(mDbHelper.getVocabCursor(categoryName, orderBy));
            }
        });
    }

}
