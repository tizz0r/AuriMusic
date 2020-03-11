package de.timschubert.uwumusic.shared.prefs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class PermissionHelper
{

    public static boolean arePermissionsGranted(@NonNull Context context)
    {
        context = context.getApplicationContext();

        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void askForPermissions(@NonNull Fragment fragment,
                                         Context context,
                                         int requestCode)
    {
        if(!arePermissionsGranted(context))
        {
            fragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    requestCode);
        }
    }

    public static void askForPermissions(@NonNull Activity activity,
                                         Context context,
                                         int requestCode)
    {
        if(arePermissionsGranted(context)) return;

        ActivityCompat.requestPermissions(activity,
                new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                requestCode);
    }
}
