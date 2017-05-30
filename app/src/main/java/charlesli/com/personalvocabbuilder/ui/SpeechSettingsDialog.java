package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
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

import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.DETECT_LANGUAGE;

/**
 * Created by charles on 2017-05-29.
 */

public class SpeechSettingsDialog extends CustomDialog {

    private TextToSpeech textToSpeech;

    public SpeechSettingsDialog(Context context, String category) {
        super(context);

        setTitle("Speech Settings");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_speech_settings, null);

        final ArrayList<String> engineAvailableLanguages = new ArrayList<>();

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        for (Locale locale : textToSpeech.getAvailableLanguages()) {
                            engineAvailableLanguages.add(locale.getDisplayName());
                        }
                        Collections.sort(engineAvailableLanguages);
                    }
                }
            }
        }, "com.google.android.tts");

        Collections.sort(engineAvailableLanguages);

        setupLanguageSelector((Spinner) promptsView.findViewById(R.id.vocabLanguageSpinner),
                engineAvailableLanguages, category, DETECT_LANGUAGE);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

    }

    private void setupLanguageSelector(Spinner spinner, List<String> languages,
                                       final String category, int defaultSelection){

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, languages);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);

        /* TODO: GETSTRING INSTEAD, CHECK STRING PATTERN FOR LANGUAGE?
        final SharedPreferences sharedPreferences = getContext()
                .getSharedPreferences(getContext().getResources().getString(R.string.sharedPrefSpeechFile), Context.MODE_PRIVATE);
        spinner.setSelection(sharedPreferences.getInt(category, defaultSelection));
        */

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(category, position);
                editor.apply();
                */
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}
