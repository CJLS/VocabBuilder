package charlesli.com.personalvocabbuilder.controller;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

import charlesli.com.personalvocabbuilder.sqlDatabase.ExportCursorAdaptor;

/**
 * Created by charles on 2017-05-18.
 */

class FileExporter extends AsyncTask<Void, Void, Void> {

    private Context context;
    private ProgressBar progressBar;
    private File exportFile = null;

    FileExporter(Context context, ProgressBar progressBar) {
        this.context = context;
        this.progressBar = progressBar;
        exportFile = null;
    }

    @Override
    protected void onPreExecute() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        progressBar.setVisibility(View.GONE);
        ((Activity) context).finish();
        if (exportFile == null) {
            Toast.makeText(context, "Sorry, an error has occurred when writing to the export file. Please try again later.", Toast.LENGTH_LONG).show();
        }
        else if (!ExportUtils.shareExportFile(context, exportFile)) {
            Toast.makeText(context, "Your export file is located in your external storage's downloads folder.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        exportFile = ExportUtils.writeToExportFile(context, ExportCursorAdaptor.getSelectedCategoryPositionList());
        return null;
    }
}
