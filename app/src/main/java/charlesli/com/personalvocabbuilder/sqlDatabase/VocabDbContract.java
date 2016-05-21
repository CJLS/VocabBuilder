package charlesli.com.personalvocabbuilder.sqlDatabase;

import android.provider.BaseColumns;

/**
 * Created by Li on 2015/4/13.
 */
public final class VocabDbContract implements BaseColumns{

    // table names
    public static final String TABLE_NAME_MY_VOCAB = "my_vocab_table";
    public static final String TABLE_NAME_MY_WORD_BANK = "my_word_bank_table";
    public static final String TABLE_NAME_GMAT = "gmat_table";
    public static final String TABLE_NAME_GRE = "gre_table";
    // column names
    public static final String COLUMN_NAME_VOCAB = "vocab";
    public static final String COLUMN_NAME_DEFINITION = "definition";
    public static final String COLUMN_NAME_LEVEL = "level";
    public static final String COLUMN_NAME_CATEGORY = "category";
    // category names
    public static final String CATEGORY_NAME_MY_VOCAB = "My Vocab";
    public static final String CATEGORY_NAME_MY_WORD_BANK = "My Word Bank";
    public static final String CATEGORY_NAME_GMAT = "GMAT";
    public static final String CATEGORY_NAME_GRE = "GRE";

}
