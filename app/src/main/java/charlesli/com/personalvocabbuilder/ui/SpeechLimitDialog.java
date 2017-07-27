package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import charlesli.com.personalvocabbuilder.controller.Subscription;

/**
 * Created by charles on 2017-07-20.
 */

public class SpeechLimitDialog extends CustomDialog {

    public SpeechLimitDialog(final Context context) {
        super(context);

        setMessage("Your monthly speech quota has been reached. Please subscribe for unlimited speech quota");

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(context, Subscription.class);
                context.startActivity(intent);
            }
        });
        setButton(BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

}
