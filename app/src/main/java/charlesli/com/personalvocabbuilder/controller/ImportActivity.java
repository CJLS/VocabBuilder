package charlesli.com.personalvocabbuilder.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import charlesli.com.personalvocabbuilder.R;

public class ImportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        TextView exportFileName = (TextView) findViewById(R.id.exportFileName);
        Button selectFileButton = (Button) findViewById(R.id.selectFileButton);
        Button importButton = (Button) findViewById(R.id.importConfirmButton);
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        Button cancelButton = (Button) findViewById(R.id.importCancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
