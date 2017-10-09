package charlesli.com.personalvocabbuilder.sqlDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.CATEGORY_ASC;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.CATEGORY_NAME_GMAT;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.CATEGORY_NAME_GRE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.CATEGORY_NAME_MY_VOCAB;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.CATEGORY_NAME_MY_WORD_BANK;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.COLUMN_NAME_CATEGORY;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.COLUMN_NAME_DEFINITION;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.COLUMN_NAME_DESCRIPTION;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.COLUMN_NAME_LEVEL;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.COLUMN_NAME_LOCALE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.COLUMN_NAME_REVIEWED_AT;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.COLUMN_NAME_VOCAB;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.TABLE_NAME_CATEGORY;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.TABLE_NAME_GMAT;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.TABLE_NAME_GRE;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.TABLE_NAME_MY_VOCAB;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract.TABLE_NAME_MY_WORD_BANK;
import static charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract._ID;

/**
 * Created by Li on 2015/4/13.
 */
public class VocabDbHelper extends SQLiteOpenHelper {
    // If the database schema is changed, the database version must be incremented.
    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "VocabDatabase.db";
    private static final String DELETE_TABLE_MY_VOCAB =
            "DROP TABLE IF EXISTS " + TABLE_NAME_MY_VOCAB;
    private static final String DELETE_TABLE_MY_WORD_BANK =
            "DROP TABLE IF EXISTS " + TABLE_NAME_MY_WORD_BANK;
    private static final String DELETE_TABLE_GMAT =
            "DROP TABLE IF EXISTS " + TABLE_NAME_GMAT;
    private static final String DELETE_TABLE_GRE =
            "DROP TABLE IF EXISTS " + TABLE_NAME_GRE;
    private static final String DELETE_TABLE_CATEGORY =
            "DROP TABLE IF EXISTS " + TABLE_NAME_CATEGORY;
    private static VocabDbHelper dbInstance;

    private String CREATE_TABLE_MY_VOCAB =
            "CREATE TABLE  " + TABLE_NAME_MY_VOCAB +
            " (" + _ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_VOCAB + " TEXT, " +
            COLUMN_NAME_DEFINITION + " TEXT, " +
            COLUMN_NAME_LEVEL + " INTEGER, " +
                    COLUMN_NAME_CATEGORY + " TEXT, " +
                    COLUMN_NAME_REVIEWED_AT + " DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP );";
    private String CREATE_TABLE_CATEGORY =
            "CREATE TABLE  " + TABLE_NAME_CATEGORY +
                    " (" + _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_CATEGORY + " TEXT, " +
                    COLUMN_NAME_DESCRIPTION + " TEXT, " +
                    COLUMN_NAME_LOCALE + " TEXT NOT NULL DEFAULT '" +
                    Locale.US.getDisplayName() + "' );";

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
        db.execSQL(CREATE_TABLE_MY_VOCAB);
        db.execSQL(CREATE_TABLE_CATEGORY);

