package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;
import charlesli.com.personalvocabbuilder.ui.AddCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.EditCategoryDialog;
import charlesli.com.personalvocabbuilder.ui.ExportDialog;
import charlesli.com.personalvocabbuilder.ui.ImportDialog;
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
            selectCategoriesToExport();
        }
        else if (id == R.id.import_button) {
            //selectExportFileToImport();
            Intent intent = new Intent(this, ImportActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectExportFileToImport() {
        ImportDialog dialog = new ImportDialog(this);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
    }

    private void selectCategoriesToExport() {
        ExportDialog dialog = new ExportDialog(this);
        dialog.show();
        dialog.changeButtonsToAppIconColor();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ExportUtils.exportCategory(this);
                } else {
                    Toast.makeText(this, R.string.externalStoragePermissionDenied, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImportDialog.GET_FILE_RESULT_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            readExportFile(uri);
        }
    }

    public void readExportFile(Uri uri) {
        /*TODO:
            1. Make sure export with large number of rows doesn't clog main thread
                - make it an async task operation
            2. Present decisions on which categories to import to
                - a. original categories (for use in second device)
                - b. all in one new category
                - c. each category in export file is specified a new category location
            3. Test when no permission initially
        */
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line;
            int count = 0;
            // Skip first header line
            br.readLine();
            while ((line = br.readLine()) != null) {
                Log.d("Test4:", line);
                //"Vocab,Definition,Level,Category Name,Category Description"
                // 1. Split line by , that's not preceded by /
                String[] row = line.split("(?<!\\\\),");
                if (row.length < 4) {
                    continue;
                }
                // 2. Get each item from array
                String vocab = row[0];
                String definition = row[1];
                String progress = row[2];
                String categoryName = row[3];
                String categoryDescription = "";
                if (row.length > 4) {
                    categoryDescription = row[4];
                }

                Log.d("Test: ", "Vocab: " + vocab + ", Definition: " + definition
                        + ", Progress: " + progress + ", Category Name: " + categoryName
                        + ", Category Description: " + categoryDescription);
                // 3. Convert each item from /, to ,
                vocab = vocab.replace("\\,", ",");
                definition = definition.replace("\\,", ",");
                int level;
                switch (progress) {
                    case "Difficult":
                        level = ReviewSession.DIFFICULT;
                        break;
                    case "Familiar":
                        level = ReviewSession.FAMILIAR;
                        break;
                    case "Easy":
                        level = ReviewSession.EASY;
                        break;
                    case "Perfect":
                        level = ReviewSession.PERFECT;
                        break;
                    default:
                        level = ReviewSession.DIFFICULT;
                        break;
                }
                categoryName = categoryName.replace("\\,", ",");
                categoryDescription = categoryDescription.replace("\\,", ",");

                // 4. Insert into category and mvvocab db
                VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(this);
                if (!dbHelper.checkIfCategoryExists(categoryName)) {
                    dbHelper.insertCategory("TEMPORARY", categoryDescription);
                }
                dbHelper.insertVocab("TEMPORARY", vocab, definition, level);

                count++;
                /*
                if (count > 10) {
                    //break;
                }
                */
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
