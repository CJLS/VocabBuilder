package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import charlesli.com.personalvocabbuilder.R;

import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.DEFAULT_TARGET_LANGUAGE_ENGLISH;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.DETECT_LANGUAGE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.FROM_LANGUAGE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.TO_LANGUAGE;

/**
 * Created by charles on 2016-12-30.
 */

public class TranslationSettingsDialog extends CustomDialog {

    public TranslationSettingsDialog(Context context) {
        super(context);

        setTitle("Translation Settings");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_translation_settings, null);

        setupLanguageSelector((Spinner) promptsView.findViewById(R.id.vocabLanguageSpinner),
                FROM_LANGUAGE, context.getString(R.string.sharedPrefTranslationSourceKey), DETECT_LANGUAGE);

        setupLanguageSelector((Spinner) promptsView.findViewById(R.id.spinnerTranslateTo),
                TO_LANGUAGE, context.getString(R.string.sharedPrefTranslationTargetKey), DEFAULT_TARGET_LANGUAGE_ENGLISH);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


    }

    private void setupLanguageSelector(Spinner spinner, String[] languages,
                                       final String sharedPrefKey, int defaultSelection){

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, languages);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);


        final SharedPreferences sharedPreferences = getContext().
                getSharedPreferences(getContext().getResources().getString(R.string.sharedPrefTranslationFile), Context.MODE_PRIVATE);
        spinner.setSelection(sharedPreferences.getInt(sharedPrefKey, defaultSelection));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(sharedPrefKey, position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


}
