package charlesli.com.personalvocabbuilder.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static charlesli.com.personalvocabbuilder.controller.Subscription.SKU_MONTHLY_TTS;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.DEFAULT_TARGET_LANGUAGE_ENGLISH;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.DETECT_LANGUAGE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.FROM_LANGUAGE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.TO_LANGUAGE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.COLUMN_NAME_CATEGORY;

public class Settings extends AppCompatActivity {

    Cursor categoryCursor;
    SharedPreferences sharedPreferencesDailyReview;

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

        sharedPreferencesDailyReview =
                getSharedPreferences(getString(R.string.sharedPrefDailyReviewFile), MODE_PRIVATE);

        VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(getBaseContext());
        categoryCursor = dbHelper.getCategoryCursor();

        setUpCategorySpinner((Spinner) findViewById(R.id.dailyReviewCategorySpinner), categoryCursor);
        setUpTypeSpinner((Spinner) findViewById(R.id.dailyReviewTypeSpinner));
        setUpModeSpinner((Spinner) findViewById(R.id.dailyReviewModeSpinner));
        setupLanguageSelector((Spinner) findViewById(R.id.translateFromSpinner),
                FROM_LANGUAGE, true, sourceLanguagePos);

        setupLanguageSelector((Spinner) findViewById(R.id.translateToSpinner),
                TO_LANGUAGE, false, targetLanguagePos);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (categoryCursor != null) categoryCursor.close();
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

    private void setUpCategorySpinner(Spinner spinner, final Cursor categoryCursor) {
        String[] from = {COLUMN_NAME_CATEGORY};
        int[] to = {android.R.id.text1};

        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getBaseContext(), android.R.layout.simple_spinner_item,
                categoryCursor, from, to, 0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        int defaultPos = 0;
        categoryCursor.moveToFirst();
        for (int currentPos = 0; currentPos < categoryCursor.getCount() - 1; currentPos++) {
            String category = categoryCursor.getString(categoryCursor.getColumnIndex(COLUMN_NAME_CATEGORY));
            if (category.equals("My Word Bank")) defaultPos = currentPos;
            categoryCursor.moveToNext();
        }

        defaultPos = sharedPreferencesDailyReview.getInt(getString(R.string.sharedPrefDailyReviewCategoryKey), defaultPos);

        spinner.setSelection(defaultPos);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferencesDailyReview.edit()
                        .putInt(getString(R.string.sharedPrefDailyReviewCategoryKey), position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpTypeSpinner(Spinner spinner) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.review_type_array,
                android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int defaultPos = sharedPreferencesDailyReview.getInt(getString(R.string.sharedPrefDailyReviewTypeKey), 0);

        spinner.setSelection(defaultPos);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferencesDailyReview.edit()
                        .putInt(getString(R.string.sharedPrefDailyReviewTypeKey), position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpModeSpinner(Spinner spinner) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.review_mode_array,
                android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int defaultPos = sharedPreferencesDailyReview.getInt(getString(R.string.sharedPrefDailyReviewModeKey), 0);

        spinner.setSelection(defaultPos);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferencesDailyReview.edit()
                        .putInt(getString(R.string.sharedPrefDailyReviewModeKey), position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
