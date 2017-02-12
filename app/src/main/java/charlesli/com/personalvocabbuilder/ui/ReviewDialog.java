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
import charlesli.com.personalvocabbuilder.controller.ReviewSession;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static charlesli.com.personalvocabbuilder.controller.ReviewSession.DEF_TO_VOCAB_REVIEW_MODE;
import static charlesli.com.personalvocabbuilder.controller.ReviewSession.MIX_REVIEW_MODE;
import static charlesli.com.personalvocabbuilder.controller.ReviewSession.VOCAB_TO_DEF_REVIEW_MODE;

/**
 * Created by charles on 2017-01-01.
 */

public class ReviewDialog extends CustomDialog {

    private boolean textChangeFromUser = true;

    public ReviewDialog(Context context, VocabDbHelper dbHelper) {
        super(context);

        setTitle("Review Vocab");

        final Cursor categoryCursor = dbHelper.getCategoryCursor();
        final int[] reviewMode = {VOCAB_TO_DEF_REVIEW_MODE};

        categoryCursor.moveToFirst();
        String firstCategoryInCategoryCursor =
                categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
        final String[] reviewCategory = {firstCategoryInCategoryCursor};

        Cursor cursor = dbHelper.getVocabCursor(firstCategoryInCategoryCursor);
        final int[] totalNum = {cursor.getCount()};
        final int[] reviewNum = {cursor.getCount()};

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_review, null);

        final EditText reviewNumET = (EditText) promptsView.findViewById(R.id.numberText);
        Spinner spinner = (Spinner) promptsView.findViewById(R.id.spinner);
        final RadioButton vocabDefReview = (RadioButton) promptsView.findViewById(R.id.vocabDefReview);
        final RadioButton defVocabReview = (RadioButton) promptsView.findViewById(R.id.defVocabReview);
        final RadioButton mixReview = (RadioButton) promptsView.findViewById(R.id.mixReview);
        final SeekBar seekBar = (SeekBar) promptsView.findViewById(R.id.seekBar);

        setUpReviewNumEditText(totalNum, reviewNum, reviewNumET, seekBar);
        setUpSpinner(categoryCursor, reviewNumET, spinner, seekBar, reviewCategory, totalNum, reviewNum);
        setUpRadioButtons(reviewMode, vocabDefReview, defVocabReview, mixReview);
        setUpSeekBar(totalNum, reviewNum, reviewNumET, seekBar);

        setView(promptsView);

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (reviewNum[0] == 0) {
                    Toast.makeText(getContext(), "There are no vocab to be reviewed", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getContext(), ReviewSession.class);
                    intent.putExtra("Mode", reviewMode[0]);
                    intent.putExtra("Category", reviewCategory[0]);
                    intent.putExtra("NumOfVocab", reviewNum[0]);
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

    private void setUpReviewNumEditText(final int[] totalNum, final int[] reviewNum,
                                        EditText reviewNumET, final SeekBar seekBar) {
        reviewNumET.setText(String.valueOf(reviewNum[0]));

        reviewNumET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (textChangeFromUser && !s.toString().equals("")) {
                    reviewNum[0] = Integer.parseInt(s.toString());
                    if (reviewNum[0] > totalNum[0]) {
                        reviewNum[0] = totalNum[0];
                    }
                    seekBar.setProgress(reviewNum[0]);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setUpSeekBar(final int[] totalNum, final int[] reviewNum,
                              final TextView numText, SeekBar seekBar) {
        seekBar.setMax(totalNum[0]);
        seekBar.setProgress(reviewNum[0]);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    reviewNum[0] = progress;
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

    private void setUpRadioButtons(final int[] reviewMode, final RadioButton wordDef,
                                   final RadioButton defWord, final RadioButton mix) {
        wordDef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordDef.setChecked(true);
                defWord.setChecked(false);
                mix.setChecked(false);
                reviewMode[0] = VOCAB_TO_DEF_REVIEW_MODE;
            }
        });

        defWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordDef.setChecked(false);
                defWord.setChecked(true);
                mix.setChecked(false);
                reviewMode[0] = DEF_TO_VOCAB_REVIEW_MODE;
            }
        });

        mix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordDef.setChecked(false);
                defWord.setChecked(false);
                mix.setChecked(true);
                reviewMode[0] = MIX_REVIEW_MODE;
            }
        });
    }

    private void setUpSpinner(final Cursor categoryCursor, final TextView numText, Spinner spinner,
                              final SeekBar seekBar, final String[] reviewCategory,
                              final int[] totalNum, final int[] reviewNum) {
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
                totalNum[0] = cursor.getCount();
                reviewNum[0] = cursor.getCount();
                textChangeFromUser = false;
                numText.setText(String.valueOf(reviewNum[0]));
                textChangeFromUser = true;
                seekBar.setMax(totalNum[0]);
                seekBar.setProgress(reviewNum[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
}
