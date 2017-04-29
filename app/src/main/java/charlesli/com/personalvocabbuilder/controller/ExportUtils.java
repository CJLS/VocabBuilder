package charlesli.com.personalvocabbuilder.controller;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import charlesli.com.personalvocabbuilder.sqlDatabase.ExportCursorAdaptor;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbContract;
import charlesli.com.personalvocabbuilder.sqlDatabase.VocabDbHelper;

/**
 * Created by charles on 2017-04-13.
 */

public class ExportUtils {

    static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    public static void exportCategory(Context context) {
        File exportFile = writeToExportFile(context, ExportCursorAdaptor.getSelectedCategoryPositionList());
        if (!shareExportFile(context, exportFile)) {
            Toast.makeText(context, "Your export file is located in your external storage's downloads folder.", Toast.LENGTH_LONG).show();
        }
    }

    private static boolean shareExportFile(Context context, File exportFile) {
        boolean exportFileSent = true;
        Intent sendFileIntent = new Intent();
        sendFileIntent.setAction(Intent.ACTION_SEND);
        sendFileIntent.setType("text/csv");
        sendFileIntent.putExtra(Intent.EXTRA_SUBJECT, "My Vocab Export File");
        sendFileIntent.putExtra(Intent.EXTRA_TEXT, "The export file can be viewed in a text editor.");
        sendFileIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exportFile));

        if (sendFileIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(sendFileIntent, "Send to"));
        }
        else {
            exportFileSent = false;
        }
        return exportFileSent;
    }

    private static File writeToExportFile(Context context, List<Integer> categoryPositionList) {
        VocabDbHelper dbHelper = VocabDbHelper.getDBHelper(context);

        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        path.mkdirs();
        File file = new File(path, "MyVocabExportFile.csv");

        try {
            FileWriter fileWriter = new FileWriter(file);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("Vocab,Definition,Level,Category Name,Category Description");

            bufferedWriter.newLine();
            Cursor cursor = dbHelper.getExportCursor(categoryPositionList);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String vocab = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_VOCAB));
                vocab = vocab.replace(",", "\\,");
                String definition = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_DEFINITION));
                definition = definition.replace(",", "\\,");
                int lvl = cursor.getInt(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_LEVEL));
                String level;
                switch (lvl) {
                    case ReviewSession.DIFFICULT:
                        level = "Difficult";
                        break;
                    case ReviewSession.FAMILIAR:
                        level = "Familiar";
                        break;
                    case ReviewSession.EASY:
                        level = "Easy";
                        break;
                    case ReviewSession.PERFECT:
                        level = "Perfect";
                        break;
                    default:
                        level = "Difficult";
                        break;
                }
                String category = cursor.getString(cursor.getColumnIndexOrThrow(VocabDbContract.COLUMN_NAME_CATEGORY));
                String description = dbHelper.getCategoryDefinition(category);

                category = category.replace(",", "\\,");
                description = description.replace(",", "\\,");
                String lineToWrite = vocab + "," + definition + "," + level + "," + category + "," + description;

                /* For TSV option
                String lineToWrite = vocab + "\t" + definition + "\t" + level + "\t" + category + "\t" + description;
                */

                bufferedWriter.write(lineToWrite);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }
        catch (Exception e) {
            Toast.makeText(context, "Sorry, an error has occurred when writing to the export file. Please try again later.", Toast.LENGTH_LONG).show();
        }
        return file;
    }

}
