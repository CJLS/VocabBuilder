package charlesli.com.personalvocabbuilder.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;
import charlesli.com.personalvocabbuilder.ui.TimePickerFragment;

import static charlesli.com.personalvocabbuilder.controller.ReviewSession.VOCAB_TO_DEF_REVIEW_MODE;
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

        setUpDailyReviewStudyTime((TextView) findViewById(R.id.reminderTimeInfo));

        setUpNotificationSwitch((SwitchCompat) findViewById(R.id.dailyReviewCompatSwitch));

        setUpCategorySpinner((Spinner) findViewById(R.id.dailyReviewCategorySpinner), categoryCursor);

        setUpStaticSpinner((Spinner) findViewById(R.id.dailyReviewTypeSpinner),
                R.array.review_type_array, getString(R.string.sharedPrefDailyReviewTypeKey));
        setUpStaticSpinner((Spinner) findViewById(R.id.dailyReviewModeSpinner),
                R.array.review_mode_array, getString(R.string.sharedPrefDailyReviewModeKey));
        setUpStaticSpinner((Spinner) findViewById(R.id.dailyReviewGoalSpinner),
                R.array.review_goal_array, getString(R.string.sharedPrefDailyReviewGoalKey));

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

    private void setUpDailyReviewStudyTime(TextView studyTime) {
        int hour = sharedPreferencesDailyReview.getInt(getString(R.string.sharedPrefDailyReviewStudyHourKey), 9);
        int minute = sharedPreferencesDailyReview.getInt(getString(R.string.sharedPrefDailyReviewStudyMinKey), 30);
        String periodOfDay = "AM";

        if (hour >= 12) {
            hour = hour - 12;
            periodOfDay = "PM";
        }
        if (hour == 0) {
            hour = 12;
        }

        studyTime.setText(hour + ":" + String.format(Locale.CANADA, "%02d", minute) + " " + periodOfDay);

        studyTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });
    }

    private void setUpNotificationSwitch(SwitchCompat switchCompat) {
        boolean isChecked = sharedPreferencesDailyReview.getBoolean(getString(R.string.sharedPrefDailyReviewSwitchKey), true);
        switchCompat.setChecked(isChecked);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                sharedPreferencesDailyReview.edit()
                        .putBoolean(getString(R.string.sharedPrefDailyReviewSwitchKey), isChecked).apply();
            }
        });
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

        String selectedCategory = sharedPreferencesDailyReview.getString(getString(R.string.sharedPrefDailyReviewCategoryKey), getString(R.string.my_word_bank));
        categoryCursor.moveToFirst();
        for (int currentPos = 0; currentPos < categoryCursor.getCount() - 1; currentPos++) {
            String category = categoryCursor.getString(categoryCursor.getColumnIndex(COLUMN_NAME_CATEGORY));
            if (category.equals(selectedCategory)) defaultPos = currentPos;
            categoryCursor.moveToNext();
        }

        spinner.setSelection(defaultPos);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryCursor.moveToPosition(position);
                String category = categoryCursor.getString(categoryCursor.getColumnIndex(COLUMN_NAME_CATEGORY));
                sharedPreferencesDailyReview.edit()
                        .putString(getString(R.string.sharedPrefDailyReviewCategoryKey), category).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpStaticSpinner(Spinner spinner, int selectionArrayResID, final String sharedPrefKey) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getBaseContext(), selectionArrayResID,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int selectedPos = 0;

        if (sharedPrefKey.equals(getString(R.string.sharedPrefDailyReviewTypeKey))) {
            String [] reviewTypeArray = getResources().getStringArray(R.array.review_type_array);
            // Default review type selection is 0 : Vocab -> Def
            String reviewType = sharedPreferencesDailyReview.getString(sharedPrefKey, reviewTypeArray[0]);
            selectedPos = Arrays.asList(reviewTypeArray).indexOf(reviewType);
            if (selectedPos < 0) selectedPos = 0;
        }
        else if (sharedPrefKey.equals(getString(R.string.sharedPrefDailyReviewModeKey))) {
            selectedPos = sharedPreferencesDailyReview.getInt(sharedPrefKey, VOCAB_TO_DEF_REVIEW_MODE);
        }
        else if (sharedPrefKey.equals(getString(R.string.sharedPrefDailyReviewGoalKey))) {
            selectedPos = sharedPreferencesDailyReview.getInt(sharedPrefKey, 0);
        }

        spinner.setSelection(selectedPos);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (sharedPrefKey.equals(getString(R.string.sharedPrefDailyReviewTypeKey))) {
                    String [] reviewTypeArray = getResources().getStringArray(R.array.review_type_array);
                    sharedPreferencesDailyReview.edit()
                            .putString(sharedPrefKey, reviewTypeArray[position]).apply();
                }
                else if (sharedPrefKey.equals(getString(R.string.sharedPrefDailyReviewModeKey))) {
                    sharedPreferencesDailyReview.edit()
                            .putInt(sharedPrefKey, position).apply();
                }
                else if (sharedPrefKey.equals(getString(R.string.sharedPrefDailyReviewGoalKey))) {
                    sharedPreferencesDailyReview.edit()
                            .putInt(sharedPrefKey, position).apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
