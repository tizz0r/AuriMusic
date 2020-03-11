package de.timschubert.uwumusic.shared.collections;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.timschubert.uwumusic.shared.R;
import de.timschubert.uwumusic.shared.mediaitems.Album;
import de.timschubert.uwumusic.shared.mediaitems.Genre;
import de.timschubert.uwumusic.shared.mediaitems.Playlist;
import de.timschubert.uwumusic.shared.mediaitems.Track;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;

public class MusicLoader
{
    private Context context;

    private ContentResolver contentResolver;

    MusicLoader(Context context)
    {
        this.context = context;

        contentResolver = context.getContentResolver();
    }

    public LongSparseArray<Track> loadAllTracks()
    {
        long beginTime = System.currentTimeMillis();

        LongSparseArray<Track> allTracks = new LongSparseArray<>();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] musicProjection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DATE_ADDED };

        String[] musicSelectionArgs = null;
        String musicSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        musicSelection += " AND " +
                "duration" + " > " +
                AuriPreferences.excludeUnderSeconds*1000;

        String searchFolder = AuriPreferences.getSearchFolder(context);
        if(!searchFolder.equals(context.getString(R.string.preferences_search_everywhere)))
        {
            musicSelection += " AND " + MediaStore.Audio.Media.DATA + " LIKE ? ";
            musicSelectionArgs = new String[]{ "%" + searchFolder + "%" };
        }

        Cursor musicCursor = contentResolver.query(musicUri,
                musicProjection,
                musicSelection,
                musicSelectionArgs,
                null);

        if(musicCursor != null && musicCursor.moveToFirst())
        {
            do
            {
                Track queriedTrack = createTrack(musicCursor, musicUri);
                if(queriedTrack != null)
                {
                    allTracks.put(queriedTrack.getId(), queriedTrack);
                }
            }
            while(musicCursor.moveToNext());

            musicCursor.close();
        }

        Log.e("AuriMusic", "All tracks loaded in: " +
                ((System.currentTimeMillis()-beginTime) / 1000) + "s");

        return allTracks;
    }

    LongSparseArray<Playlist> loadAllPlaylists()
    {
        LongSparseArray<Playlist> allPlaylists = new LongSparseArray<>();

        Uri playlistUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

        String[] playlistProjection = {
                MediaStore.Audio.Playlists.NAME,
                MediaStore.Audio.Playlists._ID};

        Cursor playlistCursor = contentResolver.query(playlistUri,
                playlistProjection,
                null,
                null,
                null);

        if(playlistCursor != null && playlistCursor.moveToFirst())
        {
            do
            {
                Playlist queriedPlaylist = createPlaylist(playlistCursor);

                Log.i("UwU Music", "Playlist found: "+queriedPlaylist.getName()+" with "+queriedPlaylist.getContainedTracks().size()+" tracks");

                // Skip if playlist is empty
                if(queriedPlaylist.getContainedTracks().isEmpty()) continue;

                allPlaylists.put(queriedPlaylist.getId(), queriedPlaylist);
            }
            while(playlistCursor.moveToNext());

            playlistCursor.close();
        }

        return allPlaylists;
    }

    List<Long> loadFavouritesFromPrefs()
    {
        List<Long> favouriteTracks = new ArrayList<>();

        SharedPreferences favPreferences = context.getSharedPreferences(context.getString(
                R.string.shared_preferences_favourite),
                Context.MODE_PRIVATE);

        Map<String, ?> allFavSongs = favPreferences.getAll();
        SharedPreferences.Editor editor = favPreferences.edit();
        for(Map.Entry<String, ?> entry : allFavSongs.entrySet())
        {
            if(entry.getKey().startsWith("heart_"))
            {
                long trackId = Long.parseLong(entry.getKey().substring(6));
                Track queriedTrack = MusicCollection.getInstance().getTrack(trackId);
                if(queriedTrack != null)
                {
                    boolean isFavourite = (Boolean)entry.getValue();
                    if(isFavourite)
                    {
                        // Track is marked as favourite
                        favouriteTracks.add(queriedTrack.getId());
                    }
                }
                else
                {
                    Log.i("UwU Music", "Found missing fav song: "+trackId+", removing...");
                    editor.remove(entry.getKey());
                }
            }
        }
        editor.apply();
        return favouriteTracks;
    }

    void loadArtworkAsync(final Context context,
                          final LongSparseArray<Album> allAlbums,
                          final Size size,
                          final Bitmap failedBitmap)
    {
        Runnable artworkRunnable = new Runnable() {
            @Override
            public void run() {
                long beginTime = System.currentTimeMillis();

                for (int i = 0; i < allAlbums.size(); i++)
                {
                    Album album = allAlbums.valueAt(i);

                    Bitmap albumArt = CollectionUtil.loadAlbumArt(context, album, size, failedBitmap);
                    album.setArtwork(albumArt);
                }

                Log.e("Auri Music", "Artwork caching done in " + (System.currentTimeMillis() - beginTime) / 1000.0f + "s");
            }
        };
        new Thread(artworkRunnable).start();
    }

    void loadGenresAsync(final LongSparseArray<Track> allTracks, final LongSparseArray<Genre> toFill, final List<Long> toFillSorted)
    {
        loadGenresCursor(allTracks, toFill, toFillSorted);
    }

    private void loadGenresCursor(final LongSparseArray<Track> allTracks, final LongSparseArray<Genre> toFill, final List<Long> toFillSorted)
    {
        Runnable genreRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                long beginTime = System.currentTimeMillis();

                for(int i = 0; i < allTracks.size(); i ++)
                {
                    Track track = allTracks.valueAt(i);

                    Uri genresUri = MediaStore.Audio.Genres.getContentUriForAudioId("external", (int) track.getId());

                    String[] genresProjection = {MediaStore.Audio.Genres.NAME};
                    Cursor genresCursor = context.getContentResolver().query(genresUri, genresProjection, null, null, null, null);

                    if (genresCursor != null && genresCursor.moveToFirst())
                    {
                        String genreRaw = genresCursor.getString(0);
                        genresCursor.close();

                        String[] genres = genreRaw.split(";\\s*");
                        long[] genreIds = new long[genres.length];

                        for (int j = 0; j < genres.length; j++)
                        {
                            genreIds[j] = genres[j].hashCode();
                        }

                        track.setGenres(genreIds, genres);
                        assignOrCreateGenre(genreIds, track, toFill);
                        toFillSorted.clear();
                        toFillSorted.addAll(CollectionUtil.sortByNameAsc(toFill));
                    }
                }

                Log.e("Auri Music", "Genres caching (Cursor) done in "+(System.currentTimeMillis()-beginTime)/1000.0f+"s");
            }
        };

        new Thread(genreRunnable).start();
    }

    //void loadGenresAsync(Context context, LongSparseArray<Song> allSongs, LongSparseArray<Genre> toFill, List<Long> sortedToFill)
    //{
    //    Runnable genreRunnable = () ->
    //    {
    //        long beginTime = System.currentTimeMillis();
