package charlesli.com.personalvocabbuilder.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-01-01.
 */

public class ReviewDialog extends AlertDialog {

    public ReviewDialog(Context context) {
        super(context);

        setTitle("Review Vocab");

        VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(context);
        Cursor cursor = dbHelper.getVocabCursor(VocabDbContract.CATEGORY_NAME_MY_VOCAB);
        final Integer maxRow = cursor.getCount();
        int reviewNumOfWords = maxRow;

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_review, null);

        final TextView numText = (TextView) promptsView.findViewById(R.id.numberText);
        Spinner spinner = (Spinner) promptsView.findViewById(R.id.spinner);
        final RadioButton wordDef = (RadioButton) promptsView.findViewById(R.id.wordDef);
        final RadioButton defWord = (RadioButton) promptsView.findViewById(R.id.defWord);
        final SeekBar seekBar = (SeekBar) promptsView.findViewById(R.id.seekBar);

    }
}
