package charlesli.com.personalvocabbuilder.sqlDatabase;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import charlesli.com.personalvocabbuilder.R;

/**
 * Created by charles on 2017-03-17.
 */

public class ExportCursorAdaptor extends CursorAdapter {

    private static List<Integer> selectedItemsPositions;

    public ExportCursorAdaptor(Context context, Cursor c, int flags) {
        super(context, c, flags);

        selectedItemsPositions = new ArrayList<>();
    }

    public static List<Integer> getSelectedCategoryPositionList() {
        return selectedItemsPositions;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_export, parent, false);
        CheckBox box = (CheckBox) view.findViewById(R.id.exportCheckbox);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int position = (int) compoundButton.getTag();
                if (b) {
                    if (!selectedItemsPositions.contains(position))
                        selectedItemsPositions.add(position);
                } else {
                    selectedItemsPositions.remove((Object) position);
                }
            }
        });
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CheckBox box = (CheckBox) view.findViewById(R.id.exportCheckbox);
        box.setTag(cursor.getPosition());

        TextView exportCategory = (TextView) view.findViewById(R.id.exportCategory);
        String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_CATEGORY));
        exportCategory.setText(categoryName);

        if (selectedItemsPositions.contains(cursor.getPosition()))
            box.setChecked(true);
        else
            box.setChecked(false);
    }
}
