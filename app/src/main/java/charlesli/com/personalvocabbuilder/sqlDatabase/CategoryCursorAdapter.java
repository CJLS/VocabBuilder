package charlesli.com.personalvocabbuilder.sqlDatabase;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import charlesli.com.personalvocabbuilder.R;

/**
 * Created by charles on 2016-05-21.
 */
public class CategoryCursorAdapter extends CursorAdapter {

    public CategoryCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvCategory = (TextView) view.findViewById(R.id.categoryName);
        String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_CATEGORY));
        tvCategory.setText(categoryName);

        TextView tvDescription = (TextView) view.findViewById(R.id.categoryDesc);
        String categoryDesc = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_DESCRIPTION));
        tvDescription.setText(categoryDesc);
    }
}
