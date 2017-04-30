package charlesli.com.personalvocabbuilder.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import charlesli.com.personalvocabbuilder.R;

/**
 * Created by charles on 2017-04-23.
 */

public class ImportDialog extends CustomDialog {

    public static int GET_FILE_RESULT_CODE = 1;

    public ImportDialog(final Context context) {
        super(context);

        setTitle("Import");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_import, null);

        setView(promptsView);

        Button selectFile = (Button) promptsView.findViewById(R.id.selectFile);
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/*");
                ((Activity) context).startActivityForResult(intent, GET_FILE_RESULT_CODE);
            }
        });

        setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        setButton(BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }
}
