package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Iterator;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static android.content.Context.MODE_PRIVATE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.DATE_ASC;

/**
 * Created by charles on 2017-01-21.
 */

public class CopyVocabDialog extends CustomDialog {

    public CopyVocabDialog(Context context, final VocabDbHelper dbHelper,
                           final VocabCursorAdapter cursorAdapter, final String fromCategory) {
        super(context);

        final String[] selectedCategory = new String[1];
        setTitle("Add Vocab To...");

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_copy_vocab, null);
        Spinner spinner = (Spinner) promptsView.findViewById(R.id.copyVocabSpinner);

        setView(promptsView);

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

        setButton(BUTTON_POSITIVE, "COPY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addVocabToSelectedTable(cursorAdapter, dbHelper, fromCategory, selectedCategory[0], true);
            }
        });
        setButton(BUTTON_NEGATIVE, "MOVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addVocabToSelectedTable(cursorAdapter, dbHelper, fromCategory, selectedCategory[0], false);
            }
        });
        setButton(BUTTON_NEUTRAL, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

    private void addVocabToSelectedTable(VocabCursorAdapter cursorAdapter, VocabDbHelper dbHelper,
                                           String fromCategory, String toCategory, boolean toBeCopied) {
        Iterator<Integer> posIt = cursorAdapter.selectedItemsPositions.iterator();
        boolean allAdded = true;
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
                if (cursor == null || cursor.getCount() == 0) {
                    allAdded = false;
                    continue;
                }
                cursor.moveToFirst();
                String vocab = cursor.getString(cursor.getColumnIndex(VocabDbContract.COLUMN_NAME_VOCAB));
                String definition = cursor.getString(cursor.getColumnIndex(VocabDbContract.COLUMN_NAME_DEFINITION));
                Integer level = cursor.getInt(cursor.getColumnIndex(VocabDbContract.COLUMN_NAME_LEVEL));
                if (cursor.isNull(cursor.getColumnIndex(VocabDbContract.COLUMN_NAME_REVIEWED_AT))) {
                    dbHelper.insertVocab(toCategory, vocab, definition, level);
                }
                else {
                    String reviewedAt = cursor.getString(cursor.getColumnIndex(VocabDbContract.COLUMN_NAME_REVIEWED_AT));
                    dbHelper.insertVocab(toCategory, vocab, definition, level, reviewedAt);
                }
                if (!toBeCopied) {
                    dbHelper.deleteVocab(idInt);
                }
                cursor.close();
            }
            cursorAdapter.selectedItemsPositions.clear();

            SharedPreferences sharedPreferences = getContext().getSharedPreferences("Sort Order", MODE_PRIVATE);
            String orderBy = sharedPreferences.getString(fromCategory, DATE_ASC);

            Cursor cursor = dbHelper.getVocabCursor(fromCategory, orderBy);
            cursorAdapter.changeCursor(cursor);

            if (allAdded) {
                if (toBeCopied) {
                    Toast.makeText(getContext(), "Vocab copied successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Vocab moved successfully", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                if (toBeCopied) {
                    Toast.makeText(getContext(), "Sorry, some vocab might not have been copied", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Sorry, some vocab might not have been moved", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
