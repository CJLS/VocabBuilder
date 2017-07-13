package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.controller.CustomTTS;
import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-01-01.
 */

public class AddCategoryDialog extends CustomDialog {

    private int selectedPos;

    public AddCategoryDialog(final Context context, final CategoryCursorAdapter categoryAdapter, CustomTTS textToSpeech) {
        super(context);

        setTitle("Add Category");

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_add_category, null);
        Spinner speechLanguageSpinner = (Spinner) promptsView.findViewById(R.id.speechLanguageSpinner);

        final HashMap<String, Locale> displayNameToLocaleMapping = textToSpeech.getSupportedDisplayNameToLocaleMapping();
        final ArrayList<String> supportedLanguages = new ArrayList<>(displayNameToLocaleMapping.keySet());
        Collections.sort(supportedLanguages);
        selectedPos = supportedLanguages.indexOf(Locale.US.getDisplayName());

        setupLanguageSelector(speechLanguageSpinner, supportedLanguages);
        final EditText categoryNameInput = (EditText) promptsView.findViewById(R.id.categoryNameInput);
        final EditText categoryDescInput = (EditText) promptsView.findViewById(R.id.categoryDescInput);
        setView(promptsView);
        final VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(context);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = categoryNameInput.getText().toString();
                String description = categoryDescInput.getText().toString();
                if (dbHelper.checkIfCategoryExists(categoryName)) {
                    Toast.makeText(getContext(), categoryName + " already exists", Toast.LENGTH_SHORT).show();
                }
                else {
                    dbHelper.insertCategory(categoryName, description);
                    categoryAdapter.changeCursor(dbHelper.getCategoryCursor());
                    dbHelper.updateCategoryLocaleDisplayName(categoryName, supportedLanguages.get(selectedPos));
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

    private void setupLanguageSelector(Spinner spinner, final List<String> languages){

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, languages);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(selectedPos);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
