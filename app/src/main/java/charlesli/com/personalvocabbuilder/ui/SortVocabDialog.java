package charlesli.com.personalvocabbuilder.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static android.content.Context.MODE_PRIVATE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.DATE_ASC;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.DATE_DESC;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.VOCAB_ASC;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.VOCAB_DESC;

/**
 * Created by charles on 2017-01-22.
 */

public class SortVocabDialog extends CustomDialog {

    private String categoryName;
    private VocabDbHelper dbHelper;
    private VocabCursorAdapter vocabAdapter;

    public SortVocabDialog(Context context, String categoryName, VocabDbHelper dbHelper,
                           VocabCursorAdapter cursorAdapter) {
        super(context);

        this.categoryName = categoryName;
        this.dbHelper = dbHelper;
        this.vocabAdapter = cursorAdapter;

        setTitle("Sort By");

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_sort, null);
        setView(promptsView);

        final RadioButton rbDateAscending = (RadioButton) promptsView.findViewById(R.id.btDateAscending);
        final RadioButton rbDateDescending = (RadioButton) promptsView.findViewById(R.id.btDateDescending);
        final RadioButton rbVocabAscending = (RadioButton) promptsView.findViewById(R.id.btVocabAscending);
        final RadioButton rbVocabDescending = (RadioButton) promptsView.findViewById(R.id.btVocabDescending);

        rbDateAscending.setChecked(false);
        rbDateDescending.setChecked(false);
        rbVocabAscending.setChecked(false);
        rbVocabDescending.setChecked(false);

        final SharedPreferences sharedPreferences = getContext().getSharedPreferences("Sort Order", MODE_PRIVATE);
        String orderBy = sharedPreferences.getString(categoryName, DATE_ASC);

        if (orderBy.equals(DATE_ASC)) {
            rbDateAscending.setChecked(true);
        }
        else if (orderBy.equals(DATE_DESC)) {
            rbDateDescending.setChecked(true);
        }
        else if (orderBy.equals(VOCAB_ASC)) {
            rbVocabAscending.setChecked(true);
        }
        else if (orderBy.equals(VOCAB_DESC)) {
            rbVocabDescending.setChecked(true);
        }


        rbDateAscending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortByRadioButton(rbDateAscending, DATE_ASC, SortVocabDialog.this);
            }
        });

        rbDateDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortByRadioButton(rbDateDescending, DATE_DESC, SortVocabDialog.this);
            }
        });

        rbVocabAscending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortByRadioButton(rbVocabAscending, VOCAB_ASC, SortVocabDialog.this);
            }
        });

        rbVocabDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortByRadioButton(rbVocabDescending, VOCAB_DESC, SortVocabDialog.this);
            }
        });
    }

    private void setSortByRadioButton(RadioButton selectedButton, String orderBy, AlertDialog dialog) {
        selectedButton.setChecked(true);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("Sort Order", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(categoryName, orderBy);
        editor.apply();

        Cursor cursor = dbHelper.getVocabCursor(categoryName, orderBy);
        vocabAdapter.changeCursor(cursor);
        dialog.dismiss();
    }
}
