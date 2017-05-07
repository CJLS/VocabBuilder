package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;
import charlesli.com.personalvocabbuilder.ui.ImportDialog;

public class ImportActivity extends AppCompatActivity {

    private int GET_FILE_RESULT_CODE = 1;
    private int IMPORT_OPTION_ORIGINAL_CATEGORIES = 0;
    private int IMPORT_OPTION_SPECIFIED_CATEGORY = 1;
    private int importOption = IMPORT_OPTION_ORIGINAL_CATEGORIES;
    private TextView exportFileName;
    private EditText importCategoryName;
    private CheckBox resetVocabProgress;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        exportFileName = (TextView) findViewById(R.id.exportFileName);
        importCategoryName = (EditText) findViewById(R.id.importCategory);
        resetVocabProgress = (CheckBox) findViewById(R.id.resetVocabProgress);
        Button selectFileButton = (Button) findViewById(R.id.selectFileButton);
        Button importButton = (Button) findViewById(R.id.importConfirmButton);
        final RadioButton originalCategories = (RadioButton) findViewById(R.id.originalCategoriesRB);
        final RadioButton specifiedCategory = (RadioButton) findViewById(R.id.specifiedCategoryRB);
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/*");
                startActivityForResult(intent, GET_FILE_RESULT_CODE);
            }
        });
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uri == null) {
                    Toast.makeText(getApplicationContext(), "No export file has been selected", Toast.LENGTH_SHORT).show();
                }
                else {
                    readExportFile(uri);
                    finish();
                }
            }
        });
        Button cancelButton = (Button) findViewById(R.id.importCancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        originalCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                specifiedCategory.setChecked(false);
                originalCategories.setChecked(true);
                importOption = IMPORT_OPTION_ORIGINAL_CATEGORIES;
                importCategoryName.setEnabled(false);
            }
        });
        specifiedCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                originalCategories.setChecked(false);
                specifiedCategory.setChecked(true);
                importOption = IMPORT_OPTION_SPECIFIED_CATEGORY;
                importCategoryName.setEnabled(true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImportDialog.GET_FILE_RESULT_CODE && resultCode == RESULT_OK) {
            uri = data.getData();
            File file= new File(uri.getPath());
            exportFileName.setText(file.getName());
            if (!file.getName().matches(".*MyVocabExportFile.*")) {
                Toast.makeText(getApplicationContext(), "Please make sure the correct export file is selected", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void readExportFile(Uri uri) {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line;
            int count = 0;
            // Skip first header line
            br.readLine();
            while ((line = br.readLine()) != null) {
                Log.d("Test5:", line);
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

                if (resetVocabProgress.isChecked()) {
                    level = ReviewSession.DIFFICULT;
                }

                categoryName = categoryName.replace("\\,", ",");
                categoryDescription = categoryDescription.replace("\\,", ",");

                if (importOption == IMPORT_OPTION_SPECIFIED_CATEGORY) {
                    categoryName = importCategoryName.getText().toString();
                    categoryDescription = "Vocab from the export file";
                }

                // 4. Insert into category and MyVocab db
                VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(this);
                if (!dbHelper.checkIfCategoryExists(categoryName)) {
                    dbHelper.insertCategory(categoryName, categoryDescription);
                }

                if (!dbHelper.checkIfVocabExistsInCategory(vocab, definition, categoryName)) {
                    dbHelper.insertVocab(categoryName, vocab, definition, level);
                }

                count++;

                // TODO: REMOVE COUNT
                if (count > 10) {
                    break;
                }

            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
