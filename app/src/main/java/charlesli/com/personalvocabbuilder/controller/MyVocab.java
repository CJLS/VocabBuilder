package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

import java.util.Iterator;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;
import charlesli.com.personalvocabbuilder.ui.AddVocabDialog;
import charlesli.com.personalvocabbuilder.ui.CopyVocabDialog;
import charlesli.com.personalvocabbuilder.ui.EditVocabDialog;
import charlesli.com.personalvocabbuilder.ui.SortVocabDialog;

import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.DATE_ASC;


public class MyVocab extends AppCompatActivity {
    // Move some common alert dialog variables to parent class
    private VocabCursorAdapter mVocabAdapter;
    private VocabDbHelper mDbHelper = VocabDbHelper.getDBHelper(MyVocab.this);
    private String categoryName;
    private FloatingActionButton addVocabFAB;

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
        categoryName = intent.getStringExtra("Category");
        setTitle(categoryName);

        ListView mVocabListView = (ListView) findViewById(R.id.mVocabList);
        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mVocabListView.setEmptyView(emptyTextView);

        SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
        String orderBy = sharedPreferences.getString(categoryName, DATE_ASC);

        Cursor cursor = mDbHelper.getVocabCursor(categoryName, orderBy);
        mVocabAdapter = new VocabCursorAdapter(this, cursor, 0);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_vocab, menu);

        implementSearchBar(menu, R.id.search_my_vocab_button, categoryName,
                mVocabAdapter, mDbHelper);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.del_my_vocab_button) {
            deleteVocab(mDbHelper, categoryName, mVocabAdapter);
        }
        else if (id == R.id.label_my_vocab_button) {
            selectTableToAddVocabTo(mVocabAdapter, mDbHelper, categoryName);
        }
        else if (id == R.id.select_all_my_vocab_button) {
            selectAll(mVocabAdapter, mDbHelper, categoryName);
        }
        else if (id == R.id.sort_my_vocab_button) {
            sortVocab();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortVocab() {
        SortVocabDialog dialog = new SortVocabDialog(this, categoryName, mDbHelper, mVocabAdapter);
        dialog.show();
    }

    private void selectAll(VocabCursorAdapter cursorAdapter, VocabDbHelper dbHelper,
                             String category) {
        SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
        String orderBy = sharedPreferences.getString(categoryName, DATE_ASC);

        Cursor cursor = dbHelper.getVocabCursor(category, orderBy);
        int numOfRows = cursor.getCount();
        for (int i = 0; i < numOfRows; i++) {
            cursorAdapter.selectedItemsPositions.add(i);
        }
        cursorAdapter.changeCursor(cursor);
    }

    private void deleteVocab(VocabDbHelper dbHelper, String category, VocabCursorAdapter cursorAdapter) {
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

    private void selectTableToAddVocabTo(final VocabCursorAdapter cursorAdapter, final VocabDbHelper dbHelper,
                                           final String fromCategory) {
        if (cursorAdapter.selectedItemsPositions.isEmpty()) {
            Toast.makeText(this, "No words are selected", Toast.LENGTH_SHORT).show();
            return;
        }
        CopyVocabDialog dialog = new CopyVocabDialog(this, dbHelper, cursorAdapter, fromCategory);
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

    private void implementSearchBar(Menu menu, int menuItemId, final String category,
                                      final VocabCursorAdapter cursorAdapter, final VocabDbHelper dbHelper) {
        MenuItem search = menu.findItem(menuItemId);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(categoryName, DATE_ASC);
                Cursor cursor = dbHelper.getVocabCursorWithStringPattern(category, s, orderBy);
                cursorAdapter.changeCursor(cursor);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(categoryName, DATE_ASC);
                Cursor cursor = dbHelper.getVocabCursorWithStringPattern(category, s, orderBy);
                cursorAdapter.changeCursor(cursor);
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
                SharedPreferences sharedPreferences = getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(categoryName, DATE_ASC);
                cursorAdapter.changeCursor(dbHelper.getVocabCursor(category, orderBy));
            }
        });
    }

}
