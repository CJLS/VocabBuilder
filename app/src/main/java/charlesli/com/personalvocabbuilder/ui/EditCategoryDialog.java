package charlesli.com.personalvocabbuilder.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static charlesli.com.personalvocabbuilder.ui.DailyReviewNotification.NOTIFICATION_ID;

/**
 * Created by charles on 2017-01-01.
 */

public class EditCategoryDialog extends CustomDialog {

    public EditCategoryDialog(final Context context, final CategoryCursorAdapter cursorAdapter,
                              final String selectedCategory, String selectedDesc) {
        super(context);

        setTitle("Edit Category");

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_edit_category, null);

        final EditText categoryNameInput = (EditText) promptsView.findViewById(R.id.editCategoryName);
        categoryNameInput.setText(selectedCategory);
        final EditText categoryDescInput = (EditText) promptsView.findViewById(R.id.editCategoryDescription);
        categoryDescInput.setText(selectedDesc);

        setView(promptsView);
        final VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(context);

        setButton(BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        setButton(BUTTON_POSITIVE, "Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = categoryNameInput.getText().toString();
                String categoryDesc = categoryDescInput.getText().toString();

                // If new category name exists already
                if (!selectedCategory.equals(categoryName) && dbHelper.checkIfCategoryExists(categoryName)) {
                    Toast.makeText(getContext(), categoryName + " already exists", Toast.LENGTH_SHORT).show();
                }
                else {
                    // Update category info in db
                    dbHelper.updateCategoryNameAndDesc(selectedCategory, categoryName, categoryDesc);

                    // Update category daily review settings if the category is selected for review
                    SharedPreferences sharedPreferencesDailyReview =
                            context.getSharedPreferences(context.getString(R.string.sharedPrefDailyReviewFile), MODE_PRIVATE);
                    String dailyReviewCategory = sharedPreferencesDailyReview.getString(context.getString(R.string.sharedPrefDailyReviewCategoryKey),
                            context.getString(R.string.my_word_bank));
                    if (selectedCategory.equals(dailyReviewCategory)) {
                        sharedPreferencesDailyReview.edit()
                                .putString(context.getString(R.string.sharedPrefDailyReviewCategoryKey), categoryName).apply();
                        // Update notification if it is already issued for the category being edited
                        NotificationCompat.Builder notificationBuilder = DailyReviewNotification.getBuilder(context);
                        if (notificationBuilder != null) {
                            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                        }
                    }

                    cursorAdapter.changeCursor(dbHelper.getCategoryCursor());
                    dialog.dismiss();
                }
            }
        });
        setButton(BUTTON_NEGATIVE, "Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteCategoryDialog alertDialog = new DeleteCategoryDialog(getContext(), dbHelper,
                        cursorAdapter, selectedCategory);
                alertDialog.show();
                alertDialog.changeButtonsToAppIconColor();
            }
        });


    }

}
