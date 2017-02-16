package charlesli.com.personalvocabbuilder.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

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

        // Get data for screen from ReviewSession through Intents
    }

}
