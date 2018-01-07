package charlesli.com.personalvocabbuilder.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

import charlesli.com.personalvocabbuilder.R;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by charles on 2018-01-06.
 */

public class NotificationAlarm {

    static final int REQUEST_CODE = 12345;

    public static boolean alarmNotificationNotScheduled(Context context) {
        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        return PendingIntent.getBroadcast(context, REQUEST_CODE,
                intent, PendingIntent.FLAG_NO_CREATE) == null;
    }

    public static void scheduleAlarm(Context context) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        Calendar calendar = Calendar.getInstance();
        SharedPreferences sharedPreferencesDailyReview =
                context.getSharedPreferences(context.getString(R.string.sharedPrefDailyReviewFile), MODE_PRIVATE);
        int hour = sharedPreferencesDailyReview.getInt(context.getString(R.string.sharedPrefDailyReviewStudyHourKey), 8);
        int minute = sharedPreferencesDailyReview.getInt(context.getString(R.string.sharedPrefDailyReviewStudyMinKey), 30);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        long firstMillis = calendar.getTimeInMillis();
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_DAY, pIntent);
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.cancel(pIntent);
        pIntent.cancel();
    }
}
