package charlesli.com.personalvocabbuilder.controller;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;



public class ReviewSession extends AppCompatActivity {

    public static final int VOCAB_TO_DEF_REVIEW_MODE = 0;
    public static final int DEF_TO_VOCAB_REVIEW_MODE = 1;
    public static final int MIX_REVIEW_MODE = 2;
    public static final int RANDOM_REVIEW_TYPE = 0;
    public static final int MOST_DIFFICULT_REVIEW_TYPE = 1;
    public static final int MOST_RECENT_REVIEW_TYPE = 2;
    private static final int DIFFICULT = 0;
    private static final int FAMILIAR = 1;
    private static final int EASY = 2;
    private static final int PERFECT = 3;
    private int mReviewMode;
    private String mReviewCategory;
    private int mReviewNumOfVocab;
    private int mReviewType;
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
    private Button mAgaLvlButton;
    private ProgressBar mReviewProgressBar;
    private Cursor mCursor;
    private VocabDbHelper mDbHelper = VocabDbHelper.getDBHelper(ReviewSession.this);
    private Random mRandom = new Random();
    private ArrayList<Integer> mTracker = new ArrayList<Integer>();

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
        mReviewNumOfVocab = intent.getIntExtra("NumOfVocab", 0);
        mReviewType = intent.getIntExtra("Type", RANDOM_REVIEW_TYPE);

        mTopTextView = (TextView) findViewById(R.id.topTextView);
        mBottomTextView = (TextView) findViewById(R.id.bottomTextView);
        mRevealButton = (Button) findViewById(R.id.revealButton);
        mDifLvlButton = (Button) findViewById(R.id.lvl_difficult_button);
        mFamLvlButton = (Button) findViewById(R.id.lvl_familiar_button);
        mEasLvlButton = (Button) findViewById(R.id.lvl_easy_button);
        mPerLvlButton = (Button) findViewById(R.id.lvl_perfect_button);
        mAgaLvlButton = (Button) findViewById(R.id.lvl_again_button);
        mReviewProgressBar = (ProgressBar) findViewById(R.id.reviewProgressBar);

        mCursor = mDbHelper.getVocabCursor(mReviewCategory);
        mReviewProgressBar.setMax(mReviewNumOfVocab);

        loadVocabInRandomOrder();
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
        String word = mCursor.getString(mCursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_VOCAB));
        String definition = mCursor.getString(mCursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_DEFINITION));
        final int curLevel = mCursor.getInt(mCursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_LEVEL));

        if (mReviewMode == VOCAB_TO_DEF_REVIEW_MODE) {
            mTopTextView.setText(word);
            mBottomTextView.setText(definition);
        }
        else if (mReviewMode == DEF_TO_VOCAB_REVIEW_MODE) {
            mTopTextView.setText(definition);
            mBottomTextView.setText(word);
        }
        else {
            if (mRandom.nextBoolean()) {
                mTopTextView.setText(word);
                mBottomTextView.setText(definition);
            }
            else {
                mTopTextView.setText(definition);
                mBottomTextView.setText(word);
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

        // Which row to update, based on the ID
        String selectionWordBank = VocabDbContract.COLUMN_NAME_VOCAB + " = ? AND " +
                VocabDbContract.COLUMN_NAME_DEFINITION + " = ?";
        String word = mCursor.getString(mCursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_VOCAB));
        String definition = mCursor.getString(mCursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_DEFINITION));
        String[] selectionArgsWordBank = {word, definition};

        db.update(
                VocabDbContract.TABLE_NAME_MY_VOCAB,
                values,
                selectionWordBank,
                selectionArgsWordBank
        );
        // If this is not last word to be reviewed
        if (mTracker.size() < mReviewNumOfVocab) {
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


}
