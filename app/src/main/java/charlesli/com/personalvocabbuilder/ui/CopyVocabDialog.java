package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Iterator;

import charlesli.com.personalvocabbuilder.sqlDatabase.VocabCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by charles on 2017-01-21.
 */

public class CopyVocabDialog extends CustomDialog {

    private final String DATE_ASC = VocabDbContract._ID + " ASC";

    public CopyVocabDialog(Context context, final VocabDbHelper dbHelper,
                           final VocabCursorAdapter cursorAdapter, final String fromCategory) {
        super(context);

        final String[] selectedCategory = new String[1];
        setTitle("Add Vocab To...");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final Spinner spinner = new Spinner(context);
        String[] from = {VocabDbContract.COLUMN_NAME_CATEGORY};
        int[] to = {android.R.id.text1};
        final Cursor categoryCursor = dbHelper.getCategoryCursor();
        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getContext(), android.R.layout.simple_spinner_item,
                categoryCursor, from, to, 0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryCursor.moveToPosition(position);
                selectedCategory[0] = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        layout.addView(spinner);
        setView(layout);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addVocabToSelectedTable(cursorAdapter, dbHelper, fromCategory, selectedCategory[0]);
            }
        });
        setButton(BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

    private void addVocabToSelectedTable(VocabCursorAdapter cursorAdapter, VocabDbHelper dbHelper,
                                           String fromCategory, String toCategory) {
        Iterator<Integer> posIt = cursorAdapter.selectedItemsPositions.iterator();
        if (cursorAdapter.selectedItemsPositions.isEmpty()) {
            Toast.makeText(getContext(), "No words are selected", Toast.LENGTH_SHORT).show();
        }
        else {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            while (posIt.hasNext()) {
                Integer posInt = posIt.next();
                Integer idInt = (int) cursorAdapter.getItemId(posInt);
                String[] projection = {
                        VocabDbContract._ID,
                        VocabDbContract.COLUMN_NAME_VOCAB,
                        VocabDbContract.COLUMN_NAME_DEFINITION,
                        VocabDbContract.COLUMN_NAME_LEVEL
                };
                String[] selectionArg = {
                        String.valueOf(idInt)
                };
                Cursor cursor = db.query(
                        VocabDbContract.TABLE_NAME_MY_VOCAB,
                        projection,
                        VocabDbContract._ID + "=?",
                        selectionArg,
                        null,
                        null,
                        null
                );
                cursor.moveToFirst();
                String vocab = cursor.getString(cursor.getColumnIndex(VocabDbContract.COLUMN_NAME_VOCAB));
                String definition = cursor.getString(cursor.getColumnIndex(VocabDbContract.COLUMN_NAME_DEFINITION));
                Integer level = cursor.getInt(cursor.getColumnIndex(VocabDbContract.COLUMN_NAME_LEVEL));
                dbHelper.insertVocab(toCategory, vocab, definition, level);
            }
            cursorAdapter.selectedItemsPositions.clear();

            SharedPreferences sharedPreferences = getContext().getSharedPreferences("Sort Order", MODE_PRIVATE);
            String orderBy = sharedPreferences.getString(fromCategory, DATE_ASC);

            Cursor cursor = dbHelper.getVocabCursor(fromCategory, orderBy);
            cursorAdapter.changeCursor(cursor);

            Toast.makeText(getContext(), "Vocab added successfully", Toast.LENGTH_SHORT).show();
        }
    }
}
