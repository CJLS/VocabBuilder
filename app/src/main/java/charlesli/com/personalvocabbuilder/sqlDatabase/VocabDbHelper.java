package charlesli.com.personalvocabbuilder.sqlDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Li on 2015/4/13.
 */
public class VocabDbHelper extends SQLiteOpenHelper {
    // If the database schema is changed, the database version must be incremented.
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "VocabDatabase.db";
    private static final String DELETE_TABLE_MY_VOCAB =
            "DROP TABLE IF EXISTS " + VocabDbContract.TABLE_NAME_MY_VOCAB;
    private static final String DELETE_TABLE_MY_WORD_BANK =
            "DROP TABLE IF EXISTS " + VocabDbContract.TABLE_NAME_MY_WORD_BANK;
    private static final String DELETE_TABLE_GMAT =
            "DROP TABLE IF EXISTS " + VocabDbContract.TABLE_NAME_GMAT;
    private static final String DELETE_TABLE_GRE =
            "DROP TABLE IF EXISTS " + VocabDbContract.TABLE_NAME_GRE;
    private static final String DELETE_TABLE_CATEGORY =
            "DROP TABLE IF EXISTS " + VocabDbContract.TABLE_NAME_CATEGORY;
    private static VocabDbHelper dbInstance;

    private String CREATE_TABLE_MY_VOCAB =
            "CREATE TABLE  " + VocabDbContract.TABLE_NAME_MY_VOCAB +
            " (" + VocabDbContract._ID + " INTEGER PRIMARY KEY," +
            VocabDbContract.COLUMN_NAME_VOCAB + " TEXT, " +
            VocabDbContract.COLUMN_NAME_DEFINITION + " TEXT, " +
            VocabDbContract.COLUMN_NAME_LEVEL + " INTEGER, " +
                    VocabDbContract.COLUMN_NAME_CATEGORY + " TEXT );";
    private String CREATE_TABLE_CATEGORY =
            "CREATE TABLE  " + VocabDbContract.TABLE_NAME_CATEGORY +
                    " (" + VocabDbContract._ID + " INTEGER PRIMARY KEY," +
                    VocabDbContract.COLUMN_NAME_CATEGORY + " TEXT, " +
                    VocabDbContract.COLUMN_NAME_DESCRIPTION + " TEXT );";

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

