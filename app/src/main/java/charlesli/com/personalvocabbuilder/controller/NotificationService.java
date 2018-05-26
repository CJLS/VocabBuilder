package charlesli.com.personalvocabbuilder.controller;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;

import charlesli.com.personalvocabbuilder.ui.DailyReviewNotification;

import static charlesli.com.personalvocabbuilder.ui.DailyReviewNotification.NOTIFICATION_ID;
import static charlesli.com.personalvocabbuilder.ui.DailyReviewNotification.createNotificationChannel;

/**
 * Created by charles on 2017-11-05.
 */

public class NotificationService extends JobIntentService {

    public static final int JOB_ID = 1;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, NotificationService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        createNotificationChannel(this);
        NotificationCompat.Builder notificationBuilder = DailyReviewNotification.getBuilder(this);
        if (notificationBuilder != null) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

}
