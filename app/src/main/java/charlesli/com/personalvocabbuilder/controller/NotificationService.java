package charlesli.com.personalvocabbuilder.controller;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import charlesli.com.personalvocabbuilder.ui.DailyReviewNotification;

import static charlesli.com.personalvocabbuilder.ui.DailyReviewNotification.NOTIFICATION_ID;

/**
 * Created by charles on 2017-11-05.
 */

public class NotificationService extends IntentService {

    public NotificationService() {
        super("My Vocab Notification");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        NotificationCompat.Builder notificationBuilder = DailyReviewNotification.getBuilder(this);
        if (notificationBuilder != null) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }
}
