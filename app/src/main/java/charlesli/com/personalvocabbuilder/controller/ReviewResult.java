package charlesli.com.personalvocabbuilder.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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

        ProgressBar difficultProgress = (ProgressBar) findViewById(R.id.difficultProgress);
        ProgressBar familiarProgress = (ProgressBar) findViewById(R.id.familiarProgress);
        ProgressBar easyProgress = (ProgressBar) findViewById(R.id.easyProgress);
        ProgressBar perfectProgress = (ProgressBar) findViewById(R.id.perfectProgress);
        TextView moreFamiliarVocabCountTV = (TextView) findViewById(R.id.vocabImprovementText);

        Intent intent = getIntent();
        int difficultPercent = intent.getIntExtra(getString(R.string.difficultPercent), 0);
        int familiarPercent = intent.getIntExtra(getString(R.string.familiarPercent), 0);
        int easyPercent = intent.getIntExtra(getString(R.string.easyPercent), 0);
        int perfectPercent = intent.getIntExtra(getString(R.string.perfectPercent), 0);
        int moreFamiliarVocabCount = intent.getIntExtra(getString(R.string.moreFamiliarVocabCount), 0);

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

}
