package charlesli.com.personalvocabbuilder.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.controller.ReviewSession;
import charlesli.com.personalvocabbuilder.sqlDatabase.ExportCursorAdaptor;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-03-12.
 */

public class ExportCategoryDialog extends CustomDialog implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private VocabDbHelper mDBHelper;
    private ExportCursorAdaptor mCursorAdapter;

    public ExportCategoryDialog(Context context, final VocabDbHelper dbHelper) {
        super(context);

        mDBHelper = dbHelper;

        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }

        setTitle("Export");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_export, null);

        Cursor categoryCursor = dbHelper.getCategoryCursor();

        ListView exportListView = (ListView) promptsView.findViewById(R.id.exportListView);
        mCursorAdapter = new ExportCursorAdaptor(context, categoryCursor, 0);
        exportListView.setAdapter(mCursorAdapter);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getExternalStoragePermission()) {
                    exportCategory(dbHelper, mCursorAdapter.getSelectedCategoryPositionList());
                }
            }
        });
    }

    private boolean getExternalStoragePermission() {
        if (!isExternalStorageWritable()) {
            Log.d("Test: ", "External Storage is not writable");
            return false;
        }
        if (getOwnerActivity() == null) {
            Log.d("Test: ", "Owner Activity is null");
            return false;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(getOwnerActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.d("Test: ", "Permission Denied");

            ActivityCompat.requestPermissions(getOwnerActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            return false;
        }
        else {
            Log.d("Test: ", "Permission Granted");
            return true;
        }
    }

    private void exportCategory(VocabDbHelper dbHelper, List<Integer> categoryPositionList) {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        path.mkdirs();
        File file = new File(path, "MyVocabExportFile2.csv");

        try {
            FileWriter fileWriter = new FileWriter(file);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("Vocab,Definition,Level,Category Name,Category Description");

            bufferedWriter.newLine();

            Cursor cursor = dbHelper.getExportCursor(categoryPositionList);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String vocab = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_VOCAB));
                vocab = vocab.replace(",", "\\,");
                String definition = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_DEFINITION));
                definition = definition.replace(",", "\\,");
                int lvl = cursor.getInt(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_LEVEL));
                String level;
                switch (lvl) {
                    case ReviewSession.DIFFICULT:
                        level = "Difficult";
                        break;
                    case ReviewSession.FAMILIAR:
                        level = "Familiar";
                        break;
                    case ReviewSession.EASY:
                        level = "Easy";
                        break;
                    case ReviewSession.PERFECT:
                        level = "Perfect";
                        break;
                    default:
                        level = "Difficult";
                        break;
                }
                String category = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_CATEGORY));
                String description = dbHelper.getCategoryDefinition(category);
                category = category.replace(",", "\\,");
                description = description.replace(",", "\\,");
                String lineToWrite = vocab + "," + definition + "," + level + "," + category + "," + description;

                bufferedWriter.write(lineToWrite);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }
        catch (Exception e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
        }

    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("OnRequestPermission: ", "Permission Granted");
                    exportCategory(mDBHelper, mCursorAdapter.getSelectedCategoryPositionList());
                } else {
                    Log.d("OnRequestPermission: ", "Permission Denied");
                }
                return;
            }
        }
    }
}
