package charlesli.com.personalvocabbuilder.sqlDatabase;

import android.provider.BaseColumns;

/**
 * Created by Li on 2015/4/13.
 */
public final class VocabDbContract implements BaseColumns{

    // table names
    public static final String TABLE_NAME_MY_VOCAB = "my_vocab_table";
    public static final String TABLE_NAME_CATEGORY = "category_table";
    // deprecated (for DB version <= 3) tables
    public static final String TABLE_NAME_MY_WORD_BANK = "my_word_bank_table";
    public static final String TABLE_NAME_GMAT = "gmat_table";
    public static final String TABLE_NAME_GRE = "gre_table";

    // column names
    public static final String COLUMN_NAME_VOCAB = "vocab";
    public static final String COLUMN_NAME_DEFINITION = "definition";
    public static final String COLUMN_NAME_LEVEL = "level";
    public static final String COLUMN_NAME_REVIEWED_AT = "reviewed_at";

    public static final String COLUMN_NAME_CATEGORY = "category";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_LOCALE = "locale";
    // category names
    public static final String CATEGORY_NAME_MY_VOCAB = "My Vocab";
    public static final String CATEGORY_NAME_MY_WORD_BANK = "My Word Bank";
    public static final String CATEGORY_NAME_GMAT = "GMAT";
    public static final String CATEGORY_NAME_GRE = "GRE";
    // sort by orders
    public static final String RANDOM = "Random()";
    public static final String DATE_ASC = _ID + " ASC";
    public static final String DATE_DESC = _ID + " DESC";
    public static final String VOCAB_ASC = COLUMN_NAME_VOCAB + " COLLATE NOCASE ASC";
    public static final String VOCAB_DESC = COLUMN_NAME_VOCAB + " COLLATE NOCASE DESC";
    public static final String LEVEL_ASC = COLUMN_NAME_LEVEL + " ASC" + "," + "Random()";
    public static final String LEVEL_DESC = COLUMN_NAME_LEVEL + " DESC" + "," + "Random()";
    public static final String CATEGORY_ASC = COLUMN_NAME_CATEGORY + " COLLATE NOCASE ASC";
}
