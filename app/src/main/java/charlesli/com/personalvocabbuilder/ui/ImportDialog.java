package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;

import charlesli.com.personalvocabbuilder.R;
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
        View promptsView = li.inflate(R.layout.alert_dialog_import, null);

        VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(context);
        Cursor categoryCursor = dbHelper.getCategoryCursor();


        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

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
