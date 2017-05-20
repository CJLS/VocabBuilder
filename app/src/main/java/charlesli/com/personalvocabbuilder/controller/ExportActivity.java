package charlesli.com.personalvocabbuilder.controller;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.ExportCursorAdaptor;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static charlesli.com.personalvocabbuilder.controller.ExportUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

public class ExportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        ListView listView = (ListView) findViewById(R.id.exportActivityLV);

        Button okButton = (Button) findViewById(R.id.exportActivityOKButton);
        Button cancelButton = (Button) findViewById(R.id.exportActivityCancelButton);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.exportActivityProgressBar);

        final VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(this);
        Cursor categoryCursor = dbHelper.getCategoryCursor();

        final ExportCursorAdaptor exportCursorAdaptor = new ExportCursorAdaptor(this, categoryCursor, 0);
        listView.setAdapter(exportCursorAdaptor);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ExportCursorAdaptor.getSelectedCategoryPositionList().size() == 0) {
                    Toast.makeText(getApplicationContext(), "No categories were selected for export.", Toast.LENGTH_LONG).show();
                }
                else if (PermissionsUtils.getExternalStoragePermission(ExportActivity.this)) {
                    Log.d("Activity Test:", "Before FileExporter");
                    FileExporter fileExporter = new FileExporter(ExportActivity.this, progressBar);
                    fileExporter.execute();
                    Log.d("Activity Test:", "After FileExporter");
                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                Log.d("Test:", "In export Activity");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ExportCursorAdaptor.getSelectedCategoryPositionList().size() == 0) {
                        Toast.makeText(this, "No categories were selected for export.", Toast.LENGTH_LONG).show();
                    }
                    else {
                        ExportUtils.exportCategory(this);
                    }
                } else {
                    Toast.makeText(this, R.string.externalStoragePermissionDenied, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
