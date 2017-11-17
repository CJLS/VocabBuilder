package charlesli.com.personalvocabbuilder.controller;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.Arrays;

import charlesli.com.personalvocabbuilder.R;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static charlesli.com.personalvocabbuilder.controller.ReviewSession.VOCAB_TO_DEF_REVIEW_MODE;

/**
 * Created by charles on 2017-11-05.
 */

public class NotificationService extends IntentService {
    private VocabDbHelper mDbHelper = VocabDbHelper.getDBHelper(NotificationService.this);

    public NotificationService() {
        super("My Vocab Notification");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SharedPreferences sharedPreferencesDailyReview =
                getSharedPreferences(getString(R.string.sharedPrefDailyReviewFile), MODE_PRIVATE);

        String reviewCategory = sharedPreferencesDailyReview.getString(getString(R.string.sharedPrefDailyReviewCategoryKey), getString(R.string.my_word_bank));
        int numOfRows = mDbHelper.getVocabCursor(reviewCategory).getCount();
        if (numOfRows == 0) return;

        String [] reviewTypeArray = getResources().getStringArray(R.array.review_type_array);
        String reviewType = sharedPreferencesDailyReview.getString(getString(R.string.sharedPrefDailyReviewTypeKey), reviewTypeArray[0]);
        int reviewTypePos = Arrays.asList(reviewTypeArray).indexOf(reviewType);
        if (reviewTypePos < 0) reviewTypePos = 0;
        int reviewMode = sharedPreferencesDailyReview.getInt(getString(R.string.sharedPrefDailyReviewModeKey), VOCAB_TO_DEF_REVIEW_MODE);
        int reviewGoalPos = sharedPreferencesDailyReview.getInt(getString(R.string.sharedPrefDailyReviewGoalKey), 0);
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


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_vocabbuilder)
                .setContentTitle("My Vocab Daily Review")
                .setContentText("The grass is greener where you water it.");

        Intent resultIntent = new Intent(this, ReviewSession.class);
        resultIntent.putExtra("Category", reviewCategory);
        resultIntent.putExtra("Mode", reviewMode);
        resultIntent.putExtra("NumOfVocab", reviewGoal);
        resultIntent.putExtra("Type", reviewTypePos);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        notificationBuilder.setContentIntent(resultPendingIntent);
        notificationBuilder.setAutoCancel(true);

        int notificationID = 123;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, notificationBuilder.build());
    }
}
