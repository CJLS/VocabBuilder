package charlesli.com.personalvocabbuilder.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.controller.ExportUtils;
import charlesli.com.personalvocabbuilder.controller.PermissionsUtils;
import charlesli.com.personalvocabbuilder.sqlDatabase.ExportCursorAdaptor;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-03-12.
 */

public class ExportCategoryDialog extends CustomDialog {

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
        ExportCursorAdaptor mCursorAdapter = new ExportCursorAdaptor(context, categoryCursor, 0);
        exportListView.setAdapter(mCursorAdapter);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (PermissionsUtils.getExternalStoragePermission(context)) {
                    ExportUtils.exportCategory(context);
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

}
