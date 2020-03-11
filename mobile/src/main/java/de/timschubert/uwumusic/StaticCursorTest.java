package de.timschubert.uwumusic;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.uwumusic.shared.mediaitems.Playlist;

public class StaticCursorTest
{


    public static void listMusicForDir(Context context, String path)
    {
        ContentResolver contentResolver = context.getContentResolver();

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DATA + " LIKE ? ";
        String[] selectionArgs = { "%" + path + "%" };

        Cursor musicCursor = contentResolver.query(musicUri,
                null,
                selection,
                selectionArgs,
                null);

        if(musicCursor != null && musicCursor.moveToFirst())
        {
            do
            {
                int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                String title = musicCursor.getString(titleColumn);

                Log.e("Auri Music", "Track found: "+title);
            }
            while (musicCursor.moveToNext());
            musicCursor.close();
        }
    }

    public static boolean isMusicInDir(Context context, String path)
    {
        ContentResolver contentResolver = context.getContentResolver();

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DATA + " LIKE ? ";
        String[] selectionArgs = { "%" + path + "%" };

        Cursor musicCursor = contentResolver.query(musicUri,
                null,
                selection,
                selectionArgs,
                null);

        boolean musicFound = musicCursor != null && musicCursor.moveToFirst();
        if(musicFound)musicCursor.close();

        return musicFound;
    }

    public static List<Playlist> loadPlaylists(Context context)
    {
        ContentResolver musicResolver = context.getContentResolver();
        Uri playlistUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String[] playlistProjection = {MediaStore.Audio.Playlists.NAME, MediaStore.Audio.Playlists._ID};
        Cursor playlistCursor = musicResolver.query(playlistUri, playlistProjection, null, null, "NAME ASC");

        ArrayList<Playlist> playlists = new ArrayList<>();

        if(playlistCursor != null && playlistCursor.moveToFirst())
        {
            int playlistNameColumn = playlistCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
            int playlistIdColumn = playlistCursor.getColumnIndex(MediaStore.Audio.Playlists._ID);

            do
            {
                String playlistName = playlistCursor.getString(playlistNameColumn);
                long playlistId = playlistCursor.getLong(playlistIdColumn);

                Uri playlistMemberUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
                String[] playlistMemberProjection = {MediaStore.Audio.Playlists.Members.AUDIO_ID};
                Cursor playlistMemberCursor = musicResolver.query(playlistMemberUri, playlistMemberProjection, null, null, null);

                if(playlistMemberCursor != null && playlistMemberCursor.moveToFirst())
                {
                    int playlistMemberIdColumn = playlistMemberCursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
                    List<Long> containedTracks = new ArrayList<>();
                    do
                    {
                        long playlistMemberId = playlistMemberCursor.getLong(playlistMemberIdColumn);
                        containedTracks.add(playlistMemberId);
                    }
                    while(playlistMemberCursor.moveToNext());

                    Playlist queriedPlaylist = new Playlist.Builder(playlistId)
                            .setName(playlistName)
                            .setContainedTracks(containedTracks)
                            .build();

                    playlists.add(queriedPlaylist);
                    playlistMemberCursor.close();
                }
            }
            while(playlistCursor.moveToNext());

            playlistCursor.close();
        }

        return playlists;
    }
}
