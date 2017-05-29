package charlesli.com.personalvocabbuilder.sqlDatabase;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import charlesli.com.personalvocabbuilder.R;

/**
 * Created by Li on 2015/4/17.
 */
public class VocabCursorAdapter extends CursorAdapter {

    private static final int DIFFICULT = 0;
    private static final int FAMILIAR = 1;
    private static final int EASY = 2;
    private static final int PERFECT = 3;

    public List<Integer> selectedItemsPositions;
    private TextToSpeech textToSpeech;


    public VocabCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, 0);

        selectedItemsPositions = new ArrayList<>();

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        for (Locale locale : textToSpeech.getAvailableLanguages()) {
                            Log.d("Tests:", "-------------------");
                            Log.d("Test Country:", locale.getCountry());
                            Log.d("Test Display Country:", locale.getDisplayCountry());
                            Log.d("Test Language:", locale.getLanguage());
                            Log.d("Test Display Language:", locale.getDisplayLanguage());
                            Log.d("Test Display Name:", locale.getDisplayName());
                            Log.d("Test Display Script:", locale.getDisplayScript());
                            Log.d("Test Display Variant:", locale.getDisplayVariant());
                        }
                    }
                }
            }
        }, "com.google.android.tts");

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vocab, parent, false);
        CheckBox box = (CheckBox) view.findViewById(R.id.editCheckbox);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int position = (int) compoundButton.getTag();
                if (b) {
                    //check whether its already selected or not
                    if (!selectedItemsPositions.contains(position))
                        selectedItemsPositions.add(position);
                } else {
                    //remove position if unchecked checked item
                    selectedItemsPositions.remove((Object) position);
                }
            }
        });
        return view;
    }


    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        final TextView vocabNameTV = (TextView) view.findViewById(R.id.vocabName);
        TextView vocabDefinitionTV = (TextView) view.findViewById(R.id.vocabDefinition);
        ImageView vocabLevel = (ImageView) view.findViewById(R.id.vocabLevel);

        final String vocab = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_VOCAB));
        String definition = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_DEFINITION));
        int level = cursor.getInt(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_LEVEL));

        vocabNameTV.setText(vocab);
        vocabDefinitionTV.setText(definition);

        if (level == DIFFICULT) {
            vocabLevel.setImageResource(R.drawable.level_difficult_orange);
        }
        else if (level == FAMILIAR) {
            vocabLevel.setImageResource(R.drawable.level_familiar_orange);
        }
        else if (level == EASY) {
            vocabLevel.setImageResource(R.drawable.level_easy_orange);
        }
        else if (level == PERFECT) {
            vocabLevel.setImageResource(R.drawable.level_perfect_orange);
        }

        CheckBox box = (CheckBox) view.findViewById(R.id.editCheckbox);
        box.setTag(cursor.getPosition());

        if (selectedItemsPositions.contains(cursor.getPosition()))
            box.setChecked(true);
        else
            box.setChecked(false);

        ImageView speaker = (ImageView) view.findViewById(R.id.vocabSpeaker);
        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 21) {
                    textToSpeech.speak(vocab, TextToSpeech.QUEUE_FLUSH, null, "1");

                }
                else {
                    textToSpeech.speak(vocab, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }
}
