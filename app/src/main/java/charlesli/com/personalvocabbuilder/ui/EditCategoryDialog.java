package charlesli.com.personalvocabbuilder.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Created by charles on 2017-01-01.
 */

public class EditCategoryDialog extends AlertDialog{

    public EditCategoryDialog(Context context, String selectedCategory, String selectedDesc) {
        super(context);

        final EditText categoryNameInput = new EditText(getContext());
        final EditText categoryDescInput = new EditText(getContext());
        setTitle("Edit Category");
        setView(setUpCustomDialogLayout(categoryNameInput, categoryDescInput,
                selectedCategory, selectedDesc));

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
