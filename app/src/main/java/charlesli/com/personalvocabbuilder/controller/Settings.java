package charlesli.com.personalvocabbuilder.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import charlesli.com.personalvocabbuilder.R;

import static charlesli.com.personalvocabbuilder.controller.Subscription.SKU_MONTHLY_TTS;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.DEFAULT_TARGET_LANGUAGE_ENGLISH;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.DETECT_LANGUAGE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.FROM_LANGUAGE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.TO_LANGUAGE;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences sharedPreferencesTTS =
                getSharedPreferences(getString(R.string.ttsMonthlyLimitPref), MODE_PRIVATE);
        boolean isSubscribed = sharedPreferencesTTS.getBoolean(getString(R.string.isSubscribed), false);
        String subscribedTTS = sharedPreferencesTTS.getString(getString(R.string.subscribedTTS), "");
        if (isSubscribed) {
            TextView accountType = (TextView) findViewById(R.id.accountTypeInfoTV);
            if (subscribedTTS.equals(SKU_MONTHLY_TTS)) {
                accountType.setText("Monthly Plan: Unlimited text-to-speech");
            }
            else {
                accountType.setText("Yearly Plan: Unlimited text-to-speech");
            }
        }


        SharedPreferences sharedPreferencesTranslation = getSharedPreferences(getResources().getString(R.string.sharedPrefTranslationFile), Context.MODE_PRIVATE);
        int sourceLanguagePos = sharedPreferencesTranslation.getInt(getString(R.string.sharedPrefTranslationSourceKey), DETECT_LANGUAGE);
        int targetLanguagePos = sharedPreferencesTranslation.getInt(getString(R.string.sharedPrefTranslationTargetKey), DEFAULT_TARGET_LANGUAGE_ENGLISH);

        setupLanguageSelector((Spinner) findViewById(R.id.translateFromSpinner),
                FROM_LANGUAGE, true, sourceLanguagePos);

        setupLanguageSelector((Spinner) findViewById(R.id.translateToSpinner),
                TO_LANGUAGE, false, targetLanguagePos);
    }

    private void setupLanguageSelector(Spinner spinner, String[] languages,
                                       final boolean isSource, int currentSelection){

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, languages);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(currentSelection);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.sharedPrefTranslationFile), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (isSource) {
                    editor.putInt(getString(R.string.sharedPrefTranslationSourceKey), position);
                }
                else {
                    editor.putInt(getString(R.string.sharedPrefTranslationTargetKey), position);
                }
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
