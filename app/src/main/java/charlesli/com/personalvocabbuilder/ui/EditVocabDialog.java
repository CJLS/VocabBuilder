package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import charlesli.com.personalvocabbuilder.controller.MyVocab;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static android.content.Context.MODE_PRIVATE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.DATE_ASC;

/**
 * Created by charles on 2017-01-22.
 */

public class EditVocabDialog extends CustomDialog {

    public EditVocabDialog(Context context, final String selectedVocab, final String selectedDefinition,
                              final long id, final VocabDbHelper dbHelper,
                              final String category, final VocabCursorAdapter cursorAdapter) {
        super(context);

        setTitle("Edit Vocab");
        setMessage(selectedVocab);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText definitionInput = new EditText(context);
        definitionInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        definitionInput.setHint("New Definition");
        definitionInput.setText(selectedDefinition);
        layout.addView(definitionInput);

        setView(layout);

        setButton(BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        setButton(BUTTON_POSITIVE, "Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String definition = definitionInput.getText().toString();
                dbHelper.updateVocabDefinition(selectedVocab, selectedDefinition, definition);

                // Update Cursor
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(category, DATE_ASC);
                Cursor cursor = dbHelper.getVocabCursorWithStringPattern(category, MyVocab.searchPattern, orderBy);
                cursorAdapter.changeCursor(cursor);
            }
        });
        setButton(BUTTON_NEGATIVE, "Delete Vocab", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.deleteVocab(id);

                // Update Cursor
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(category, DATE_ASC);
                Cursor cursor = dbHelper.getVocabCursor(category, orderBy);
                cursorAdapter.changeCursor(cursor);
            }
        });
    }
}
