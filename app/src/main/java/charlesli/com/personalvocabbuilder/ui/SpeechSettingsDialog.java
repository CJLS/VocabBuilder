package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.controller.CustomTTS;

/**
 * Created by charles on 2017-05-29.
 */

public class SpeechSettingsDialog extends CustomDialog {

    private int selectedPos;

    public SpeechSettingsDialog(Context context, final String category, final CustomTTS textToSpeech) {
        super(context);

        setTitle("Speech Settings");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_speech_settings, null);

        final SharedPreferences sharedPreferences = context
                .getSharedPreferences(context.getResources().getString(R.string.sharedPrefSpeechFile), Context.MODE_PRIVATE);
        selectedPos = sharedPreferences.getInt(category, textToSpeech.getDefaultLanguageSelectionPos());
        final HashMap<String, Locale> hashMap = textToSpeech.getSupportedDisplayNameToLocaleMapping();
        final ArrayList<String> supportedLanguages = new ArrayList<>(hashMap.keySet());
        Collections.sort(supportedLanguages);

        setupLanguageSelector((Spinner) promptsView.findViewById(R.id.vocabLanguageSpinner),
                supportedLanguages);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(category, selectedPos);
                editor.apply();
                textToSpeech.setLanguage(hashMap.get(supportedLanguages.get(selectedPos)));
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