        loadDefaultVocabTable(db, CATEGORY_NAME_GMAT, DefaultVocab.vocabGMAT, DefaultVocab.definitionGMAT);
        loadDefaultVocabTable(db, CATEGORY_NAME_GRE, DefaultVocab.vocabGRE, DefaultVocab.definitionGRE);
        loadDefaultCategoryTable(db);
    }

    private void loadDefaultVocabTable(SQLiteDatabase db, String category, String[] word, String[] definition) {
        for (int i = 0; i < word.length; i++) {
            loadDefaultVocabValue(db, category, word[i], definition[i]);
        }
    }

    private void loadDefaultVocabValue(SQLiteDatabase db, String category, String word, String definition) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_VOCAB, word);
        cv.put(COLUMN_NAME_DEFINITION, definition);
        cv.put(COLUMN_NAME_LEVEL, 0);
        cv.put(COLUMN_NAME_CATEGORY, category);

        db.insert(TABLE_NAME_MY_VOCAB, null, cv);
    }

    private void loadDefaultCategoryTable(SQLiteDatabase db) {
        loadDefaultCategoryValue(db, CATEGORY_NAME_MY_VOCAB, "Vocab currently being learned");
        loadDefaultCategoryValue(db, CATEGORY_NAME_MY_WORD_BANK, "A collection of all your vocab");
        loadDefaultCategoryValue(db, CATEGORY_NAME_GMAT, "Graduate Management Admission Test");
        loadDefaultCategoryValue(db, CATEGORY_NAME_GRE, "Graduate Record Examination");
    }

    private void loadDefaultCategoryValue(SQLiteDatabase db, String category, String description) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_CATEGORY, category);
        cv.put(COLUMN_NAME_DESCRIPTION, description);
        db.insert(TABLE_NAME_CATEGORY, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME_MY_VOCAB +
                    " ADD COLUMN " + COLUMN_NAME_CATEGORY + " TEXT DEFAULT '" +
                    CATEGORY_NAME_MY_VOCAB + "'");
            db.execSQL("ALTER TABLE " + TABLE_NAME_MY_WORD_BANK +
                    " ADD COLUMN " + COLUMN_NAME_CATEGORY + " TEXT DEFAULT '" +
                    CATEGORY_NAME_MY_WORD_BANK + "'");
            db.execSQL("ALTER TABLE " + TABLE_NAME_GMAT +
                    " ADD COLUMN " + COLUMN_NAME_CATEGORY + " TEXT DEFAULT '" +
                    CATEGORY_NAME_GMAT + "'");
            db.execSQL("ALTER TABLE " + TABLE_NAME_GRE +
                    " ADD COLUMN " + COLUMN_NAME_CATEGORY + " TEXT DEFAULT '" +
                    CATEGORY_NAME_GRE + "'");
            // Combine all table data into My Vocab table
            db.execSQL("INSERT INTO " + TABLE_NAME_MY_VOCAB +
                    " (" + COLUMN_NAME_VOCAB +
                    ", " + COLUMN_NAME_DEFINITION +
                    ", " + COLUMN_NAME_LEVEL +
                    ", " + COLUMN_NAME_CATEGORY + ") " +
                    " SELECT " + COLUMN_NAME_VOCAB +
                    ", " + COLUMN_NAME_DEFINITION +
                    ", " + COLUMN_NAME_LEVEL +
                    ", " + COLUMN_NAME_CATEGORY +
                    " FROM " + TABLE_NAME_MY_WORD_BANK);
            db.execSQL("INSERT INTO " + TABLE_NAME_MY_VOCAB +
                    " (" + COLUMN_NAME_VOCAB +
                    ", " + COLUMN_NAME_DEFINITION +
                    ", " + COLUMN_NAME_LEVEL +
                    ", " + COLUMN_NAME_CATEGORY + ") " +
                    " SELECT " + COLUMN_NAME_VOCAB +
                    ", " + COLUMN_NAME_DEFINITION +
                    ", " + COLUMN_NAME_LEVEL +
                    ", " + COLUMN_NAME_CATEGORY +
                    " FROM " + TABLE_NAME_GMAT);
            db.execSQL("INSERT INTO " + TABLE_NAME_MY_VOCAB +
                    " (" + COLUMN_NAME_VOCAB +
                    ", " + COLUMN_NAME_DEFINITION +
                    ", " + COLUMN_NAME_LEVEL +
                    ", " + COLUMN_NAME_CATEGORY + ") " +
                    " SELECT " + COLUMN_NAME_VOCAB +
                    ", " + COLUMN_NAME_DEFINITION +
                    ", " + COLUMN_NAME_LEVEL +
                    ", " + COLUMN_NAME_CATEGORY +
                    " FROM " + TABLE_NAME_GRE);
            db.execSQL(DELETE_TABLE_MY_WORD_BANK);
            db.execSQL(DELETE_TABLE_GMAT);
            db.execSQL(DELETE_TABLE_GRE);
        }
        if (oldVersion <= 4) {
            db.execSQL(CREATE_TABLE_CATEGORY);
            loadDefaultCategoryTable(db);
        }
        if (oldVersion <= 5) {
            db.execSQL("ALTER TABLE " + TABLE_NAME_CATEGORY +
                    " ADD COLUMN " + COLUMN_NAME_DESCRIPTION + " TEXT");
            db.execSQL("UPDATE " + TABLE_NAME_CATEGORY +
                    " SET " + COLUMN_NAME_DESCRIPTION +
                    " = " + "'Vocab currently being learned'" +
                    " WHERE " + COLUMN_NAME_CATEGORY +
                    " = " + "'" + CATEGORY_NAME_MY_VOCAB + "'");
            db.execSQL("UPDATE " + TABLE_NAME_CATEGORY +
                    " SET " + COLUMN_NAME_DESCRIPTION +
                    " = " + "'A collection of all your vocab'" +
                    " WHERE " + COLUMN_NAME_CATEGORY +
                    " = " + "'" + CATEGORY_NAME_MY_WORD_BANK + "'");
            db.execSQL("UPDATE " + TABLE_NAME_CATEGORY +
                    " SET " + COLUMN_NAME_DESCRIPTION +
                    " = " + "'Graduate Management Admission Test'" +
                    " WHERE " + COLUMN_NAME_CATEGORY +
                    " = " + "'" + CATEGORY_NAME_GMAT + "'");
            db.execSQL("UPDATE " + TABLE_NAME_CATEGORY +
                    " SET " + COLUMN_NAME_DESCRIPTION +
                    " = " + "'Graduate Record Examination'" +
                    " WHERE " + COLUMN_NAME_CATEGORY +
                    " = " + "'" + CATEGORY_NAME_GRE + "'");
        }
        if (oldVersion <= 6) {
            db.execSQL("ALTER TABLE " + TABLE_NAME_CATEGORY +
                    " ADD COLUMN " + COLUMN_NAME_LOCALE + " TEXT NOT NULL DEFAULT '" +
                    Locale.US.getDisplayName() + "'");
        }
        if (oldVersion <= 7) {
            db.execSQL("ALTER TABLE " + TABLE_NAME_MY_VOCAB +
                    " ADD COLUMN " + COLUMN_NAME_REVIEWED_AT + " DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        }

    }

    public void insertVocab(String category, String vocab, String definition, int level) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_VOCAB, vocab);
        values.put(COLUMN_NAME_DEFINITION, definition);
        values.put(COLUMN_NAME_LEVEL, level);
        values.put(COLUMN_NAME_CATEGORY, category);

        db.insert(TABLE_NAME_MY_VOCAB, null, values);
    }

    public void updateVocabDefinition(String selectedVocab, String selectedDefinition, String newVocab, String newDefinition) {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_VOCAB, newVocab);
        values.put(COLUMN_NAME_DEFINITION, newDefinition);

        String selectionMyVocab = COLUMN_NAME_VOCAB + " = ? AND " +
                COLUMN_NAME_DEFINITION + " = ?";
        String[] selectionArgsMyVocab = {selectedVocab, selectedDefinition};

        db.update(
                TABLE_NAME_MY_VOCAB,
                values,
                selectionMyVocab,
                selectionArgsMyVocab
        );
    }

    public void deleteVocab(long posID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = _ID + " = ?";
        String[] selectionArgs = {String.valueOf(posID)};
        db.delete(TABLE_NAME_MY_VOCAB, selection, selectionArgs);
    }

    public Cursor getVocabCursor(String category) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                _ID,
                COLUMN_NAME_VOCAB,
                COLUMN_NAME_DEFINITION,
                COLUMN_NAME_LEVEL,
                COLUMN_NAME_CATEGORY
        };

        String selection = COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgs = {category};

        Cursor cursor = db.query(
                TABLE_NAME_MY_VOCAB,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        );
        return cursor;
    }

    public Cursor getVocabCursor(String category, String orderBy) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
            _ID,
            COLUMN_NAME_VOCAB,
            COLUMN_NAME_DEFINITION,
            COLUMN_NAME_LEVEL,
            COLUMN_NAME_CATEGORY
        };

        String selection = COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgs = {category};

        Cursor cursor = db.query(
                TABLE_NAME_MY_VOCAB,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                orderBy,
                null
        );
        return cursor;
    }

    public Cursor getVocabCursor(String category, String orderBy, int limit) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                _ID,
                COLUMN_NAME_VOCAB,
                COLUMN_NAME_DEFINITION,
                COLUMN_NAME_LEVEL,
                COLUMN_NAME_CATEGORY
        };

        String selection = COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgs = {category};

        Cursor cursor = db.query(
                TABLE_NAME_MY_VOCAB,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                orderBy,
                String.valueOf(limit)
        );
        return cursor;
    }

    public Cursor getVocabCursorWithStringPattern(String category, String pattern, String orderBy) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                _ID,
                COLUMN_NAME_VOCAB,
                COLUMN_NAME_DEFINITION,
                COLUMN_NAME_LEVEL,
                COLUMN_NAME_CATEGORY
        };

        String selection = COLUMN_NAME_CATEGORY + " = ?" +
                " AND " + "(" + COLUMN_NAME_VOCAB + " LIKE " + "?"
                + " OR " + COLUMN_NAME_DEFINITION + " LIKE " + "?" + ")";
        String likePattern = "%" + pattern + "%";
        String[] selectionArgs = {category, likePattern, likePattern};

        Cursor cursor = db.query(
                TABLE_NAME_MY_VOCAB, // The table to query
                projection,                                 // The columns for the WHERE clause
                selection,                                   // The rows to return for the WHERE clause
                selectionArgs,                                        // selectionArgs
                null,                                        // groupBy
                null,                                        // having
                orderBy,                                     // orderBy
                null                                         // limit (the number of rows)
        );
        return cursor;
    }

    public void insertCategory(String name, String description) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_CATEGORY, name);
        values.put(COLUMN_NAME_DESCRIPTION, description);

        db.insert(TABLE_NAME_CATEGORY, null, values);
    }

    public void deleteCategory(String selectedCategory) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgs = {selectedCategory};
        db.delete(TABLE_NAME_CATEGORY, selection, selectionArgs);
        db.delete(TABLE_NAME_MY_VOCAB, selection, selectionArgs);
    }

    public void updateCategoryNameAndDesc(String selectedCategory, String categoryName, String categoryDesc) {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues vocabTableValues = new ContentValues();
        vocabTableValues.put(COLUMN_NAME_CATEGORY, categoryName);

        ContentValues categoryTableValues = new ContentValues();
        categoryTableValues.put(COLUMN_NAME_CATEGORY, categoryName);
        categoryTableValues.put(COLUMN_NAME_DESCRIPTION, categoryDesc);

        String selectionVocab = COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgsVocab = {selectedCategory};

        // Update Category Table
        db.update(
                TABLE_NAME_CATEGORY,
                categoryTableValues,
                selectionVocab,
                selectionArgsVocab
        );

        // Update Vocab Table for categories column to transfer the data
        db.update(
                TABLE_NAME_MY_VOCAB,
                vocabTableValues,
                selectionVocab,
                selectionArgsVocab
        );
    }

    public void updateCategoryLocaleDisplayName(String selectedCategory, String localeDisplayName) {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues categoryTableValues = new ContentValues();
        categoryTableValues.put(COLUMN_NAME_LOCALE, localeDisplayName);

        String selectionVocab = COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgsVocab = {selectedCategory};

        db.update(
                TABLE_NAME_CATEGORY,
                categoryTableValues,
                selectionVocab,
                selectionArgsVocab
        );
    }


    public Cursor getCategoryCursor() {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                _ID,
                COLUMN_NAME_CATEGORY,
                COLUMN_NAME_DESCRIPTION
        };

        Cursor cursor = db.query(
                TABLE_NAME_CATEGORY, // The table to query
                projection,                                 // The columns for the WHERE clause
                null,                                   // The rows to return for the WHERE clause
                null,                                        // selectionArgs
                null,                                        // groupBy
                null,                                        // having
                null,                                        // orderBy
                null                                         // limit (the number of rows)
        );
        return cursor;
    }

    public String getCategoryDefinition(String category) throws Exception {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COLUMN_NAME_DESCRIPTION
        };

        String selection = COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgsVocab = {category};

        Cursor cursor = db.query(
                TABLE_NAME_CATEGORY,
                projection,
                selection,
                selectionArgsVocab,
                null,
                null,
                null,
                null
        );
        if (!cursor.moveToFirst()) {
            throw new Exception();
        }
        String definition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_DESCRIPTION));
        cursor.close();
        return definition;
    }

    public String getCategoryLocaleDisplayName(String category) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COLUMN_NAME_LOCALE
        };

        String selection = COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgsVocab = {category};

        Cursor cursor = db.query(
                TABLE_NAME_CATEGORY,
                projection,
                selection,
                selectionArgsVocab,
                null,
                null,
                null,
                null
        );
        if (!cursor.moveToFirst()) {
            return Locale.US.getDisplayName();
        }
        String localeDisplayName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LOCALE));
        cursor.close();
        return localeDisplayName;
    }

    public Cursor getExportCursor(List<Integer> categoryPosList) {
        Cursor categoryCursor = getCategoryCursor();
        Iterator<Integer> categoryIterator = categoryPosList.iterator();

        ArrayList<String> selectedCategories = new ArrayList<String>();
        while (categoryIterator.hasNext()) {
            int position = categoryIterator.next();
            categoryCursor.moveToPosition(position);
            String categoryName =
                    categoryCursor.getString(categoryCursor.getColumnIndexOrThrow(COLUMN_NAME_CATEGORY));
            selectedCategories.add(categoryName);
        }
        int arrayLength = selectedCategories.size();
        String categoryArg = "(";
        for (int i = 0; i < arrayLength; i++) {
            categoryArg += "?";
            if (i < arrayLength - 1) {
                categoryArg += ",";
            }
        }
        categoryArg += ")";
        String selection = COLUMN_NAME_CATEGORY + " IN " + categoryArg;

        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                _ID,
                COLUMN_NAME_VOCAB,
                COLUMN_NAME_DEFINITION,
                COLUMN_NAME_LEVEL,
                COLUMN_NAME_CATEGORY
        };

        Cursor cursor = db.query(
                TABLE_NAME_MY_VOCAB, // The table to query
                projection,                                 // The columns for the WHERE clause
                selection,                                   // The rows to return for the WHERE clause
                selectedCategories.toArray(new String [0]),  // selectionArgs
                null,                                        // groupBy
                null,                                        // having
                CATEGORY_ASC,                // orderBy
                null                                         // limit (the number of rows)
        );
        return cursor;
    }

    public boolean checkIfVocabExists(String vocab, String definition) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME_MY_VOCAB + " WHERE " +
                COLUMN_NAME_VOCAB + " = ? " + " AND " +
                COLUMN_NAME_DEFINITION + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{vocab, definition});
        boolean vocabExists = false;

        if (cursor.getCount() > 0) {
            vocabExists = true;
        }
        cursor.close();
        return vocabExists;
    }

    public boolean checkIfVocabExistsInCategory(String vocab, String definition, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_MY_VOCAB + " WHERE " +
                COLUMN_NAME_VOCAB + " = ?" + " AND " +
                COLUMN_NAME_DEFINITION + " = ?" + " AND " +
                COLUMN_NAME_CATEGORY + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{vocab, definition, category});
        boolean vocabExists = false;

        if (cursor.getCount() > 0) {
            vocabExists = true;
        }
        cursor.close();
        return vocabExists;
    }

    public boolean checkIfCategoryExists(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_CATEGORY + " WHERE " +
                COLUMN_NAME_CATEGORY + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{category});

        boolean vocabExists = false;
        if (cursor.getCount() > 0) {
            vocabExists = true;
        }
        cursor.close();
        return vocabExists;
    }

    public String findVocabFirstCategory(String vocab, String definition, String currentCategory) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Check current category first
        String currentCategoryQuery = "SELECT " + COLUMN_NAME_CATEGORY + " FROM "
                + TABLE_NAME_MY_VOCAB + " WHERE " + COLUMN_NAME_VOCAB
                + " = ?" + " AND " + COLUMN_NAME_DEFINITION
                + " = ?" + " AND " + COLUMN_NAME_CATEGORY
                + " = ?";
        Cursor currentCategoryCursor = db.rawQuery(currentCategoryQuery, new String[]{vocab, definition, currentCategory});
        if (currentCategoryCursor.getCount() > 0) {
            currentCategoryCursor.moveToFirst();
            String category = currentCategoryCursor.getString(currentCategoryCursor.getColumnIndex(COLUMN_NAME_CATEGORY));
            while (category.equals(CATEGORY_NAME_MY_WORD_BANK) && currentCategoryCursor.moveToNext()) {
                category = currentCategoryCursor.getString(currentCategoryCursor.getColumnIndex(COLUMN_NAME_CATEGORY));
            }
            currentCategoryCursor.close();
            return category;
        }
        currentCategoryCursor.close();
        // Check other categories
        String allCategoriesQuery = "SELECT " + COLUMN_NAME_CATEGORY + " FROM "
                + TABLE_NAME_MY_VOCAB + " WHERE " + COLUMN_NAME_VOCAB
                + " = ?" + " AND " + COLUMN_NAME_DEFINITION + " = ?";
        Cursor allCategoriesCursor = db.rawQuery(allCategoriesQuery, new String[]{vocab, definition});
        if (allCategoriesCursor.getCount() > 0) {
            allCategoriesCursor.moveToFirst();
            String category = allCategoriesCursor.getString(allCategoriesCursor.getColumnIndex(COLUMN_NAME_CATEGORY));
            while (category.equals(CATEGORY_NAME_MY_WORD_BANK) && allCategoriesCursor.moveToNext()) {
                category = allCategoriesCursor.getString(allCategoriesCursor.getColumnIndex(COLUMN_NAME_CATEGORY));
            }
            allCategoriesCursor.close();
            return category;
        }
        allCategoriesCursor.close();
        return null;
    }


}

























