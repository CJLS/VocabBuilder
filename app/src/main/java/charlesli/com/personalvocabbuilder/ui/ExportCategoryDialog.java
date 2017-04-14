package charlesli.com.personalvocabbuilder.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.controller.ExportUtils;
import charlesli.com.personalvocabbuilder.sqlDatabase.ExportCursorAdaptor;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static charlesli.com.personalvocabbuilder.controller.ExportUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

/**
 * Created by charles on 2017-03-12.
 */

public class ExportCategoryDialog extends CustomDialog {

    private ExportCursorAdaptor mCursorAdapter;

    //TODO: Add TEST cases
    //T1: No apps can send intent
    //T2: External Storage is unavailable
    //T5: Permission request is denied **
    //T6: Permission request is denied, enabled, and export file
    //T7: Permission request granted at first but disabled later
    //T8: Permission request is denied, export file
    //T9: Check if all apps can send file intent properly with file

    public ExportCategoryDialog(final Context context, final VocabDbHelper dbHelper) {
        super(context);

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
                    ExportUtils.exportCategory(context, dbHelper, mCursorAdapter);
                }
            }
        });
        setButton(BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
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
            Log.d("Test: ", "Get External Storage Permission denied");
            ActivityCompat.requestPermissions(getOwnerActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            Log.d("Test: ", "After request permission");
            return false;
        }
        return true;
    }


    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
