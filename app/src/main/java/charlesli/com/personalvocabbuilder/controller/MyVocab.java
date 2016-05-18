package charlesli.com.personalvocabbuilder.controller;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;


public class MyVocab extends CategoryItem implements SearchView.OnQueryTextListener{

    private VocabCursorAdapter mVocabAdapter;
    private ListView mVocabListView;
    private VocabDbHelper mDbHelper = VocabDbHelper.getDBHelper(MyVocab.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_vocab);

        /* Search
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
        }
        */

        mVocabListView = (ListView) findViewById(R.id.mVocabList);
        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mVocabListView.setEmptyView(emptyTextView);
        Cursor cursor = mDbHelper.getCursor(VocabDbContract.TABLE_NAME_MY_VOCAB);
        mVocabAdapter = new VocabCursorAdapter(this, cursor, 0);
        mVocabListView.setAdapter(mVocabAdapter);
        mVocabListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedVocab = (String) ((TextView) view.findViewById(R.id.vocabName)).getText();
                String selectedDefinition = (String) ((TextView) view.findViewById(R.id.vocabDefinition)).getText();
                editVocabAlertDialog(selectedVocab, selectedDefinition, id, mDbHelper,
                        VocabDbContract.TABLE_NAME_MY_VOCAB, mVocabAdapter);
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_vocab, menu);

        MenuItem search = menu.findItem(R.id.search_my_vocab_button);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_vocab_my_vocab_button) {
            addVocabAlertDialog(mDbHelper, VocabDbContract.TABLE_NAME_MY_VOCAB, mVocabAdapter);
        }
        else if (id == R.id.del_my_vocab_button) {
            deleteVocab(mDbHelper, VocabDbContract.TABLE_NAME_MY_VOCAB, mVocabAdapter);
        }
        else if (id == R.id.label_my_vocab_button) {
            selectTableToAddVocabTo(mVocabAdapter, mDbHelper, VocabDbContract.TABLE_NAME_MY_VOCAB);
        }
        else if (id == R.id.select_all_my_vocab_button) {
            selectAll(mVocabAdapter, mDbHelper, VocabDbContract.TABLE_NAME_MY_VOCAB);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Log.i("Search", s);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        Log.i("Search", s);
        return true;
    }
}
