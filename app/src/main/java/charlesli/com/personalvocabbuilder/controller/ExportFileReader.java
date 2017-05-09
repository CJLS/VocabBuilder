package charlesli.com.personalvocabbuilder.controller;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

import static charlesli.com.personalvocabbuilder.controller.ImportActivity.IMPORT_OPTION_SPECIFIED_CATEGORY;

/**
 * Created by charles on 2017-05-07.
 */

class ExportFileReader extends AsyncTask<Void, Void, Void> {

    private Context context;
    private ProgressBar progressBar;
    private Uri exportFile;
    private boolean resetVocabProgress;
    private String importCategoryName;
    private int importOption;
    private Activity activity;

    ExportFileReader(Activity activity, boolean resetVocabProgress, int importOption,
                     String importCategoryName, ProgressBar progressBar, Uri exportFile) {
        super();
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.resetVocabProgress = resetVocabProgress;
        this.progressBar = progressBar;
        this.exportFile = exportFile;
        this.importCategoryName = importCategoryName;
        this.importOption = importOption;
    }

    @Override
    protected void onPreExecute() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        progressBar.setVisibility(View.GONE);
        activity.finish();
        Toast.makeText(context, "Import Complete!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(context.getContentResolver().openInputStream(exportFile)));
            String line;
            // Skip first header line
            br.readLine();
            while ((line = br.readLine()) != null) {
                // Header: "Vocab,Definition,Level,Category Name,Category Description"
                // 1. Split line by , that's not preceded by /
                String[] row = line.split("(?<!\\\\),");
                if (row.length < 4) {
                    continue;
                }
                // 2. Get each item from array
                String vocab = row[0];
                String definition = row[1];
                String progress = row[2];
                String categoryName = row[3];
                String categoryDescription = "";
                if (row.length > 4) {
                    categoryDescription = row[4];
                }

                // 3. Convert each item from /, to ,
                vocab = vocab.replace("\\,", ",");
                definition = definition.replace("\\,", ",");
                int level;
                switch (progress) {
                    case "Difficult":
                        level = ReviewSession.DIFFICULT;
                        break;
                    case "Familiar":
                        level = ReviewSession.FAMILIAR;
                        break;
                    case "Easy":
                        level = ReviewSession.EASY;
                        break;
                    case "Perfect":
                        level = ReviewSession.PERFECT;
                        break;
                    default:
                        level = ReviewSession.DIFFICULT;
                        break;
                }

                if (resetVocabProgress) {
                    level = ReviewSession.DIFFICULT;
                }

                categoryName = categoryName.replace("\\,", ",");
                categoryDescription = categoryDescription.replace("\\,", ",");

                if (importOption == IMPORT_OPTION_SPECIFIED_CATEGORY) {
                    categoryName = importCategoryName;
                    categoryDescription = "Vocab from the export file";
                }

                // 4. Insert into category and MyVocab db
                VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(context);
                if (!dbHelper.checkIfCategoryExists(categoryName)) {
                    dbHelper.insertCategory(categoryName, categoryDescription);
                }

                if (!dbHelper.checkIfVocabExistsInCategory(vocab, definition, categoryName)) {
                    dbHelper.insertVocab(categoryName, vocab, definition, level);
                }

            }
            br.close();
        } catch (Exception e) {
            Toast.makeText(context, "Sorry an error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}
