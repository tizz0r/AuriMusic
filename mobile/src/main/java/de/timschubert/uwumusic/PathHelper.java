package de.timschubert.uwumusic;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.File;

public class PathHelper
{

    public static String getPath(@NonNull Context context, @NonNull Uri uri)
    {
        if(!DocumentsContract.isDocumentUri(context, uri)) return null;
        if(!isExternalStorageDocument(uri)) return null;

        String docId = DocumentsContract.getDocumentId(uri);
        String[] splitIds = docId.split(":");
        String type = splitIds[0];

        if("primary".equalsIgnoreCase(type))
        {
            return getPrimaryStoragePath(splitIds);
        }
        else
        {
            return getExternalStoragePath(context, splitIds);
        }
    }

    private static String getPrimaryStoragePath(String[] splitIds)
    {
        if(splitIds.length > 1)
        {
            return Environment.getExternalStorageDirectory() + "/" + splitIds[splitIds.length-1];
        }

        return Environment.getExternalStorageDirectory() + "/";
    }

    private static String getExternalStoragePath(Context context, String[] splitIds)
    {
        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(
                context.getApplicationContext(),
                null);

        for(File externalVolume : externalStorageVolumes)
        {
            String externalVolumePath = externalVolume.toString();

            if(externalVolume.toString().contains("/Android"))
            {
                externalVolumePath = externalVolume.toString().substring(0,
                        externalVolume.toString().lastIndexOf("/Android"));
            }

            if(!externalVolumePath.contains(splitIds[0])) continue;

            if(splitIds.length > 1)
            {
                return externalVolumePath + "/" + splitIds[splitIds.length-1];
            }
            else
            {
                // Root SD Card Volume
                return externalVolumePath + "/";
            }
        }

        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
}
