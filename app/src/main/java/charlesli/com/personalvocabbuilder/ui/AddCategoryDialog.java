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

public class AddCategoryDialog extends CustomDialog {

    public AddCategoryDialog(Context context, final VocabDbHelper dbHelper,
                             final CategoryCursorAdapter categoryAdapter) {
        super(context);

        final EditText categoryNameInput = new EditText(context);
        final EditText categoryDescInput = new EditText(context);
        setTitle("Add Category");
        setView(setUpCustomDialogLayout(categoryNameInput, categoryDescInput));

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = categoryNameInput.getText().toString();
                String description = categoryDescInput.getText().toString();
                if (dbHelper.checkIfCategoryExists(name)) {
                    Toast.makeText(getContext(), name + " already exists", Toast.LENGTH_SHORT).show();
                }
                else {
                    dbHelper.insertCategory(name, description);
                    categoryAdapter.changeCursor(dbHelper.getCategoryCursor());
                }
            }
        });
        setButton(BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

    private LinearLayout setUpCustomDialogLayout(EditText categoryNameInput, EditText categoryDescInput) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        categoryNameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        categoryNameInput.setHint("Name");
        layout.addView(categoryNameInput);

        categoryDescInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        categoryDescInput.setHint("Description");
        layout.addView(categoryDescInput);

        return layout;
    }
}
