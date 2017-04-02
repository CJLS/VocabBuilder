package charlesli.com.personalvocabbuilder.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

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
    private Context context;

    //TODO: Add TEST cases
    //T1: Chinese, spanish, korean characters
    //T2: Permission enabled
    //T3: Permission disabled

    // Check all possible excpetions toast message cases, can manually throw exceptions for testing


    public ExportCategoryDialog(final Context context, final VocabDbHelper dbHelper) {
        super(context);

        mDBHelper = dbHelper;
        this.context = context;

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
                    exportCategory();
                }
            }
        });
    }

    private void exportCategory() {
        File exportFile = writeToExportFile(mCursorAdapter.getSelectedCategoryPositionList());
        shareExportFile(exportFile);
    }

    private boolean shareExportFile(File exportFile) {
        boolean exportFileSent = true;
        Intent sendFileIntent = new Intent();
        sendFileIntent.setAction(Intent.ACTION_SEND);
        sendFileIntent.setType("text/csv");
        sendFileIntent.putExtra(Intent.EXTRA_SUBJECT, "My Vocab Export File");
        sendFileIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exportFile));

        if (sendFileIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(sendFileIntent, "Send to"));
        }
        else {
            exportFileSent = false;
        }
        return exportFileSent;
    }

    private boolean getExternalStoragePermission() {
        if (!isExternalStorageWritable()) {
            Toast.makeText(getContext(), "External storage is unavailable. Please check your device's external storage.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (getOwnerActivity() == null) {
            Toast.makeText(getContext(), "Sorry, the export operation did not go through. Please try again later.", Toast.LENGTH_LONG).show();
            return false;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(getOwnerActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getOwnerActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    private File writeToExportFile(List<Integer> categoryPositionList) {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        path.mkdirs();
        File file = new File(path, "MyVocabExportFile.csv");

        try {
            FileWriter fileWriter = new FileWriter(file);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("Vocab,Definition,Level,Category Name,Category Description");

            bufferedWriter.newLine();

            Cursor cursor = mDBHelper.getExportCursor(categoryPositionList);
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
                String description = mDBHelper.getCategoryDefinition(category);
                category = category.replace(",", "\\,");
                description = description.replace(",", "\\,");
                String lineToWrite = vocab + "," + definition + "," + level + "," + category + "," + description;

                bufferedWriter.write(lineToWrite);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }
        catch (Exception e) {
            Toast.makeText(getContext(), "Sorry, an error has occurred when writing to the export file. Please try again later.", Toast.LENGTH_LONG).show();
        }
        return file;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportCategory();
                } else {
                    Toast.makeText(getContext(), "Export functionality can't be carried out without permission to write to external storage.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
