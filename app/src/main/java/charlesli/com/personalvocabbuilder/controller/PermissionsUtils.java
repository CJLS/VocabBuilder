package charlesli.com.personalvocabbuilder.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import static charlesli.com.personalvocabbuilder.controller.ExportUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

/**
 * Created by charles on 2017-04-14.
 */

public class PermissionsUtils {

    public static boolean getExternalStoragePermission(Context context) {
        if (!isExternalStorageWritable()) {
            Toast.makeText(context, "External storage is unavailable. Please check your device's external storage.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (context == null) {
            Toast.makeText(context, "Sorry, the export operation did not go through. Please try again later.", Toast.LENGTH_LONG).show();
            return false;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.d("Test: ", "Get External Storage Permission denied");
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            Log.d("Test: ", "After request permission");
            return false;
        }
        return true;
    }


    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
