package charlesli.com.personalvocabbuilder.sqlDatabase;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Li on 2015/4/13.
 */
public class VocabDbHelper extends SQLiteOpenHelper {

    private Context context;
    private static VocabDbHelper dbInstance;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "VocabDatabase.db";


    // Table for My Vocab
    private String CREATE_TABLE_MY_VOCAB =
            "CREATE TABLE  " + VocabDbContract.TABLE_NAME_MY_VOCAB +
            " (" + VocabDbContract._ID + " INTEGER PRIMARY KEY," +
            VocabDbContract.COLUMN_NAME_VOCAB + " TEXT, " +
            VocabDbContract.COLUMN_NAME_DEFINITION + " TEXT, " +
            VocabDbContract.COLUMN_NAME_LEVEL + " INTEGER );";

    // Table for My Word Bank
    private String CREATE_TABLE_MY_WORD_BANK =
            "CREATE TABLE  " + VocabDbContract.TABLE_NAME_MY_WORD_BANK +
            " (" + VocabDbContract._ID + " INTEGER PRIMARY KEY," +
            VocabDbContract.COLUMN_NAME_VOCAB + " TEXT, " +
            VocabDbContract.COLUMN_NAME_DEFINITION + " TEXT, " +
            VocabDbContract.COLUMN_NAME_LEVEL + " INTEGER );";

    // Table for My Word Bank
    private String CREATE_TABLE_GMAT =
            "CREATE TABLE  " + VocabDbContract.TABLE_NAME_GMAT +
                    " (" + VocabDbContract._ID + " INTEGER PRIMARY KEY," +
                    VocabDbContract.COLUMN_NAME_VOCAB + " TEXT, " +
                    VocabDbContract.COLUMN_NAME_DEFINITION + " TEXT, " +
                    VocabDbContract.COLUMN_NAME_LEVEL + " INTEGER );";



    private static final String DELETE_TABLE_MY_VOCAB =
            "DROP TABLE IF EXISTS " + VocabDbContract.TABLE_NAME_MY_VOCAB;

    private static final String DELETE_TABLE_MY_WORD_BANK =
            "DROP TABLE IF EXISTS " + VocabDbContract.TABLE_NAME_MY_WORD_BANK;

    private static final String DELETE_TABLE_GMAT =
            "DROP TABLE IF EXISTS " + VocabDbContract.TABLE_NAME_GMAT;

    private VocabDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized VocabDbHelper getDBHelper(Context context) {
        if (dbInstance == null) {
            dbInstance = new VocabDbHelper(context);
        }
        return dbInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Creating required tables
        db.execSQL(CREATE_TABLE_MY_VOCAB);
        db.execSQL(CREATE_TABLE_MY_WORD_BANK);
        db.execSQL(CREATE_TABLE_GMAT);


        /*
        VocabDbHelper mDbHelper = getDBHelper(context);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abaft", "adj: on or toward the rear of a ship", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abandon", "v: to leave behind", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abase", "v: to degrade, humiliate", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abbreviate", "v: to shorten, compress", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abdicate", "v: to reject, renounce", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abhor", "v: to hate", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abject", "v: to give up", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abnegation", "n: denial", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abominate", "v: to loathe, hate", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abridge", "v: to shorten, limit", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abrogate", "v: to cancel by authority", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abrupt", "adj: happening or ending unexpectedly", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abscond", "v: to go away hastily or secretly", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Absolve", "v: to forgive", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abstemious", "adj: sparing in use of food or drinks", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abstruse", "adj: hard to understand", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Abysmal", "adj: very deep", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Accede", "v: to comply with", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Acclaim", "v: loud approval", 0);
        mDbHelper.insertVocab(mDbHelper, VocabDbContract.TABLE_NAME_GMAT, "Accrue", "v: a natural growth; a periodic increase", 0);
        */

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // simply to discard the data and start over
        db.execSQL(DELETE_TABLE_MY_VOCAB);
        db.execSQL(DELETE_TABLE_MY_WORD_BANK);
        db.execSQL(DELETE_TABLE_GMAT);

        // Create new tables
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertVocab(String tableName, String vocab, String definition, int level) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new vap of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(VocabDbContract.COLUMN_NAME_VOCAB, vocab);
        values.put(VocabDbContract.COLUMN_NAME_DEFINITION, definition);
        values.put(VocabDbContract.COLUMN_NAME_LEVEL, level);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(tableName, null, values);
    }


    public Cursor getCursorMyVocab(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
            VocabDbContract._ID,
            VocabDbContract.COLUMN_NAME_VOCAB,
            VocabDbContract.COLUMN_NAME_DEFINITION,
            VocabDbContract.COLUMN_NAME_LEVEL
        };

        Cursor cursor = db.query(
                tableName, // The table to query
                projection,                                 // The columns for the WHERE clause
                null,                                        // The rows to return for the WHERE clause
                null,                                        // selectionArgs
                null,                                        // groupBy
                null,                                        // having
                null,                                        // orderBy
                null                                         // limit (the number of rows)
        );
        return cursor;
    }

}

























