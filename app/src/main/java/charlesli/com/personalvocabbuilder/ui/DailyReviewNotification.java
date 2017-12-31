package charlesli.com.personalvocabbuilder.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import java.util.Arrays;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.controller.ReviewSession;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static android.content.Context.MODE_PRIVATE;
import static charlesli.com.personalvocabbuilder.controller.ReviewSession.VOCAB_TO_DEF_REVIEW_MODE;

/**
 * Created by charles on 2017-12-29.
 */

public class DailyReviewNotification {

    public static final int NOTIFICATION_ID = 123;

    public static NotificationCompat.Builder getBuilder(Context context) {
        VocabDbHelper mDbHelper = VocabDbHelper.getDBHelper(context);

        SharedPreferences sharedPreferencesDailyReview =
                context.getSharedPreferences(context.getString(R.string.sharedPrefDailyReviewFile), MODE_PRIVATE);

        String reviewCategory = sharedPreferencesDailyReview.getString(context.getString(R.string.sharedPrefDailyReviewCategoryKey), context.getString(R.string.my_word_bank));
        int numOfRows = mDbHelper.getVocabCursor(reviewCategory).getCount();
        if (numOfRows == 0) return null;

        String [] reviewTypeArray = context.getResources().getStringArray(R.array.review_type_array);
        String reviewType = sharedPreferencesDailyReview.getString(context.getString(R.string.sharedPrefDailyReviewTypeKey), reviewTypeArray[0]);
        int reviewTypePos = Arrays.asList(reviewTypeArray).indexOf(reviewType);
        if (reviewTypePos < 0) reviewTypePos = 0;
        int reviewMode = sharedPreferencesDailyReview.getInt(context.getString(R.string.sharedPrefDailyReviewModeKey), VOCAB_TO_DEF_REVIEW_MODE);
        int reviewGoalPos = sharedPreferencesDailyReview.getInt(context.getString(R.string.sharedPrefDailyReviewGoalKey), 0);
        int reviewGoal;
        switch (reviewGoalPos) {
            case 0:
                reviewGoal = 10;
                break;
            case 1:
                reviewGoal = 20;
                break;
            case 2:
                reviewGoal = 30;
                break;
            default:
                reviewGoal = 10;
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.icon_vocabbuilder)
                .setContentTitle("My Vocab Daily Review")
                .setContentText("The grass is greener where you water it.");

        Intent resultIntent = new Intent(context, ReviewSession.class);
        resultIntent.putExtra("Category", reviewCategory);
        resultIntent.putExtra("Mode", reviewMode);
        resultIntent.putExtra("NumOfVocab", reviewGoal);
        resultIntent.putExtra("Type", reviewTypePos);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        notificationBuilder.setContentIntent(resultPendingIntent);
        notificationBuilder.setAutoCancel(true);

        return notificationBuilder;
    }

}