//
    //        for(int i = 0; i < allSongs.size(); i++)
    //        {
    //            Song song = allSongs.valueAt(i);
//
    //            Uri genresUri = MediaStore.Audio.Genres.getContentUriForAudioId("external", (int)song.getID());
//
    //            String[] genresProjection = {MediaStore.Audio.Genres.NAME};
    //            Cursor genresCursor = context.getContentResolver().query(genresUri, genresProjection, null, null, null, null);
//
    //            if(genresCursor != null && genresCursor.moveToFirst())
    //            {
    //                String genreRaw = genresCursor.getString(0);
    //                genresCursor.close();
//
    //                String[] genres = genreRaw.split(";\\s*");
    //                long[] genreIds = new long[genres.length];
//
    //                for(int j = 0; j < genres.length; j++)
    //                {
    //                    genreIds[j] = genres[j].hashCode();
    //                }
//
    //                song.setGenres(genreIds, genres);
    //                assignOrCreateGenre(genreIds, song, toFill);
    //            }
    //        }
//
    //        sortedToFill.addAll(CollectionUtil.sortByName(toFill));
//
    //        Log.i("UwU Music", "Genres caching done in "+(System.currentTimeMillis()-beginTime)/1000.0f+"s");
    //    };
//
    //    new Thread(genreRunnable).start();
    //}

    void release()
    {

    }

    private Track createTrack(Cursor musicCursor, Uri musicUri)
    {
        int songTitleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int songIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
        int albumTitleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        int artistTitleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int trackPosColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
        int dateAddedColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);

        String trackTitle = musicCursor.getString(songTitleColumn);
        String albumTitle = musicCursor.getString(albumTitleColumn);
        String artistTitleRaw = musicCursor.getString(artistTitleColumn);
        String trackPosRaw = musicCursor.getString(trackPosColumn);
        long trackId = musicCursor.getLong(songIdColumn);
        long albumId = musicCursor.getLong(albumIdColumn);
        long dateAdded = musicCursor.getLong(dateAddedColumn);
        Uri trackUri = ContentUris.withAppendedId(musicUri, trackId);

        int trackPos = getTrackPos(trackPosRaw);
        String[] artistTitles = getAllArtistTitles(artistTitleRaw);
        long[] artistIds = getAllArtistIds(artistTitleRaw);

        return new Track.Builder()
                .setId(trackId)
                .setAlbumId(albumId)
                .setTrackPosition(trackPos)
                .setDateAdded(dateAdded)
                .setArtistIds(artistIds)
                .setArtistNames(artistTitles)
                .setName(trackTitle)
                .setAlbumName(albumTitle)
                .setUri(trackUri)
                .build();
    }

    private Playlist createPlaylist(Cursor playlistCursor)
    {
        int playlistTitleColumn = playlistCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
        int playlistIdColumn = playlistCursor.getColumnIndex(MediaStore.Audio.Playlists._ID);

        String playlistName = playlistCursor.getString(playlistTitleColumn);
        long playlistId = playlistCursor.getLong(playlistIdColumn);

        Uri playlistMemberUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        String[] playlistMemberProjection = {MediaStore.Audio.Playlists.Members.AUDIO_ID};
        String sortOrder = MediaStore.Audio.Playlists.Members.TRACK + " ASC";

        Cursor playlistMemberCursor;

        try
        {
            playlistMemberCursor = contentResolver.query(playlistMemberUri,
                playlistMemberProjection,
                null,
                null,
                sortOrder);
        }
        catch (Exception e)
        {
            playlistMemberCursor = contentResolver.query(playlistMemberUri,
                    playlistMemberProjection,
                    null,
                    null,
                    null);
        }


        List<Long> containedTracks = new ArrayList<>();
        if(playlistMemberCursor != null && playlistMemberCursor.moveToFirst())
        {
            int playlistMemberIdColumn = playlistMemberCursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
            do
            {
                long playlistMemberId = playlistMemberCursor.getLong(playlistMemberIdColumn);

                if(MusicCollection.getInstance().getTrack(playlistMemberId) != null)
                {
                    containedTracks.add(playlistMemberId);
                }
            }
            while(playlistMemberCursor.moveToNext());

            playlistMemberCursor.close();
        }

        return new Playlist.Builder(playlistId)
                .setName(playlistName)
                .setContainedTracks(containedTracks)
                .build();
    }

    private String[] getAllArtistTitles(String artistTitleRaw)
    {
        return artistTitleRaw.split(";\\s*");
    }

    private long[] getAllArtistIds(String artistTitleRaw)
    {
        String[] artistTitles = artistTitleRaw.split(";\\s*");
        long[] artistIds = new long[artistTitles.length];

        for(int j = 0; j < artistTitles.length; j++)
        {
            artistIds[j] = artistTitles[j].hashCode();
        }

        return artistIds;
    }

    private void assignOrCreateGenre(long[] genreIds, Track track, LongSparseArray<Genre> toFill)
    {
        for(int i = 0; i < genreIds.length; i++)
        {
            long genreId = genreIds[i];

            if(toFill.get(genreId) != null)
            {
                toFill.get(genreId).addTrack(track);
            }
            else
            {
                if(track.getGenreNames() == null) return;

                Genre newGenre = new Genre(genreId, track.getGenreNames()[i]);
                newGenre.addTrack(track);

                toFill.put(genreId, newGenre);
            }
        }
    }

    private int getTrackPos(String trackPosRaw)
    {
        int trackPos;

        try
        {
            trackPos = Integer.parseInt(trackPosRaw);
        }
        catch(Exception e)
        {
            trackPos = 0;
        }

        return trackPos;
    }
}
