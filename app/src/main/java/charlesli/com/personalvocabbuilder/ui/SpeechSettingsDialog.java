package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.controller.CustomTTS;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-05-29.
 */

public class SpeechSettingsDialog extends CustomDialog {

    private int selectedPos;
    private String selectedLocaleDisplayName;

    public SpeechSettingsDialog(final Context context, final String category, final CustomTTS textToSpeech) {
        super(context);

        setTitle("Speech Settings");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_speech_settings, null);

        final VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(context);
        selectedLocaleDisplayName = dbHelper.getCategoryLocaleDisplayName(category);
        final HashMap<String, Locale> displayNameToLocaleMapping = textToSpeech.getSupportedDisplayNameToLocaleMapping();
        final ArrayList<String> supportedLanguages = new ArrayList<>(displayNameToLocaleMapping.keySet());
        Collections.sort(supportedLanguages);
        selectedPos = supportedLanguages.indexOf(selectedLocaleDisplayName);

        setupLanguageSelector((Spinner) promptsView.findViewById(R.id.vocabLanguageSpinner),
                supportedLanguages);
        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedLocaleDisplayName = supportedLanguages.get(selectedPos);
                dbHelper.updateCategoryLocaleDisplayName(category, selectedLocaleDisplayName);
                int result = textToSpeech.setLanguage(displayNameToLocaleMapping.get(selectedLocaleDisplayName));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, "Please enable internet to download the selected language voice data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setButton(BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
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
