package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.controller.GoogleTranslate;
import charlesli.com.personalvocabbuilder.controller.JSONParser;
import charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static android.content.Context.MODE_PRIVATE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.DEFAULT_TARGET_LANGUAGE_ENGLISH;
import static charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions.DETECT_LANGUAGE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.DATE_ASC;

/**
 * Created by charles on 2017-01-21.
 */

public class AddVocabDialog extends CustomDialog {

    private EditText vocabInput;
    private EditText definitionInput;
    private ProgressBar progressBar;

    public AddVocabDialog(final Context context, final VocabDbHelper dbHelper,
                          final VocabCursorAdapter cursorAdapter, final String category) {
        super(context);

        setTitle("Add Vocab");

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_add_vocab, null);
        vocabInput = (EditText) promptsView.findViewById(R.id.vocabInput);
        definitionInput = (EditText) promptsView.findViewById(R.id.definitionInput);
        progressBar = (ProgressBar) promptsView.findViewById(R.id.progressBar);
        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String vocab = vocabInput.getText().toString();
                String definition = definitionInput.getText().toString();
                if (dbHelper.checkIfVocabExists(vocab, definition)) {
                    DuplicateVocabEntryDialog alertDialog =
                            new DuplicateVocabEntryDialog(getContext(), dbHelper, cursorAdapter,
                                    category, vocab, definition);
                    alertDialog.show();
                }
                else {
                    dbHelper.insertVocab(category, vocab, definition, 0);
                    if (!category.equals(VocabDbContract.CATEGORY_NAME_MY_WORD_BANK)) {
                        dbHelper.insertVocab(VocabDbContract.CATEGORY_NAME_MY_WORD_BANK, vocab, definition, 0);
                    }
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("Sort Order", MODE_PRIVATE);
                    String orderBy = sharedPreferences.getString(category, DATE_ASC);

                    Cursor cursor = dbHelper.getVocabCursor(category, orderBy);
                    cursorAdapter.changeCursor(cursor);
                }
            }
        });
        setButton(BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // setButton automatically closes dialog
        // therefore need to perform translation in getButton after dialog is shown
        setButton(BUTTON_NEUTRAL, "Translate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
    }

    public void setUpTranslationButtonAfterShowDialog() {
        getButton(BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vocab = vocabInput.getText().toString();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("Translation", MODE_PRIVATE);
                int sourcePos = sharedPreferences.getInt("Source", DETECT_LANGUAGE);
                int targetPos = sharedPreferences.getInt("Target", DEFAULT_TARGET_LANGUAGE_ENGLISH);

                String source = LanguageOptions.FROM_LANGUAGE_CODE[sourcePos];
                String target = LanguageOptions.TO_LANGUAGE_CODE[targetPos];

                if (isNetworkAvailable()) {
                    String APIKey = getContext().getString(R.string.translateKey);
                    GoogleTranslate googleTranslate = new GoogleTranslate(progressBar, APIKey);
                    googleTranslate.setListener(new GoogleTranslate.Listener() {
                        @Override
                        public void onTaskResult(String string) {
                            if (string != null) {
                                JSONParser jsonParser = new JSONParser();
                                String translatedText = jsonParser.parseJSONForTranslation(string);
                                definitionInput.setText(Html.fromHtml(translatedText));
                            }
                            else {
                                Toast.makeText(getContext(), "Sorry, the translation operation did not go through. Please try again.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    googleTranslate.execute(vocab, source, target);
                }
                else {
                    NetworkUnavailableDialog dialog = new NetworkUnavailableDialog(getContext());
                    dialog.show();
                    dialog.changeButtonsToAppIconColor();
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }
}
