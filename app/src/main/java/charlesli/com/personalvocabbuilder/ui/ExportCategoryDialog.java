package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.ExportCursorAdaptor;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-03-12.
 */

public class ExportCategoryDialog extends CustomDialog {

    public ExportCategoryDialog(Context context, final VocabDbHelper dbHelper) {
        super(context);

        setTitle("Export");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_export, null);

        Cursor categoryCursor = dbHelper.getCategoryCursor();

        ListView exportListView = (ListView) promptsView.findViewById(R.id.exportListView);
        final ExportCursorAdaptor cursorAdaptor = new ExportCursorAdaptor(context, categoryCursor, 0);
        exportListView.setAdapter(cursorAdaptor);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exportCategory(dbHelper, cursorAdaptor.getSelectedCategoryPositionList());
            }
        });
    }

    private void exportCategory(VocabDbHelper dbHelper, List<Integer> categoryPositionList) {
        Cursor cursor = dbHelper.getCategoryCursor(categoryPositionList);
        Log.d("Cursor Count: ", String.valueOf(cursor.getCount()));
    }
}
