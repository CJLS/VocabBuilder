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

    private int sourceLanguagePos;
    private int targetLanguagePos;

    public TranslationSettingsDialog(final Context context) {
        super(context);

        setTitle("Translation Settings");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_translation_settings, null);

        final SharedPreferences sharedPreferences = getContext().
                getSharedPreferences(getContext().getResources().getString(R.string.sharedPrefTranslationFile), Context.MODE_PRIVATE);

        sourceLanguagePos = sharedPreferences.getInt(context.getString(R.string.sharedPrefTranslationSourceKey), DETECT_LANGUAGE);
        targetLanguagePos = sharedPreferences.getInt(context.getString(R.string.sharedPrefTranslationTargetKey), DEFAULT_TARGET_LANGUAGE_ENGLISH);

        setupLanguageSelector((Spinner) promptsView.findViewById(R.id.spinnerTranslateFrom),
                FROM_LANGUAGE, true, sourceLanguagePos);

        setupLanguageSelector((Spinner) promptsView.findViewById(R.id.spinnerTranslateTo),
                TO_LANGUAGE, false, targetLanguagePos);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(context.getString(R.string.sharedPrefTranslationSourceKey), sourceLanguagePos);
                editor.putInt(context.getString(R.string.sharedPrefTranslationTargetKey), targetLanguagePos);
                editor.apply();
            }
        });

        setButton(BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


    }

    private void setupLanguageSelector(Spinner spinner, String[] languages,
                                       final boolean isSource, int currentSelection){

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, languages);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(currentSelection);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSource) {
                    sourceLanguagePos = position;
                }
                else {
                    targetLanguagePos = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


}
