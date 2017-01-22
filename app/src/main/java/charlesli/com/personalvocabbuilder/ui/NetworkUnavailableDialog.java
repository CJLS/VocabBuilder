package charlesli.com.personalvocabbuilder.ui;

import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by charles on 2017-01-22.
 */

public class NetworkUnavailableDialog extends CustomDialog {

    public NetworkUnavailableDialog(Context context) {
        super(context);

        setMessage("Network is unavailable. Please try again later.");
        setButton(BUTTON_NEGATIVE, "OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }
}
