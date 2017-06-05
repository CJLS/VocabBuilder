package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import charlesli.com.personalvocabbuilder.R;

/**
 * Created by charles on 2017-05-29.
 */

public class SpeechSettingsDialog extends CustomDialog {

    public SpeechSettingsDialog(Context context, TextToSpeech textToSpeech, String category) {
        super(context);

        setTitle("Speech Settings");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_speech_settings, null);

        final ArrayList<String> engineAvailableLanguages = new ArrayList<>();

        String defaultLanguageUSEnglish = "";
        int defaultSelectionIndex = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Locale locale : textToSpeech.getAvailableLanguages()) {
                if (locale.getLanguage().equals("en") && locale.getCountry().equals("US")) {
                    defaultLanguageUSEnglish = locale.getDisplayName();
                }
                engineAvailableLanguages.add(locale.getDisplayName());
            }
            Collections.sort(engineAvailableLanguages);
            if (engineAvailableLanguages.contains(defaultLanguageUSEnglish)) {
                defaultSelectionIndex = engineAvailableLanguages.indexOf(defaultLanguageUSEnglish);
            }
        }

        setupLanguageSelector((Spinner) promptsView.findViewById(R.id.vocabLanguageSpinner),
                engineAvailableLanguages, category, defaultSelectionIndex);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

    }

    private void setupLanguageSelector(Spinner spinner, List<String> languages,
                                       final String category, int defaultSelectionPos){

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, languages);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);
        final SharedPreferences sharedPreferences = getContext()
                .getSharedPreferences(getContext().getResources().getString(R.string.sharedPrefSpeechFile), Context.MODE_PRIVATE);
        spinner.setSelection(sharedPreferences.getInt(category, defaultSelectionPos));


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(category, position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}
