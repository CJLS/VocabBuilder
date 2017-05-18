package charlesli.com.personalvocabbuilder.controller;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import charlesli.com.personalvocabbuilder.R;

public class ImportActivity extends AppCompatActivity {

    public static int IMPORT_OPTION_ORIGINAL_CATEGORIES = 0;
    public static int IMPORT_OPTION_SPECIFIED_CATEGORY = 1;
    private int GET_FILE_RESULT_CODE = 1;
    private int importOption = IMPORT_OPTION_ORIGINAL_CATEGORIES;
    private TextView exportFileName;
    private EditText importCategoryName;
    private CheckBox resetVocabProgress;
    private ProgressBar importProgressBar;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        exportFileName = (TextView) findViewById(R.id.exportFileName);
        importCategoryName = (EditText) findViewById(R.id.importCategory);
        resetVocabProgress = (CheckBox) findViewById(R.id.resetVocabProgress);
        importProgressBar = (ProgressBar) findViewById(R.id.importProgressBar);
        Button selectFileButton = (Button) findViewById(R.id.selectFileButton);
        final Button importButton = (Button) findViewById(R.id.importConfirmButton);
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
                    ExportFileReader exportFileReader =
                            new ExportFileReader(ImportActivity.this,
                                    resetVocabProgress.isChecked(), importOption,
                                    importCategoryName.getText().toString(),
                                    importProgressBar, uri);
                    exportFileReader.execute();
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
        if (requestCode == GET_FILE_RESULT_CODE && resultCode == RESULT_OK) {
            uri = data.getData();
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};

            String fileName = getFileName(projection);
            exportFileName.setText(fileName);
            if (!fileName.matches(".*MyVocabExportFile.*")) {
                Toast.makeText(getApplicationContext(), "Please make sure the correct export file is selected", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getFileName(String[] projection) {
        String fileName = "File Selected";
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            Cursor returnCursor =
                    getContentResolver().query(uri, projection, null, null, null);
            if (returnCursor != null) {
                try {
                    if (returnCursor.moveToFirst()) {
                        fileName = returnCursor.getString(0);
                    }
                } finally {
                    returnCursor.close();
                }
            }
        }
        else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            File file = new File(uri.getPath());
            fileName = file.getName();
        }
        return fileName;
    }

}
