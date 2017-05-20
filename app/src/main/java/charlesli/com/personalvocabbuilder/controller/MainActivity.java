package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;
import charlesli.com.personalvocabbuilder.ui.AddCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.EditCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.ModifyMyWordBankCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.ReviewDialog;
import charlesli.com.personalvocabbuilder.ui.TranslationSettingsDialog;


public class MainActivity extends AppCompatActivity {

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
                editCategoryAlertDialog(categoryName, categoryDesc);
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
            Intent intent = new Intent(this, ExportActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.import_button) {
            Intent intent = new Intent(this, ImportActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTranslationLanguage() {
        TranslationSettingsDialog dialog = new TranslationSettingsDialog(this);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void addCategory() {
        AddCategoryDialog dialog = new AddCategoryDialog(this, mCategoryAdapter);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void selectReviewType() {
        ReviewDialog dialog = new ReviewDialog(this);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void editCategoryAlertDialog(final String selectedCategory, final String selectedDesc) {
        if (selectedCategory.equals("My Word Bank")) {
            ModifyMyWordBankCategoryDialog dialog = new ModifyMyWordBankCategoryDialog(this);
            dialog.show();
            dialog.changeButtonsToAppIconColor();
            return;
        }

        EditCategoryDialog dialog = new EditCategoryDialog(this, mCategoryAdapter, selectedCategory, selectedDesc);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

}
