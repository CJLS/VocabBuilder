package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.ExportCursorAdaptor;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;
import charlesli.com.personalvocabbuilder.ui.AddCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.EditCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.ExportCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.ModifyMyWordBankCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.ReviewDialog;
import charlesli.com.personalvocabbuilder.ui.TranslationSettingsDialog;

import static charlesli.com.personalvocabbuilder.controller.ExportUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private CategoryCursorAdapter mCategoryAdapter;
    private VocabDbHelper mDbHelper = VocabDbHelper.getDBHelper(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ListView mCategoryListView = (ListView) findViewById(R.id.mainListView);
        Cursor cursor = mDbHelper.getCategoryCursor();
        mCategoryAdapter = new CategoryCursorAdapter(this, cursor, 0);
        mCategoryListView.setAdapter(mCategoryAdapter);

        mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor categoryCursor = mDbHelper.getCategoryCursor();
                categoryCursor.moveToPosition(position);
                String categoryName = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
                Intent intent = new Intent(MainActivity.this, MyVocab.class);
                intent.putExtra("Category", categoryName);
                startActivity(intent);
            }
        });

        mCategoryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor categoryCursor = mDbHelper.getCategoryCursor();
                categoryCursor.moveToPosition(position);
                String categoryName = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
                String categoryDesc = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_DESCRIPTION));
                editCategoryAlertDialog(categoryName, categoryDesc, mDbHelper, mCategoryAdapter);
                return true;
            }
        });

        FloatingActionButton reviewFAB = (FloatingActionButton) findViewById(R.id.categoryFAB);
        if (reviewFAB != null) {
            reviewFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectReviewType();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCategoryAdapter.changeCursor(mDbHelper.getCategoryCursor());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_category_button) {
            addCategory();
        }
        else if (id == R.id.settings_button) {
            setTranslationLanguage();
        }
        else if (id == R.id.export_button) {
            selectCategoriesToExport();
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectCategoriesToExport() {
        ExportCategoryDialog dialog = new ExportCategoryDialog(this, mDbHelper);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void setTranslationLanguage() {
        TranslationSettingsDialog dialog = new TranslationSettingsDialog(this);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void addCategory() {
        AddCategoryDialog dialog = new AddCategoryDialog(this, mDbHelper, mCategoryAdapter);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void selectReviewType() {
        ReviewDialog dialog = new ReviewDialog(this, mDbHelper);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void editCategoryAlertDialog(final String selectedCategory, final String selectedDesc,
                                         final VocabDbHelper dbHelper, final CategoryCursorAdapter cursorAdapter) {
        if (selectedCategory.equals("My Word Bank")) {
            ModifyMyWordBankCategoryDialog dialog = new ModifyMyWordBankCategoryDialog(this);
            dialog.show();
            dialog.changeButtonsToAppIconColor();
            return;
        }

        EditCategoryDialog dialog = new EditCategoryDialog(this, dbHelper, cursorAdapter, selectedCategory, selectedDesc);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Cursor categoryCursor = mDbHelper.getCategoryCursor();
                    ExportCursorAdaptor mCursorAdapter = new ExportCursorAdaptor(this, categoryCursor, 0);
                    ExportUtils.exportCategory(this, mDbHelper, mCursorAdapter);
                } else {
                    Toast.makeText(this, "No external storage permission to export categories.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
