package charlesli.com.personalvocabbuilder.controller;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;
import charlesli.com.personalvocabbuilder.ui.SpeechLimitDialog;

import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.DATE_ASC;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.DATE_DESC;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.LEVEL_ASC;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.LEVEL_DESC;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.RANDOM;


public class ReviewSession extends AppCompatActivity {

    public static final int VOCAB_TO_DEF_REVIEW_MODE = 0;
    public static final int DEF_TO_VOCAB_REVIEW_MODE = 1;
    public static final int MIX_REVIEW_MODE = 2;
    public static final int RANDOM_REVIEW_TYPE = 0;
    public static final int MOST_FAMILIAR_REVIEW_TYPE = 1;
    public static final int LEAST_FAMILIAR_REVIEW_TYPE = 2;
    public static final int MOST_RECENT_REVIEW_TYPE = 3;
    public static final int LEAST_RECENT_REVIEW_TYPE = 4;
    public static final int DIFFICULT = 0;
    public static final int FAMILIAR = 1;
    public static final int EASY = 2;
    public static final int PERFECT = 3;
    private int mReviewMode;
    private String mReviewCategory;
    private int mDifficultCount = 0;
    private int mFamiliarCount = 0;
    private int mEasyCount = 0;
    private int mPerfectCount = 0;
    private int mMoreFamiliarVocabCount = 0;
    private TextView mTopTextView;
    private TextView mBottomTextView;
    private Button mRevealButton;
    private Button mDifLvlButton;
    private Button mFamLvlButton;
    private Button mEasLvlButton;
    private Button mPerLvlButton;
    private ImageView mAgaLvlButton;
    private ProgressBar mReviewProgressBar;
    private ImageView mSpeaker;
    private Cursor mCursor;
    private VocabDbHelper mDbHelper = VocabDbHelper.getDBHelper(ReviewSession.this);
    private Random mRandom = new Random();
    private ArrayList<Integer> mTracker = new ArrayList<Integer>();
    private CustomTTS textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_session);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        }

        Intent intent = getIntent();

        mReviewMode = intent.getIntExtra("Mode", VOCAB_TO_DEF_REVIEW_MODE);
        mReviewCategory = intent.getStringExtra("Category");
        int mReviewNumOfVocab = intent.getIntExtra("NumOfVocab", 0);
        int mReviewType = intent.getIntExtra("Type", RANDOM_REVIEW_TYPE);

        textToSpeech = new CustomTTS(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    String selectedLocaleDisplayName = mDbHelper.getCategoryLocaleDisplayName(mReviewCategory);
                    HashMap<String, Locale> displayNameToLocaleMapping = textToSpeech.getSupportedDisplayNameToLocaleMapping();

                    int result = textToSpeech.setLanguage(displayNameToLocaleMapping.get(selectedLocaleDisplayName));

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(ReviewSession.this, "Please enable internet to download the selected language voice data", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String s) {
                                SharedPreferences sharedPreferencesTTS =
                                        getSharedPreferences(getString(R.string.ttsMonthlyLimitPref), MODE_PRIVATE);
                                boolean isSubscribed = sharedPreferencesTTS.getBoolean(getString(R.string.isSubscribed), false);
                                int remainingTTSQuota = sharedPreferencesTTS.getInt(getString(R.string.remainingTTSQuota), 60);
                                SharedPreferences.Editor editor = sharedPreferencesTTS.edit();
                                if (!isSubscribed) {
                                    remainingTTSQuota -= 1;
                                    editor.putInt(getString(R.string.remainingTTSQuota), remainingTTSQuota);
                                    editor.apply();
                                }
                            }

                            @Override
                            public void onDone(String s) {

                            }

                            @Override
                            public void onError(String s) {
                                Toast.makeText(ReviewSession.this, "Language voice data might not be downloaded yet. Please enable internet when a new language is selected", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }, "com.google.android.tts");

        mTopTextView = (TextView) findViewById(R.id.topTextView);
        mBottomTextView = (TextView) findViewById(R.id.bottomTextView);
        mRevealButton = (Button) findViewById(R.id.revealButton);
        mDifLvlButton = (Button) findViewById(R.id.lvl_difficult_button);
        mFamLvlButton = (Button) findViewById(R.id.lvl_familiar_button);
        mEasLvlButton = (Button) findViewById(R.id.lvl_easy_button);
        mPerLvlButton = (Button) findViewById(R.id.lvl_perfect_button);
        mAgaLvlButton = (ImageView) findViewById(R.id.lvl_again_button);
        mReviewProgressBar = (ProgressBar) findViewById(R.id.reviewProgressBar);
        mSpeaker = (ImageView) findViewById(R.id.pronounceVocab);


        switch (mReviewType) {
            case RANDOM_REVIEW_TYPE:
                mCursor = mDbHelper.getVocabCursor(mReviewCategory, RANDOM, mReviewNumOfVocab);
                break;
            case MOST_FAMILIAR_REVIEW_TYPE:
                mCursor = mDbHelper.getVocabCursor(mReviewCategory, LEVEL_DESC, mReviewNumOfVocab);
                break;
            case LEAST_FAMILIAR_REVIEW_TYPE:
                mCursor = mDbHelper.getVocabCursor(mReviewCategory, LEVEL_ASC, mReviewNumOfVocab);
                break;
            case MOST_RECENT_REVIEW_TYPE:
                mCursor = mDbHelper.getVocabCursor(mReviewCategory, DATE_DESC, mReviewNumOfVocab);
                break;
            case LEAST_RECENT_REVIEW_TYPE:
                mCursor = mDbHelper.getVocabCursor(mReviewCategory, DATE_ASC, mReviewNumOfVocab);
                break;
            default:
                mCursor = mDbHelper.getVocabCursor(mReviewCategory, RANDOM, mReviewNumOfVocab);
        }

        mReviewProgressBar.setMax(mCursor.getCount());
        loadVocabInRandomOrder();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
    }

    private void loadVocabInRandomOrder() {
        int numOfRows = mCursor.getCount();
        int randomNum = mRandom.nextInt(numOfRows);
        // Prevent a repeated number from being generated
        while (mTracker.contains(randomNum)) {
            randomNum = mRandom.nextInt(numOfRows);
        }

        mCursor.moveToPosition(randomNum);
        // Get word and definition from Desired Random Row
        final String word = mCursor.getString(mCursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_VOCAB));
        String definition = mCursor.getString(mCursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_DEFINITION));
        final int curLevel = mCursor.getInt(mCursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_LEVEL));

        mSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textToSpeech != null) {
                    SharedPreferences sharedPreferencesTTS =
                            getSharedPreferences(getString(R.string.ttsMonthlyLimitPref), MODE_PRIVATE);
                    boolean isSubscribed = sharedPreferencesTTS.getBoolean(getString(R.string.isSubscribed), false);
                    int remainingTTSQuota = sharedPreferencesTTS.getInt(getString(R.string.remainingTTSQuota), 60);
                    if (isSubscribed || (remainingTTSQuota > 0)) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "1");

                        } else {
                            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                    else {
                        SpeechLimitDialog dialog = new SpeechLimitDialog(ReviewSession.this);
                        dialog.show();
                        dialog.changeButtonsToAppIconColor();
                    }
                }
                else {
                    Toast.makeText(ReviewSession.this, "Sorry, the speech engine is currently unavailable.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (mReviewMode == VOCAB_TO_DEF_REVIEW_MODE) {
            mTopTextView.setText(word);
            mBottomTextView.setText(definition);
            mSpeaker.setVisibility(View.VISIBLE);
        }
        else if (mReviewMode == DEF_TO_VOCAB_REVIEW_MODE) {
            mTopTextView.setText(definition);
            mBottomTextView.setText(word);
            mSpeaker.setVisibility(View.INVISIBLE);
        }
        else {
            if (mRandom.nextBoolean()) {
                mTopTextView.setText(word);
                mBottomTextView.setText(definition);
                mSpeaker.setVisibility(View.VISIBLE);
            }
            else {
                mTopTextView.setText(definition);
                mBottomTextView.setText(word);
                mSpeaker.setVisibility(View.INVISIBLE);
            }
        }

        mDifLvlButton.setVisibility(View.INVISIBLE);
        mFamLvlButton.setVisibility(View.INVISIBLE);
        mEasLvlButton.setVisibility(View.INVISIBLE);
        mPerLvlButton.setVisibility(View.INVISIBLE);
        mAgaLvlButton.setVisibility(View.INVISIBLE);
        mBottomTextView.setVisibility(View.INVISIBLE);
        mRevealButton.setVisibility(View.VISIBLE);

        final int finalRandomNum = randomNum;
        mRevealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRevealButton.setVisibility(View.INVISIBLE);
                mSpeaker.setVisibility(View.VISIBLE);

                mDifLvlButton.setVisibility(View.VISIBLE);
                mFamLvlButton.setVisibility(View.VISIBLE);
                mEasLvlButton.setVisibility(View.VISIBLE);
                mPerLvlButton.setVisibility(View.VISIBLE);
                mAgaLvlButton.setVisibility(View.VISIBLE);
                mBottomTextView.setVisibility(View.VISIBLE);

                mDifLvlButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDifficultCount++;
                        mTracker.add(finalRandomNum);
                        selectVocabFamiliarityLevel(DIFFICULT);
                    }
                });
                mFamLvlButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFamiliarCount++;
                        mTracker.add(finalRandomNum);
                        if (FAMILIAR > curLevel) {
                            mMoreFamiliarVocabCount++;
                        }
                        selectVocabFamiliarityLevel(FAMILIAR);
                    }
                });
                mEasLvlButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mEasyCount++;
                        mTracker.add(finalRandomNum);
                        if (EASY > curLevel) {
                            mMoreFamiliarVocabCount++;
                        }
                        selectVocabFamiliarityLevel(EASY);
                    }
                });
                mPerLvlButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPerfectCount++;
                        mTracker.add(finalRandomNum);
                        if (PERFECT > curLevel) {
                            mMoreFamiliarVocabCount++;
                        }
                        selectVocabFamiliarityLevel(PERFECT);
                    }
                });
                mAgaLvlButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadVocabInRandomOrder();
                    }
                });
            }
        });

    }

    private void selectVocabFamiliarityLevel(int level) {
        mReviewProgressBar.incrementProgressBy(1);
        // Update level information of the word to the SQLite database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(VocabDbContract.COLUMN_NAME_LEVEL, level);
        values.put(VocabDbContract.COLUMN_NAME_REVIEWED_AT, getDateTime());

        // Which row to update, based on the ID
        String selection = VocabDbContract.COLUMN_NAME_VOCAB + " = ? AND " +
                VocabDbContract.COLUMN_NAME_DEFINITION + " = ?";
        String word = mCursor.getString(mCursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_VOCAB));
        String definition = mCursor.getString(mCursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_DEFINITION));
        String[] selectionArgs = {word, definition};

        db.update(
                VocabDbContract.TABLE_NAME_MY_VOCAB,
                values,
                selection,
                selectionArgs
        );
        // If this is not last word to be reviewed
        if (mTracker.size() < mCursor.getCount()) {
            // load the next vocab to be reviewed
            loadVocabInRandomOrder();

        }
        // If this is last row
        else {
            // Exit the interface and go back to Review
            mTracker.clear();
            finish();

            // Review Result
            Intent intent = new Intent(this, ReviewResult.class);
            intent.putExtra(getString(R.string.difficultCount), mDifficultCount);
            intent.putExtra(getString(R.string.familiarCount), mFamiliarCount);
            intent.putExtra(getString(R.string.easyCount), mEasyCount);
            intent.putExtra(getString(R.string.perfectCount), mPerfectCount);
            intent.putExtra(getString(R.string.moreFamiliarVocabCount), mMoreFamiliarVocabCount);
            startActivity(intent);

        }
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


}
