package charlesli.com.personalvocabbuilder.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.controller.ExportUtils;
import charlesli.com.personalvocabbuilder.controller.PermissionsUtils;
import charlesli.com.personalvocabbuilder.sqlDatabase.ExportCursorAdaptor;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-03-12.
 */

public class ExportDialog extends CustomDialog {

    public ExportDialog(final Context context) {
        super(context);

        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }

        setTitle("Select categories to export: ");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_export, null);

        VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(context);
        Cursor categoryCursor = dbHelper.getCategoryCursor();

        ListView exportListView = (ListView) promptsView.findViewById(R.id.exportListView);
        final ExportCursorAdaptor exportCursorAdaptor = new ExportCursorAdaptor(context, categoryCursor, 0);
        exportListView.setAdapter(exportCursorAdaptor);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ExportCursorAdaptor.getSelectedCategoryPositionList().size() == 0) {
                    Toast.makeText(context, "No categories were selected for export.", Toast.LENGTH_LONG).show();
                }
                else if (PermissionsUtils.getExternalStoragePermission(context)) {
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
