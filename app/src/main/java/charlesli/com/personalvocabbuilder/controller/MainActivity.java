package charlesli.com.personalvocabbuilder.controller;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.LanguageOptions;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;
import charlesli.com.personalvocabbuilder.ui.SettingsDialog;


public class MainActivity extends AppCompatActivity {

    // Review Mode
    public static final int WORDTODEF = 0;
    public static final int DEFTOWORD = 1;
    private String reviewCategory = VocabDbContract.CATEGORY_NAME_MY_VOCAB;
    private int reviewMode = WORDTODEF;
    private int reviewNumOfWords = 0;
    private CategoryCursorAdapter mCategoryAdapter;
    private ListView mCategoryListView;
    private VocabDbHelper mDbHelper = VocabDbHelper.getDBHelper(MainActivity.this);
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        mCategoryListView = (ListView) findViewById(R.id.mainListView);
        Cursor cursor = mDbHelper.getCategoryCursor();
        mCategoryAdapter = new CategoryCursorAdapter(this, cursor, 0);
        mCategoryListView.setAdapter(mCategoryAdapter);

        mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor categoryCursor = mDbHelper.getCategoryCursor();
                categoryCursor.moveToPosition(position);
                String categoryName = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
                Intent intent = new Intent(MainActivity.this, MyVocab.class);
                intent.putExtra("Category", categoryName);
                startActivity(intent);
            }
        });

        mCategoryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor categoryCursor = mDbHelper.getCategoryCursor();
                categoryCursor.moveToPosition(position);
                String categoryName = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
                String categoryDesc = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_DESCRIPTION));
                editCategoryAlertDialog(categoryName, categoryDesc, mDbHelper, mCategoryAdapter);
                return true;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.categoryFAB);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createReviewDialog();
                }
            });
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCategoryAdapter.changeCursor(mDbHelper.getCategoryCursor());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_category_button) {
            createAddCategoryDialog();
        } else if (id == R.id.settings_button) {
            createSettingsDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void createSettingsDialog() {
        final AlertDialog dialog = new SettingsDialog(this);
        dialog.show();
        changeDialogButtonsColor(dialog);
    }

    private void changeDialogButtonsColor(AlertDialog dialog) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.app_icon_color));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.app_icon_color));
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(this, R.color.app_icon_color));
    }

    private void createAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Category");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText categoryNameInput = new EditText(this);
        categoryNameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        categoryNameInput.setHint("Name");
        layout.addView(categoryNameInput);

        final EditText categoryDescInput = new EditText(this);
        categoryDescInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        categoryDescInput.setHint("Description");
        layout.addView(categoryDescInput);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = categoryNameInput.getText().toString();
                String description = categoryDescInput.getText().toString();
                if (mDbHelper.checkIfCategoryExists(name)) {
                    Toast.makeText(MainActivity.this, name + " already exists", Toast.LENGTH_SHORT).show();
                } else {
                    mDbHelper.insertCategory(name, description);
                    mCategoryAdapter.changeCursor(mDbHelper.getCategoryCursor());
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();

        dialog.show();

        changeDialogButtonsColor(dialog);
    }

    private void createReviewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Review Vocab");

        VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(MainActivity.this);
        Cursor cursor = dbHelper.getVocabCursor(VocabDbContract.CATEGORY_NAME_MY_VOCAB);
        final Integer maxRow = cursor.getCount();
        reviewNumOfWords = maxRow;

        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.alert_dialog_review, null);

        final TextView numText = (TextView) promptsView.findViewById(R.id.numberText);
        Spinner spinner = (Spinner) promptsView.findViewById(R.id.spinner);
        final RadioButton wordDef = (RadioButton) promptsView.findViewById(R.id.wordDef);
        final RadioButton defWord = (RadioButton) promptsView.findViewById(R.id.defWord);
        final SeekBar seekBar = (SeekBar) promptsView.findViewById(R.id.seekBar);

        // TextView
        numText.setText(String.valueOf(maxRow));

        // Spinner
        String[] from = {VocabDbContract.COLUMN_NAME_CATEGORY};
        int[] to = {android.R.id.text1};
        final Cursor categoryCursor = mDbHelper.getCategoryCursor();
        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
                categoryCursor, from, to, 0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryCursor.moveToPosition(position);
                reviewCategory = categoryCursor.getString(categoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
                VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(MainActivity.this);
                Cursor cursor = dbHelper.getVocabCursor(reviewCategory);
                Integer maxRow = cursor.getCount();
                numText.setText(String.valueOf(maxRow));
                seekBar.setMax(maxRow);
                seekBar.setProgress(maxRow);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Radio Button
        wordDef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordDef.setChecked(true);
                defWord.setChecked(false);
                reviewMode = WORDTODEF;
            }
        });

        defWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordDef.setChecked(false);
                defWord.setChecked(true);
                reviewMode = DEFTOWORD;
            }
        });

        // SeekBar
        seekBar.setMax(maxRow);
        seekBar.setProgress(maxRow);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                numText.setText(String.valueOf(progress));
                reviewNumOfWords = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        builder.setView(promptsView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (reviewNumOfWords == 0) {
                    Toast.makeText(MainActivity.this, "There are no words to be reviewed", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, Review.class);
                    intent.putExtra("Mode", reviewMode);
                    intent.putExtra("Category", reviewCategory);
                    intent.putExtra("NumOfWords", reviewNumOfWords);
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();

        dialog.show();

        changeDialogButtonsColor(dialog);
    }

    protected void editCategoryAlertDialog(final String selectedCategory, final String selectedDesc, final VocabDbHelper dbHelper,
                                           final CategoryCursorAdapter cursorAdapter) {
        if (selectedCategory.equals("My Word Bank")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("My Word Bank backs up all the vocab you've added so it can't be modified.");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Edit Category");
            // Set up the input
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText categoryNameInput = new EditText(this);
            categoryNameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
            categoryNameInput.setHint("New name");
            categoryNameInput.setText(selectedCategory);
            layout.addView(categoryNameInput);

            final EditText categoryDescInput = new EditText(this);
            categoryDescInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
            categoryDescInput.setHint("New description");
            categoryDescInput.setText(selectedDesc);
            layout.addView(categoryDescInput);

            builder.setView(layout);

            // Set up the buttons
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("This action will delete all the vocab in this category.");
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete Category from Database
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            // Define 'where' part of query
                            String selection = VocabDbContract.COLUMN_NAME_CATEGORY + " LIKE ?";
                            // Specify arguments in placeholder order
                            String[] selectionArgs = {selectedCategory};
                            // Issue SQL statement
                            db.delete(VocabDbContract.TABLE_NAME_CATEGORY, selection, selectionArgs);
                            db.delete(VocabDbContract.TABLE_NAME_MY_VOCAB, selection, selectionArgs);

                            // Update Cursor
                            cursorAdapter.changeCursor(dbHelper.getCategoryCursor());

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                            .setTextColor(ContextCompat.getColor(MainActivity.this, R.color.app_icon_color));
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                            .setTextColor(ContextCompat.getColor(MainActivity.this, R.color.app_icon_color));

                }
            });

            final AlertDialog dialog = builder.create();

            dialog.show();

            changeDialogButtonsColor(dialog);

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String categoryName = categoryNameInput.getText().toString();
                    String categoryDesc = categoryDescInput.getText().toString();

                    // If new category name exists already
                    if (!selectedCategory.equals(categoryName) && mDbHelper.checkIfCategoryExists(categoryName)) {
                        Toast.makeText(MainActivity.this, categoryName + " already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        SQLiteDatabase db = dbHelper.getReadableDatabase();

                        ContentValues vocabTableValues = new ContentValues();
                        vocabTableValues.put(VocabDbContract.COLUMN_NAME_CATEGORY, categoryName);

                        ContentValues categoryTableValues = new ContentValues();
                        categoryTableValues.put(VocabDbContract.COLUMN_NAME_CATEGORY, categoryName);
                        categoryTableValues.put(VocabDbContract.COLUMN_NAME_DESCRIPTION, categoryDesc);

                        String selectionVocab = VocabDbContract.COLUMN_NAME_CATEGORY + " = ?";
                        String[] selectionArgsVocab = {selectedCategory};

                        // Update Category Table
                        db.update(
                                VocabDbContract.TABLE_NAME_CATEGORY,
                                categoryTableValues,
                                selectionVocab,
                                selectionArgsVocab
                        );

                        // Update Vocab Table for categories column to transfer the data
                        db.update(
                                VocabDbContract.TABLE_NAME_MY_VOCAB,
                                vocabTableValues,
                                selectionVocab,
                                selectionArgsVocab
                        );

                        // Update Cursor
                        cursorAdapter.changeCursor(dbHelper.getCategoryCursor());
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
