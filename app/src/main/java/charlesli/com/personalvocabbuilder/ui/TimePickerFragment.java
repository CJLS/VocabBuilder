package charlesli.com.personalvocabbuilder.ui;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Locale;

import charlesli.com.personalvocabbuilder.R;

import static android.content.Context.MODE_PRIVATE;
import static charlesli.com.personalvocabbuilder.controller.NotificationAlarm.alarmNotificationNotScheduled;
import static charlesli.com.personalvocabbuilder.controller.NotificationAlarm.cancelAlarm;
import static charlesli.com.personalvocabbuilder.controller.NotificationAlarm.scheduleAlarm;

/**
 * Created by charles on 2017-11-02.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    SharedPreferences sharedPreferencesDailyReview;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        sharedPreferencesDailyReview =
                getContext().getSharedPreferences(getString(R.string.sharedPrefDailyReviewFile), MODE_PRIVATE);
        int hour = sharedPreferencesDailyReview.getInt(getString(R.string.sharedPrefDailyReviewStudyHourKey), 9);
        int minute = sharedPreferencesDailyReview.getInt(getString(R.string.sharedPrefDailyReviewStudyMinKey), 30);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        SharedPreferences.Editor editor = sharedPreferencesDailyReview.edit();
        editor.putInt(getString(R.string.sharedPrefDailyReviewStudyHourKey), hour);
        editor.putInt(getString(R.string.sharedPrefDailyReviewStudyMinKey), minute);
        editor.apply();

        String periodOfDay = "AM";

        if (hour >= 12) {
            hour = hour - 12;
            periodOfDay = "PM";
        }
        if (hour == 0) {
            hour = 12;
        }

        TextView studyTime = (TextView) getActivity().findViewById(R.id.reminderTimeInfo);
        studyTime.setText(hour + ":" + String.format(Locale.CANADA, "%02d", minute) + " " + periodOfDay);

        boolean isDailyReviewOn = sharedPreferencesDailyReview.getBoolean(getString(R.string.sharedPrefDailyReviewSwitchKey), true);
        if (isDailyReviewOn) {
            if (alarmNotificationNotScheduled(getContext())) {
                scheduleAlarm(getContext());
            }
        }
        else {
            cancelAlarm(getContext());
        }
    }
}
