package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-03-12.
 */

public class ExportCategoryDialog extends CustomDialog {

    public ExportCategoryDialog(Context context, VocabDbHelper dbHelper) {
        super(context);

        setTitle("Export");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_export, null);

        Cursor categoryCursor = dbHelper.getCategoryCursor();
        Spinner categorySpinner = (Spinner) promptsView.findViewById(R.id.exportSpinner);
        setUpExportSpinner(categoryCursor, categorySpinner);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

    private void setUpExportSpinner(final Cursor categoryCursor, Spinner spinner) {
        String[] from = {VocabDbContract.COLUMN_NAME_CATEGORY};
        int[] to = {android.R.id.text1};

        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getContext(), android.R.layout.simple_spinner_item,
                categoryCursor, from, to, 0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
}
