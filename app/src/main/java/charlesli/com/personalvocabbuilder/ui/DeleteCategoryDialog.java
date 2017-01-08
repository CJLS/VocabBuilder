package charlesli.com.personalvocabbuilder.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;

import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-01-07.
 */

public class DeleteCategoryDialog extends AlertDialog {

    public DeleteCategoryDialog(Context context, final VocabDbHelper dbHelper,
                                final CategoryCursorAdapter cursorAdapter, final String selectedCategory) {
        super(context);

        setMessage("This action will delete all the vocab in this category.");
        setButton(BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        setButton(BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete Category from Database
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String selection = VocabDbContract.COLUMN_NAME_CATEGORY + " LIKE ?";
                // Specify arguments in placeholder order
                String[] selectionArgs = {selectedCategory};
                // Issue SQL statement
                db.delete(VocabDbContract.TABLE_NAME_CATEGORY, selection, selectionArgs);
                db.delete(VocabDbContract.TABLE_NAME_MY_VOCAB, selection, selectionArgs);

                // Update Cursor
                cursorAdapter.changeCursor(dbHelper.getCategoryCursor());
            }
        });
    }
}
