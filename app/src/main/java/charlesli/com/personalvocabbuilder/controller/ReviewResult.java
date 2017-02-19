package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;

import charlesli.com.personalvocabbuilder.R;

public class ReviewResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_result);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.result_toolbar);
        setSupportActionBar(myToolbar);

        Button continueButton = (Button) findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        int difficultPercent = intent.getIntExtra(getString(R.string.difficultPercent), 0);
        int familiarPercent = intent.getIntExtra(getString(R.string.familiarPercent), 0);
        int easyPercent = intent.getIntExtra(getString(R.string.easyPercent), 0);
        int perfectPercent = intent.getIntExtra(getString(R.string.perfectPercent), 0);
        int moreFamiliarVocabCount = intent.getIntExtra(getString(R.string.moreFamiliarVocabCount), 0);

        setReviewProgress(difficultPercent, familiarPercent, easyPercent, perfectPercent, moreFamiliarVocabCount);
        setDayStreak();
    }

    private void setReviewProgress(int difficultPercent, int familiarPercent, int easyPercent, int perfectPercent, int moreFamiliarVocabCount) {
        ProgressBar difficultProgress = (ProgressBar) findViewById(R.id.difficultProgress);
        ProgressBar familiarProgress = (ProgressBar) findViewById(R.id.familiarProgress);
        ProgressBar easyProgress = (ProgressBar) findViewById(R.id.easyProgress);
        ProgressBar perfectProgress = (ProgressBar) findViewById(R.id.perfectProgress);
        TextView moreFamiliarVocabCountTV = (TextView) findViewById(R.id.vocabImprovementText);

        difficultProgress.setProgress(difficultPercent);
        familiarProgress.setProgress(familiarPercent);
        easyProgress.setProgress(easyPercent);
        perfectProgress.setProgress(perfectPercent);
        if (moreFamiliarVocabCount == 0) {
            moreFamiliarVocabCountTV.setText("Keep up the good work!");
        } else {
            moreFamiliarVocabCountTV.setText("You are more familiar with " + moreFamiliarVocabCount + " vocab!");
        }
    }

    private void setDayStreak() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.dayStreakSharedPreferences), MODE_PRIVATE);
        int yearLastReview = sharedPreferences.getInt(getString(R.string.yearKeySP), 2017);
        int dayOfYearLastReview = sharedPreferences.getInt(getString(R.string.dayOfYearKeySP), 1);
        int dayStreakCount = sharedPreferences.getInt(getString(R.string.dayStreakCountKeySP), 0);

        Calendar calendar = Calendar.getInstance();
        int yearToday = calendar.get(Calendar.YEAR);
        int dayOfYearToday = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        int yearYesterday = calendar.get(Calendar.YEAR);
        int dayOfYearYesterday = calendar.get(Calendar.DAY_OF_YEAR);

        if (yearToday == yearLastReview && dayOfYearToday == dayOfYearLastReview) {
            // Don't update dayStreakCount
        }
        else if (yearYesterday == yearLastReview && dayOfYearYesterday == dayOfYearLastReview) {
            dayStreakCount++;
        }
        else {
            dayStreakCount = 1;
        }

        TextView dayStreakCountTV = (TextView) findViewById(R.id.dayStreakNum);
        dayStreakCountTV.setText(String.valueOf(dayStreakCount));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.yearKeySP), yearToday);
        editor.putInt(getString(R.string.dayOfYearKeySP), dayOfYearToday);
        editor.putInt(getString(R.string.dayStreakCountKeySP), dayStreakCount);
        editor.apply();
    }

}