        loadDefaultVocabTable(db, VocabDbContract.CATEGORY_NAME_GMAT, DefaultVocab.vocabGMAT, DefaultVocab.definitionGMAT);
        loadDefaultVocabTable(db, VocabDbContract.CATEGORY_NAME_GRE, DefaultVocab.vocabGRE, DefaultVocab.definitionGRE);
        loadDefaultCategoryTable(db);
    }

    private void loadDefaultVocabTable(SQLiteDatabase db, String category, String[] word, String[] definition) {
        for (int i = 0; i < word.length; i++) {
            loadDefaultVocabValue(db, category, word[i], definition[i]);
        }
    }

    private void loadDefaultVocabValue(SQLiteDatabase db, String category, String word, String definition) {
        ContentValues cv = new ContentValues();
        cv.put(VocabDbContract.COLUMN_NAME_VOCAB, word);
        cv.put(VocabDbContract.COLUMN_NAME_DEFINITION, definition);
        cv.put(VocabDbContract.COLUMN_NAME_LEVEL, 0);
        cv.put(VocabDbContract.COLUMN_NAME_CATEGORY, category);

        db.insert(VocabDbContract.TABLE_NAME_MY_VOCAB, null, cv);
    }

    private void loadDefaultCategoryTable(SQLiteDatabase db) {
        loadDefaultCategoryValue(db, VocabDbContract.CATEGORY_NAME_MY_VOCAB, "Vocab currently being learned");
        loadDefaultCategoryValue(db, VocabDbContract.CATEGORY_NAME_MY_WORD_BANK, "A collection of all your vocab");
        loadDefaultCategoryValue(db, VocabDbContract.CATEGORY_NAME_GMAT, "Graduate Management Admission Test");
        loadDefaultCategoryValue(db, VocabDbContract.CATEGORY_NAME_GRE, "Graduate Record Examination");
    }

    private void loadDefaultCategoryValue(SQLiteDatabase db, String category, String description) {
        ContentValues cv = new ContentValues();
        cv.put(VocabDbContract.COLUMN_NAME_CATEGORY, category);
        cv.put(VocabDbContract.COLUMN_NAME_DESCRIPTION, description);
        db.insert(VocabDbContract.TABLE_NAME_CATEGORY, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 3) {
            db.execSQL("ALTER TABLE " + VocabDbContract.TABLE_NAME_MY_VOCAB +
                    " ADD COLUMN " + VocabDbContract.COLUMN_NAME_CATEGORY + " TEXT DEFAULT '" +
                    VocabDbContract.CATEGORY_NAME_MY_VOCAB + "'");
            db.execSQL("ALTER TABLE " + VocabDbContract.TABLE_NAME_MY_WORD_BANK +
                    " ADD COLUMN " + VocabDbContract.COLUMN_NAME_CATEGORY + " TEXT DEFAULT '" +
                    VocabDbContract.CATEGORY_NAME_MY_WORD_BANK + "'");
            db.execSQL("ALTER TABLE " + VocabDbContract.TABLE_NAME_GMAT +
                    " ADD COLUMN " + VocabDbContract.COLUMN_NAME_CATEGORY + " TEXT DEFAULT '" +
                    VocabDbContract.CATEGORY_NAME_GMAT + "'");
            db.execSQL("ALTER TABLE " + VocabDbContract.TABLE_NAME_GRE +
                    " ADD COLUMN " + VocabDbContract.COLUMN_NAME_CATEGORY + " TEXT DEFAULT '" +
                    VocabDbContract.CATEGORY_NAME_GRE + "'");
            // Combine all table data into My Vocab table
            db.execSQL("INSERT INTO " + VocabDbContract.TABLE_NAME_MY_VOCAB +
                    " (" + VocabDbContract.COLUMN_NAME_VOCAB +
                    ", " + VocabDbContract.COLUMN_NAME_DEFINITION +
                    ", " + VocabDbContract.COLUMN_NAME_LEVEL +
                    ", " + VocabDbContract.COLUMN_NAME_CATEGORY + ") " +
                    " SELECT " + VocabDbContract.COLUMN_NAME_VOCAB +
                    ", " + VocabDbContract.COLUMN_NAME_DEFINITION +
                    ", " + VocabDbContract.COLUMN_NAME_LEVEL +
                    ", " + VocabDbContract.COLUMN_NAME_CATEGORY +
                    " FROM " + VocabDbContract.TABLE_NAME_MY_WORD_BANK);
            db.execSQL("INSERT INTO " + VocabDbContract.TABLE_NAME_MY_VOCAB +
                    " (" + VocabDbContract.COLUMN_NAME_VOCAB +
                    ", " + VocabDbContract.COLUMN_NAME_DEFINITION +
                    ", " + VocabDbContract.COLUMN_NAME_LEVEL +
                    ", " + VocabDbContract.COLUMN_NAME_CATEGORY + ") " +
                    " SELECT " + VocabDbContract.COLUMN_NAME_VOCAB +
                    ", " + VocabDbContract.COLUMN_NAME_DEFINITION +
                    ", " + VocabDbContract.COLUMN_NAME_LEVEL +
                    ", " + VocabDbContract.COLUMN_NAME_CATEGORY +
                    " FROM " + VocabDbContract.TABLE_NAME_GMAT);
            db.execSQL("INSERT INTO " + VocabDbContract.TABLE_NAME_MY_VOCAB +
                    " (" + VocabDbContract.COLUMN_NAME_VOCAB +
                    ", " + VocabDbContract.COLUMN_NAME_DEFINITION +
                    ", " + VocabDbContract.COLUMN_NAME_LEVEL +
                    ", " + VocabDbContract.COLUMN_NAME_CATEGORY + ") " +
                    " SELECT " + VocabDbContract.COLUMN_NAME_VOCAB +
                    ", " + VocabDbContract.COLUMN_NAME_DEFINITION +
                    ", " + VocabDbContract.COLUMN_NAME_LEVEL +
                    ", " + VocabDbContract.COLUMN_NAME_CATEGORY +
                    " FROM " + VocabDbContract.TABLE_NAME_GRE);
            db.execSQL(DELETE_TABLE_MY_WORD_BANK);
            db.execSQL(DELETE_TABLE_GMAT);
            db.execSQL(DELETE_TABLE_GRE);
        }
        if (oldVersion <= 4) {
            db.execSQL(CREATE_TABLE_CATEGORY);
            loadDefaultCategoryTable(db);
        }
        if (oldVersion <= 5) {
            db.execSQL("ALTER TABLE " + VocabDbContract.TABLE_NAME_CATEGORY +
                    " ADD COLUMN " + VocabDbContract.COLUMN_NAME_DESCRIPTION + " TEXT");
            db.execSQL("UPDATE " + VocabDbContract.TABLE_NAME_CATEGORY +
                    " SET " + VocabDbContract.COLUMN_NAME_DESCRIPTION +
                    " = " + "'Vocab currently being learned'" +
                    " WHERE " + VocabDbContract.COLUMN_NAME_CATEGORY +
                    " = " + "'" + VocabDbContract.CATEGORY_NAME_MY_VOCAB + "'");
            db.execSQL("UPDATE " + VocabDbContract.TABLE_NAME_CATEGORY +
                    " SET " + VocabDbContract.COLUMN_NAME_DESCRIPTION +
                    " = " + "'A collection of all your vocab'" +
                    " WHERE " + VocabDbContract.COLUMN_NAME_CATEGORY +
                    " = " + "'" + VocabDbContract.CATEGORY_NAME_MY_WORD_BANK + "'");
            db.execSQL("UPDATE " + VocabDbContract.TABLE_NAME_CATEGORY +
                    " SET " + VocabDbContract.COLUMN_NAME_DESCRIPTION +
                    " = " + "'Graduate Management Admission Test'" +
                    " WHERE " + VocabDbContract.COLUMN_NAME_CATEGORY +
                    " = " + "'" + VocabDbContract.CATEGORY_NAME_GMAT + "'");
            db.execSQL("UPDATE " + VocabDbContract.TABLE_NAME_CATEGORY +
                    " SET " + VocabDbContract.COLUMN_NAME_DESCRIPTION +
                    " = " + "'Graduate Record Examination'" +
                    " WHERE " + VocabDbContract.COLUMN_NAME_CATEGORY +
                    " = " + "'" + VocabDbContract.CATEGORY_NAME_GRE + "'");
        }

    }

    public void insertVocab(String category, String vocab, String definition, int level) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(VocabDbContract.COLUMN_NAME_VOCAB, vocab);
        values.put(VocabDbContract.COLUMN_NAME_DEFINITION, definition);
        values.put(VocabDbContract.COLUMN_NAME_LEVEL, level);
        values.put(VocabDbContract.COLUMN_NAME_CATEGORY, category);

        db.insert(VocabDbContract.TABLE_NAME_MY_VOCAB, null, values);
    }

    public void updateVocabDefinition(String selectedVocab, String selectedDefinition, String newDefinition) {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(VocabDbContract.COLUMN_NAME_DEFINITION, newDefinition);

        String selectionMyVocab = VocabDbContract.COLUMN_NAME_VOCAB + " = ? AND " +
                VocabDbContract.COLUMN_NAME_DEFINITION + " = ?";
        String[] selectionArgsMyVocab = {selectedVocab, selectedDefinition};

        db.update(
                VocabDbContract.TABLE_NAME_MY_VOCAB,
                values,
                selectionMyVocab,
                selectionArgsMyVocab
        );
    }

    public void deleteVocab(long posID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = VocabDbContract._ID + " = ?";
        String[] selectionArgs = {String.valueOf(posID)};
        db.delete(VocabDbContract.TABLE_NAME_MY_VOCAB, selection, selectionArgs);
    }

    public Cursor getVocabCursor(String category) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                VocabDbContract._ID,
                VocabDbContract.COLUMN_NAME_VOCAB,
                VocabDbContract.COLUMN_NAME_DEFINITION,
                VocabDbContract.COLUMN_NAME_LEVEL,
                VocabDbContract.COLUMN_NAME_CATEGORY
        };

        String selection = VocabDbContract.COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgs = {category};

        Cursor cursor = db.query(
                VocabDbContract.TABLE_NAME_MY_VOCAB,
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
            VocabDbContract._ID,
            VocabDbContract.COLUMN_NAME_VOCAB,
            VocabDbContract.COLUMN_NAME_DEFINITION,
            VocabDbContract.COLUMN_NAME_LEVEL,
            VocabDbContract.COLUMN_NAME_CATEGORY
        };

        String selection = VocabDbContract.COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgs = {category};

        Cursor cursor = db.query(
                VocabDbContract.TABLE_NAME_MY_VOCAB,
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
                VocabDbContract._ID,
                VocabDbContract.COLUMN_NAME_VOCAB,
                VocabDbContract.COLUMN_NAME_DEFINITION,
                VocabDbContract.COLUMN_NAME_LEVEL,
                VocabDbContract.COLUMN_NAME_CATEGORY
        };

        String selection = VocabDbContract.COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgs = {category};

        Cursor cursor = db.query(
                VocabDbContract.TABLE_NAME_MY_VOCAB,
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
                VocabDbContract._ID,
                VocabDbContract.COLUMN_NAME_VOCAB,
                VocabDbContract.COLUMN_NAME_DEFINITION,
                VocabDbContract.COLUMN_NAME_LEVEL,
                VocabDbContract.COLUMN_NAME_CATEGORY
        };

        String selection = VocabDbContract.COLUMN_NAME_CATEGORY + " = ?" +
                " AND " + "(" + VocabDbContract.COLUMN_NAME_VOCAB + " LIKE " + "?"
                + " OR " + VocabDbContract.COLUMN_NAME_DEFINITION + " LIKE " + "?" + ")";
        String likePattern = "%" + pattern + "%";
        String[] selectionArgs = {category, likePattern, likePattern};

        Cursor cursor = db.query(
                VocabDbContract.TABLE_NAME_MY_VOCAB, // The table to query
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
        values.put(VocabDbContract.COLUMN_NAME_CATEGORY, name);
        values.put(VocabDbContract.COLUMN_NAME_DESCRIPTION, description);

        db.insert(VocabDbContract.TABLE_NAME_CATEGORY, null, values);
    }

    public void deleteCategory(String selectedCategory) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = VocabDbContract.COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgs = {selectedCategory};
        db.delete(VocabDbContract.TABLE_NAME_CATEGORY, selection, selectionArgs);
        db.delete(VocabDbContract.TABLE_NAME_MY_VOCAB, selection, selectionArgs);
    }

    public void updateCategory(String selectedCategory, String categoryName, String categoryDesc) {
        SQLiteDatabase db = this.getReadableDatabase();

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
    }

    public Cursor getCategoryCursor() {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                VocabDbContract._ID,
                VocabDbContract.COLUMN_NAME_CATEGORY,
                VocabDbContract.COLUMN_NAME_DESCRIPTION
        };

        Cursor cursor = db.query(
                VocabDbContract.TABLE_NAME_CATEGORY, // The table to query
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
                VocabDbContract.COLUMN_NAME_DESCRIPTION
        };

        String selection = VocabDbContract.COLUMN_NAME_CATEGORY + " = ?";
        String[] selectionArgsVocab = {category};

        Cursor cursor = db.query(
                VocabDbContract.TABLE_NAME_CATEGORY,
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
        String definition = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_DESCRIPTION));
        cursor.close();
        return definition;
    }

    public Cursor getExportCursor(List<Integer> categoryPosList) {
        Cursor categoryCursor = getCategoryCursor();
        Iterator<Integer> categoryIterator = categoryPosList.iterator();

        ArrayList<String> selectedCategories = new ArrayList<String>();
        while (categoryIterator.hasNext()) {
            int position = categoryIterator.next();
            categoryCursor.moveToPosition(position);
            String categoryName =
                    categoryCursor.getString(categoryCursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_CATEGORY));
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
        String selection = VocabDbContract.COLUMN_NAME_CATEGORY + " IN " + categoryArg;

        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                VocabDbContract._ID,
                VocabDbContract.COLUMN_NAME_VOCAB,
                VocabDbContract.COLUMN_NAME_DEFINITION,
                VocabDbContract.COLUMN_NAME_LEVEL,
                VocabDbContract.COLUMN_NAME_CATEGORY
        };

        Cursor cursor = db.query(
                VocabDbContract.TABLE_NAME_MY_VOCAB, // The table to query
                projection,                                 // The columns for the WHERE clause
                selection,                                   // The rows to return for the WHERE clause
                selectedCategories.toArray(new String [0]),  // selectionArgs
                null,                                        // groupBy
                null,                                        // having
                VocabDbContract.CATEGORY_ASC,                // orderBy
                null                                         // limit (the number of rows)
        );
        return cursor;
    }

    public boolean checkIfVocabExists(String vocab, String definition) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + VocabDbContract.TABLE_NAME_MY_VOCAB + " WHERE " +
                VocabDbContract.COLUMN_NAME_VOCAB + " = ? " + " AND " +
                VocabDbContract.COLUMN_NAME_DEFINITION + " = ?";
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
        String query = "SELECT * FROM " + VocabDbContract.TABLE_NAME_MY_VOCAB + " WHERE " +
                VocabDbContract.COLUMN_NAME_VOCAB + " = ?" + " AND " +
                VocabDbContract.COLUMN_NAME_DEFINITION + " = ?" + " AND " +
                VocabDbContract.COLUMN_NAME_CATEGORY + " = ?";
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
        String query = "SELECT * FROM " + VocabDbContract.TABLE_NAME_CATEGORY + " WHERE " +
                VocabDbContract.COLUMN_NAME_CATEGORY + " = ?";
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
        String currentCategoryQuery = "SELECT " + VocabDbContract.COLUMN_NAME_CATEGORY + " FROM "
                + VocabDbContract.TABLE_NAME_MY_VOCAB + " WHERE " + VocabDbContract.COLUMN_NAME_VOCAB
                + " = ?" + " AND " + VocabDbContract.COLUMN_NAME_DEFINITION
                + " = ?" + " AND " + VocabDbContract.COLUMN_NAME_CATEGORY
                + " = ?";
        Cursor currentCategoryCursor = db.rawQuery(currentCategoryQuery, new String[]{vocab, definition, currentCategory});
        if (currentCategoryCursor.getCount() > 0) {
            currentCategoryCursor.moveToFirst();
            String category = currentCategoryCursor.getString(currentCategoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
            while (category.equals(VocabDbContract.CATEGORY_NAME_MY_WORD_BANK) && currentCategoryCursor.moveToNext()) {
                category = currentCategoryCursor.getString(currentCategoryCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
            }
            currentCategoryCursor.close();
            return category;
        }
        currentCategoryCursor.close();
        // Check other categories
        String allCategoriesQuery = "SELECT " + VocabDbContract.COLUMN_NAME_CATEGORY + " FROM "
                + VocabDbContract.TABLE_NAME_MY_VOCAB + " WHERE " + VocabDbContract.COLUMN_NAME_VOCAB
                + " = ?" + " AND " + VocabDbContract.COLUMN_NAME_DEFINITION + " = ?";
        Cursor allCategoriesCursor = db.rawQuery(allCategoriesQuery, new String[]{vocab, definition});
        if (allCategoriesCursor.getCount() > 0) {
            allCategoriesCursor.moveToFirst();
            String category = allCategoriesCursor.getString(allCategoriesCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
            while (category.equals(VocabDbContract.CATEGORY_NAME_MY_WORD_BANK) && allCategoriesCursor.moveToNext()) {
                category = allCategoriesCursor.getString(allCategoriesCursor.getColumnIndex(VocabDbContract.COLUMN_NAME_CATEGORY));
            }
            allCategoriesCursor.close();
            return category;
        }
        allCategoriesCursor.close();
        return null;
    }


}

























