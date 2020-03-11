package de.timschubert.uwumusic.shared.collections;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Size;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.timschubert.uwumusic.shared.mediaitems.Album;
import de.timschubert.uwumusic.shared.mediaitems.Artist;
import de.timschubert.uwumusic.shared.mediaitems.MediaItem;
import de.timschubert.uwumusic.shared.mediaitems.PlayItem;
import de.timschubert.uwumusic.shared.mediaitems.Playlist;
import de.timschubert.uwumusic.shared.mediaitems.Track;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;

public final class CollectionUtil
{

    private CollectionUtil() { }

    static LongSparseArray<Album> createAlbums(LongSparseArray<Track> allTracks)
    {
        LongSparseArray<Album> allAlbums = new LongSparseArray<>();

        for(int i = 0; i < allTracks.size(); i++)
        {
            Track track = allTracks.valueAt(i);

            if(allAlbums.get(track.getAlbumId()) != null)
            {
                allAlbums.get(track.getAlbumId()).addTrack(track.getId());
            }
            else
            {
                // TODO maybe check artistId == null
                Album newAlbum = new Album(track.getAlbumId(),
                        track.getArtistIds()[0],
                        track.getAlbumName());
                newAlbum.addTrack(track.getId());

                allAlbums.put(newAlbum.getId(), newAlbum);
            }
        }

        return allAlbums;
    }

    static LongSparseArray<Artist> createArtists(LongSparseArray<Track> allTracks)
    {
        LongSparseArray<Artist> allArtists = new LongSparseArray<>();

        for(int i = 0; i < allTracks.size(); i++)
        {
            Track track = allTracks.valueAt(i);

            for(int j = 0; j < track.getArtistIds().length; j++)
            {
                long artistId = track.getArtistIds()[j];

                if(allArtists.get(artistId) != null)
                {
                    allArtists.get(artistId).addTrack(track);
                }
                else
                {
                    Artist newArtist = new Artist(artistId, track.getArtistNames()[j]);
                    newArtist.addTrack(track);

                    allArtists.put(artistId, newArtist);
                }
            }
        }

        return allArtists;
    }

    static List<Long> sortByNameAsc(LongSparseArray<? extends MediaItem> items)
    {
        List<MediaItem> musicItems = new ArrayList<>();
        for(int i = 0; i < items.size(); i++) musicItems.add(items.valueAt(i));

        Collections.sort(musicItems, new Comparator<MediaItem>() {
            @Override
            public int compare(MediaItem o1, MediaItem o2) {
                return o1.getMediaName().compareTo(o2.getMediaName());
            }
        });

        List<Long> musicItemIds = new ArrayList<>();
        for(MediaItem item : musicItems) musicItemIds.add(item.getMediaId());

        return musicItemIds;
    }

    static List<Long> sortByDateDesc(LongSparseArray<Track> allTracksArray)
    {
        List<Track> allTracks = new ArrayList<>();
        for(int i = 0; i < allTracksArray.size(); i++) allTracks.add(allTracksArray.valueAt(i));

        Collections.sort(allTracks, new Comparator<Track>() {
            @Override
            public int compare(Track o1, Track o2) {
                return Long.compare(o2.getDateAdded(), o1.getDateAdded());
            }
        });

        List<Long> sortedIds = new ArrayList<>();
        for(Track track : allTracks) sortedIds.add(track.getId());

        return sortedIds;
    }

    static List<Long> createShuffleTracks(LongSparseArray<Track> allTracks)
    {
        List<Long> shuffleTracks = new ArrayList<>();
        MusicCollection collection = MusicCollection.getInstance();

        for(int i = 0; i < allTracks.size(); i++) shuffleTracks.add(allTracks.keyAt(i));

        if(AuriPreferences.shuffleExcludedPlaylistIds == null) return shuffleTracks;

        for(long playlistId : AuriPreferences.shuffleExcludedPlaylistIds)
        {
            Playlist excludePlaylist = collection.getPlaylist(playlistId);

            if(excludePlaylist == null) continue;

            for(Long playlistTrackId : excludePlaylist.getContainedTracks())
            {
                shuffleTracks.remove(playlistTrackId);
            }
        }

        return shuffleTracks;
    }

    /*static List<Long> createFavourites(LongSparseArray<Track> allTracks)
    {
        List<Long> favourites = new ArrayList<>();

        for(int i = 0; i < allTracks.size(); i++)
        {
            Track track = allTracks.valueAt(i);

            try {
                if(track.hasHeart()) favourites.add(track.getId());
            } catch (Exception ignore) { }
        }

        return favourites;
    }*/

    static Bitmap loadAlbumArt(Context context, Album album, Size size, Bitmap failedBitmap)
    {
        Bitmap albumArt;

        try
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                MusicCollection collection = MusicCollection.getInstance();
                Uri uri = collection.getTrack(album.getContainedTracks().get(0)).getUri();

                albumArt = context.getContentResolver().loadThumbnail(uri, size, null);
            }
            else
            {
                Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(artworkUri, album.getId());

                Bitmap albumArtHigh = MediaStore.Images.Media.getBitmap(
                        context.getContentResolver(),
                        uri);
                albumArt = Bitmap.createScaledBitmap(
                        albumArtHigh,
                        size.getWidth(),
                        size.getHeight(),
                        false);
                albumArtHigh.recycle();
            }
        }
        catch(Exception e)
        {
            albumArt = failedBitmap;
        }

        return albumArt;
    }

    public static String getArtistNamesComma(long trackId)
    {
        MusicCollection collection = MusicCollection.getInstance();

        String artistNames;
        if(AuriPreferences.showAllArtists)
        {
            artistNames = TextUtils.join(", ",
                    collection.getTrack(trackId).getArtistNames());
        }
        else
        {
            artistNames = collection.getTrack(trackId).getArtistNames()[0];
        }

        return artistNames;
    }

    public static String queueTrackPosition(long trackId)
    {
        int trackPos = (int) MusicCollection.getInstance().getTrack(trackId).getPosition();

        if(trackPos < 1000) return "CD1 - "+trackPos;

        int discNumber = (trackPos / 1000) % 10;
        int trackNumber = trackPos % 1000;

        return "CD" + discNumber + " - " + trackNumber;
    }

    public static List<PlayItem> createQueueForIds(@NonNull List<Long> list)
    {
        MusicCollection collection = MusicCollection.getInstance();

        List<PlayItem> queue = new ArrayList<>();

        try
        {
            for(Long trackId : list)
            {
                queue.add(new PlayItem(trackId, collection.getTrack(trackId).getUri()));
            }
        }
        catch (NullPointerException e) { return queue; }

        return queue;
    }

    public static boolean isEmpty(List list)
    {
        return list == null || list.isEmpty();
    }
}
