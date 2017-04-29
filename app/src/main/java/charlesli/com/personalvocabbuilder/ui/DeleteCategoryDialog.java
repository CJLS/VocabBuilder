package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;

import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-01-07.
 */

public class DeleteCategoryDialog extends CustomDialog {

    public DeleteCategoryDialog(Context context, final VocabDbHelper dbHelper,
                                final CategoryCursorAdapter cursorAdapter,
                                final String selectedCategory) {
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
                dbHelper.deleteCategory(selectedCategory);
                cursorAdapter.changeCursor(dbHelper.getCategoryCursor());
            }
        });
    }
}
