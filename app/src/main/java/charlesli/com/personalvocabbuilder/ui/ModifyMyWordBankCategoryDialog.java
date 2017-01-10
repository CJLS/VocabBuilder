package charlesli.com.personalvocabbuilder.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by charles on 2017-01-09.
 */

public class ModifyMyWordBankCategoryDialog extends AlertDialog {

    public ModifyMyWordBankCategoryDialog(Context context) {
        super(context);

        setMessage("My Word Bank backs up all the vocab you've added so it can't be modified.");
        setButton(BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }
}
