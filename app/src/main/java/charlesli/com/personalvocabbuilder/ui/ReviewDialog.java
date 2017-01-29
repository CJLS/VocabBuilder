package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.controller.Review;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static charlesli.com.personalvocabbuilder.controller.Review.DEFTOWORD;
import static charlesli.com.personalvocabbuilder.controller.Review.WORDTODEF;

/**
 * Created by charles on 2017-01-01.
 */

public class ReviewDialog extends CustomDialog {

    private boolean textChangeFromUser = true;

    public ReviewDialog(Context context, VocabDbHelper dbHelper) {
        super(context);

        setTitle("Review Vocab");

        final Cursor categoryCursor = dbHelper.getCategoryCursor();
        final int[] reviewMode = {WORDTODEF};

        categoryCursor.moveToFirst();
        String firstCategoryInCategoryCursor =
                categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
        final String[] reviewCategory = {firstCategoryInCategoryCursor};

        Cursor cursor = dbHelper.getVocabCursor(firstCategoryInCategoryCursor);
        final int[] reviewNumOfWords = {cursor.getCount()};

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_review, null);

        final EditText reviewNumET = (EditText) promptsView.findViewById(R.id.numberText);
        Spinner spinner = (Spinner) promptsView.findViewById(R.id.spinner);
        final RadioButton wordDef = (RadioButton) promptsView.findViewById(R.id.wordDef);
        final RadioButton defWord = (RadioButton) promptsView.findViewById(R.id.defWord);
        final SeekBar seekBar = (SeekBar) promptsView.findViewById(R.id.seekBar);

        setUpReviewNumEditText(reviewNumOfWords, reviewNumET, seekBar);
        setUpSpinner(categoryCursor, reviewNumET, spinner, seekBar, reviewCategory, reviewNumOfWords);
        setUpRadioButtons(reviewMode, wordDef, defWord);
        setUpSeekBar(reviewNumOfWords, reviewNumET, seekBar);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (reviewNumOfWords[0] == 0) {
                    Toast.makeText(getContext(), "There are no words to be reviewed", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getContext(), Review.class);
                    intent.putExtra("Mode", reviewMode[0]);
                    intent.putExtra("Category", reviewCategory[0]);
                    intent.putExtra("NumOfWords", reviewNumOfWords[0]);
                    getContext().startActivity(intent);
                }
            }
        });
        setButton(BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


    }

    private void setUpReviewNumEditText(final int[] reviewNumOfWords,
                                        EditText reviewNum, final SeekBar seekBar) {
        reviewNum.setText(String.valueOf(reviewNumOfWords[0]));

        reviewNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (textChangeFromUser && !s.toString().equals("")) {
                    int inputNum = Integer.parseInt(s.toString());
                    if (inputNum > reviewNumOfWords[0]) {
                        inputNum = reviewNumOfWords[0];
                    }
                    reviewNumOfWords[0] = inputNum;
                    seekBar.setProgress(inputNum);
                }
            }
        });
    }

    private void setUpSeekBar(final int[] reviewNumOfWords,
                              final TextView numText, SeekBar seekBar) {
        seekBar.setMax(reviewNumOfWords[0]);
        seekBar.setProgress(reviewNumOfWords[0]);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    reviewNumOfWords[0] = progress;
                    textChangeFromUser = false;
                    numText.setText(String.valueOf(progress));
                    textChangeFromUser = true;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setUpRadioButtons(final int[] reviewMode, final RadioButton wordDef, final RadioButton defWord) {
        wordDef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordDef.setChecked(true);
                defWord.setChecked(false);
                reviewMode[0] = WORDTODEF;
            }
        });

        defWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordDef.setChecked(false);
                defWord.setChecked(true);
                reviewMode[0] = DEFTOWORD;
            }
        });
    }

    private void setUpSpinner(final Cursor categoryCursor, final TextView numText, Spinner spinner,
                              final SeekBar seekBar, final String[] reviewCategory, final int[] reviewNumOfWords) {
        String[] from = {VocabDbContract.COLUMN_NAME_CATEGORY};
        int[] to = {android.R.id.text1};

        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getContext(), android.R.layout.simple_spinner_item,
                categoryCursor, from, to, 0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryCursor.moveToPosition(position);
                reviewCategory[0] = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
                VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(getContext());
                Cursor cursor = dbHelper.getVocabCursor(reviewCategory[0]);
                Integer maxRow = cursor.getCount();
                reviewNumOfWords[0] = maxRow;
                textChangeFromUser = false;
                numText.setText(String.valueOf(maxRow));
                textChangeFromUser = true;
                seekBar.setMax(maxRow);
                seekBar.setProgress(maxRow);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
}
