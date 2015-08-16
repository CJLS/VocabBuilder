package charlesli.com.personalvocabbuilder;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class ReviewActivity extends ActionBarActivity {

    private Button mWordDefinitionButton;
    private Button mDefinitionWordButton;

    private Cursor mCursor;
    private VocabDbHelper mDbHelper = new VocabDbHelper(this);

    private static final int WORDTODEF = 0;
    private static final int DEFTOWORD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        mWordDefinitionButton = (Button) findViewById(R.id.word_definition_button);
        mDefinitionWordButton = (Button) findViewById(R.id.definition_word_button);

        mCursor = mDbHelper.getCursorMyVocab(mDbHelper);

        mWordDefinitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCursor.getCount() > 0) {
                    startReview(WORDTODEF);
                }
                else {
                    Toast.makeText(ReviewActivity.this, "Add words to My Vocab first", Toast.LENGTH_LONG).show();
                }

            }
        });

        mDefinitionWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCursor.getCount() > 0) {
                    startReview(DEFTOWORD);
                }
                else {
                    Toast.makeText(ReviewActivity.this, "Add words to My Vocab first", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private  void startReview(int reviewOption) {
        Intent intent = new Intent(this, WordDefinitionActivity.class);
        intent.putExtra(getString(R.string.review_option_selected), reviewOption);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
