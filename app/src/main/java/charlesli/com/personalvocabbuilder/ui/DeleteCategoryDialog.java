package charlesli.com.personalvocabbuilder.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.CategoryCursorAdapter;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static charlesli.com.personalvocabbuilder.ui.DailyReviewNotification.NOTIFICATION_ID;

/**
 * Created by charles on 2017-01-07.
 */

class DeleteCategoryDialog extends CustomDialog {

    DeleteCategoryDialog(final Context context, final VocabDbHelper dbHelper,
                         final CategoryCursorAdapter cursorAdapter,
                         final String selectedCategory) {
        super(context);

        setMessage("This action will delete all the vocab in this category.");
        setButton(BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        setButton(BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.deleteCategory(selectedCategory);

                // Update category daily review settings if the category is selected for review
                SharedPreferences sharedPreferencesDailyReview =
                        context.getSharedPreferences(context.getString(R.string.sharedPrefDailyReviewFile), MODE_PRIVATE);
                String dailyReviewCategory = sharedPreferencesDailyReview.getString(context.getString(R.string.sharedPrefDailyReviewCategoryKey),
                        context.getString(R.string.my_word_bank));
                if (selectedCategory.equals(dailyReviewCategory)) {
                    sharedPreferencesDailyReview.edit()
                            .putString(context.getString(R.string.sharedPrefDailyReviewCategoryKey), context.getString(R.string.my_word_bank)).apply();
                    // Update notification if it is already issued for the category being edited
                    NotificationCompat.Builder notificationBuilder = DailyReviewNotification.getBuilder(context);
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                    if (notificationBuilder != null) {
                        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                    }
                    else {
                        // Cancel notification in the event that my word bank is empty
                        notificationManager.cancel(NOTIFICATION_ID);
                    }
                }

                cursorAdapter.changeCursor(dbHelper.getCategoryCursor());
            }
        });
    }
}
