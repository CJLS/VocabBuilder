package charlesli.com.personalvocabbuilder.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;

import charlesli.com.personalvocabbuilder.R;

/**
 * Created by charles on 2017-01-18.
 */

public class CustomDialog extends AlertDialog {

    protected CustomDialog(Context context) {
        super(context);
    }

    public void changeDialogButtonsColor() {
        if (getButton(DialogInterface.BUTTON_POSITIVE) != null) {
            getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(getContext(), R.color.app_icon_color));
        }

        if (getButton(DialogInterface.BUTTON_NEGATIVE) != null) {
            getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(getContext(), R.color.app_icon_color));
        }

        if (getButton(DialogInterface.BUTTON_NEUTRAL) != null) {
            getButton(DialogInterface.BUTTON_NEUTRAL)
                    .setTextColor(ContextCompat.getColor(getContext(), R.color.app_icon_color));
        }
    }
}
