package charlesli.com.personalvocabbuilder.ui;

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
 * Created by charles on 2017-04-23.
 */

public class ImportDialog extends CustomDialog {

    public ImportDialog(final Context context) {
        super(context);

        setTitle("Import");
        LayoutInflater li = LayoutInflater.from(context);
        // TODO: Replace placeholder
        View promptsView = li.inflate(R.layout.alert_dialog_export, null);

        VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(context);
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
