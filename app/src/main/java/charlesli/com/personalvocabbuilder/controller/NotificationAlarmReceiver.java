package charlesli.com.personalvocabbuilder.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by charles on 2017-11-05.
 */

public class NotificationAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, NotificationService.class);
        context.startService(notificationIntent);
    }
}
