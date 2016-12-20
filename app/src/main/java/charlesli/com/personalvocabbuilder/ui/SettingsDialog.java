package charlesli.com.personalvocabbuilder.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions;



public class SettingsDialog extends AlertDialog{


    private static final int DEFAULT_TARGET_LANGUAGE = 19;  // English


    public SettingsDialog(Context context) {
        super(context);

        setTitle("Translation Settings");

        View promptsView = LayoutInflater.from(getContext())
                            .inflate(R.layout.alert_dialog_settings, null);

        setupLanguageSelector((Spinner) promptsView.findViewById(R.id.spinnerTranslateFrom),
                LanguageOptions.FROM_LANGUAGE, "Source", LanguageOptions.DETECT_LANGUAGE);

        setupLanguageSelector((Spinner) promptsView.findViewById(R.id.spinnerTranslateTo),
                LanguageOptions.TO_LANGUAGE, "Target", DEFAULT_TARGET_LANGUAGE);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

    }



    private void setupLanguageSelector(Spinner spinner, String[] languages,
                                       final String sharedPrefKey, int defaultSelection){

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, languages);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);


        final SharedPreferences sharedPreferences = getContext()
                .getSharedPreferences("Translation", Context.MODE_PRIVATE);
        spinner.setSelection(sharedPreferences.getInt(sharedPrefKey, defaultSelection));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(sharedPrefKey, position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


}
