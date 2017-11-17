package charlesli.com.personalvocabbuilder.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by charles on 2017-11-05.
 */

public class NotificationAlarmReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "charlesli.com.personalvocabbuilder.notification.alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, NotificationService.class);
        context.startService(notificationIntent);
    }
}
