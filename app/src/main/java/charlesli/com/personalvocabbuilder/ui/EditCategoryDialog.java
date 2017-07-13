package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-01-01.
 */

public class EditCategoryDialog extends CustomDialog {

    public EditCategoryDialog(Context context, final CategoryCursorAdapter cursorAdapter,
                              final String selectedCategory, String selectedDesc) {
        super(context);

        final EditText categoryNameInput = new EditText(getContext());
        final EditText categoryDescInput = new EditText(getContext());
        setTitle("Edit Category");
        setView(setUpCustomDialogLayout(categoryNameInput, categoryDescInput,
                selectedCategory, selectedDesc));
        final VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(context);

        setButton(BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        setButton(BUTTON_POSITIVE, "Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = categoryNameInput.getText().toString();
                String categoryDesc = categoryDescInput.getText().toString();

                // If new category name exists already
                if (!selectedCategory.equals(categoryName) && dbHelper.checkIfCategoryExists(categoryName)) {
                    Toast.makeText(getContext(), categoryName + " already exists", Toast.LENGTH_SHORT).show();
                }
                else {
                    dbHelper.updateCategoryNameAndDesc(selectedCategory, categoryName, categoryDesc);
                    cursorAdapter.changeCursor(dbHelper.getCategoryCursor());
                    dialog.dismiss();
                }
            }
        });
        setButton(BUTTON_NEGATIVE, "Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteCategoryDialog alertDialog = new DeleteCategoryDialog(getContext(), dbHelper,
                        cursorAdapter, selectedCategory);
                alertDialog.show();
                alertDialog.changeButtonsToAppIconColor();
            }
        });


    }

    private LinearLayout setUpCustomDialogLayout(EditText categoryNameInput, EditText categoryDescInput,
                                                 String selectedCategory, String selectedDesc) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        categoryNameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        categoryNameInput.setHint("New name");
        categoryNameInput.setText(selectedCategory);
        layout.addView(categoryNameInput);


        categoryDescInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        categoryDescInput.setHint("New description");
        categoryDescInput.setText(selectedDesc);
        layout.addView(categoryDescInput);

        return layout;
    }
}
