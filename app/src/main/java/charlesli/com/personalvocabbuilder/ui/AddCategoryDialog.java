package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-01-01.
 */

public class AddCategoryDialog extends CustomDialog {

    public AddCategoryDialog(Context context, final CategoryCursorAdapter categoryAdapter) {
        super(context);

        setTitle("Add Category");

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_add_category, null);

        final EditText categoryNameInput = (EditText) promptsView.findViewById(R.id.categoryNameInput);
        final EditText categoryDescInput = (EditText) promptsView.findViewById(R.id.categoryDescInput);
        setView(promptsView);
        final VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(context);

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
}
