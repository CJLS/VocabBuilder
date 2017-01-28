package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;

import charlesli.com.personalvocabbuilder.sqlDatabase.VocabCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static android.content.Context.MODE_PRIVATE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.DATE_ASC;

/**
 * Created by charles on 2017-01-26.
 */

class DuplicateVocabEntryDialog extends CustomDialog {

    DuplicateVocabEntryDialog(Context context, final VocabDbHelper dbHelper,
                              final VocabCursorAdapter cursorAdapter, final String currentCategory,
                              final String vocab, final String definition) {
        super(context);
        setTitle("Do you still want to add this vocab?");
        setMessage("This vocab already exists in " + dbHelper.findVocabFirstCategory(vocab, definition, currentCategory) + ".");
        setButton(BUTTON_POSITIVE, "YES", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.insertVocab(currentCategory, vocab, definition, 0);
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("Sort Order", MODE_PRIVATE);
                String orderBy = sharedPreferences.getString(currentCategory, DATE_ASC);

                Cursor cursor = dbHelper.getVocabCursor(currentCategory, orderBy);
                cursorAdapter.changeCursor(cursor);
            }
        });

        setButton(BUTTON_NEGATIVE, "NO", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }
}
