package de.timschubert.uwumusic.shared.search;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaDescriptionCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.timschubert.uwumusic.shared.collections.MusicCollection;
import de.timschubert.uwumusic.shared.mediaitems.Album;
import de.timschubert.uwumusic.shared.mediaitems.PlayItem;
import de.timschubert.uwumusic.shared.mediaitems.Track;

public class MusicSearchUtil
{

    private final static String SEARCH_TRACK_PREFIX = "searchtrack_";

    public static SearchResult search(String query, Bundle extras)
    {
        String mediaFocus = extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);

        Log.e("Auri Search", "focus: "+mediaFocus);

        for(String key : extras.keySet())
        {
            Log.e("Auri Search", key + ": " + extras.get(key));
        }

        if(TextUtils.equals(mediaFocus, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE))
        {
            String artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);

            // TODO implement artist search logic
            Log.e("Auri Search", "search focus is artist: "+artist);
        }
        else if(TextUtils.equals(mediaFocus, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE))
        {
            // TODO implement album search logic
            Log.e("Auri Search", "search focus is album");
        }
        else if(TextUtils.equals(mediaFocus, MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE))
        {
            // TODO implement genre search logic
            Log.e("Auri Search", "search focus is genre");
        }

        if(TextUtils.equals(mediaFocus, MediaStore.Audio.Media.ENTRY_CONTENT_TYPE))
        {
            String artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
            String title = extras.getString(MediaStore.EXTRA_MEDIA_TITLE);
            String album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
            Log.e("Auri Search", "search focus is track: "+title+" from: "+album+" by: "+artist);

            return searchTrack(title, album, artist);
        }

        String searchQuery = query;
        String queryConcise = extras.getString(MediaStore.EXTRA_MEDIA_TITLE);

        if(queryConcise != null) searchQuery = queryConcise;
        Log.e("Auri Search", "no search focus, query: "+searchQuery);

        return searchEverywhere(searchQuery);
    }

    public static List<MediaItem> searchToMediaItems(List<PlayItem> searchResult)
    {
        MusicCollection collection = MusicCollection.getInstance();
        List<MediaItem> mediaList = new ArrayList<>();

        for(PlayItem item : searchResult)
        {
            Track track = collection.getTrack(item.id);
            if(track == null) continue;

            mediaList.add(new MediaItem(new MediaDescriptionCompat.Builder()
                    .setMediaId(SEARCH_TRACK_PREFIX + track.getId())
                    .setTitle(track.getName())
                    .setSubtitle(track.getAlbumName())
                    .build(), MediaItem.FLAG_PLAYABLE));
        }

        return mediaList;
    }

    private static SearchResult searchTrack(String trackName, String albumName, String artistName)
    {
        SearchResult result = searchTrackWithAlbum(trackName, albumName);

        if(result != null) return result;

        SearchResult deepSearch = searchEverywhere(trackName+" "+albumName+" "+artistName);
        if(deepSearch == null) return null;

        long topResultId = deepSearch.getResults().get(0).id;
        MusicCollection collection = MusicCollection.getInstance();
        Album album = collection.getAlbum(collection.getTrack(topResultId).getAlbumId());

        int position = album.getContainedTracks().indexOf(topResultId);
        return new SearchResult(playItemsFromAlbum(album), position);
    }

    private static SearchResult searchTrackWithAlbum(String trackName, String albumName)
    {
        MusicCollection collection = MusicCollection.getInstance();

        SearchResult albumResult = searchAlbum(albumName);

        if(albumResult == null) { return null; }

        int position = 0;
        for(PlayItem item : albumResult.getResults())
        {
            Track track = collection.getTrack(item.id);

            if(track.getName().equalsIgnoreCase(trackName))
            {
                position = albumResult.getResults().indexOf(item);
            }
        }

        if(position > 0) return new SearchResult(albumResult.getResults(), position);
        else return albumResult;
    }

    /*private static SearchResult searchTrackAlone(String trackName)
    {
        MusicCollection collection = MusicCollection.getInstance();

        for(long trackId : collection.getSortedTracksByName())
        {
            Track track = collection.getTrack(trackId);

            if(!track.getName().equalsIgnoreCase(trackName)) continue;

            Album album = collection.getAlbum(track.getAlbumId());

            int position = album.getContainedTracks().indexOf(trackId);

            return new SearchResult(playItemsFromAlbum(album), position);
        }

        return null;
    }*/

    private static SearchResult searchAlbum(String albumName)
    {
        MusicCollection collection = MusicCollection.getInstance();

        for(long albumId : collection.getSortedAlbumsByName())
        {
            Album album = collection.getAlbum(albumId);

            if(album.getAlbumName().equalsIgnoreCase(albumName))
            {
                return new SearchResult(playItemsFromAlbum(album), 0);
            }
        }

        return null;
    }

    private static SearchResult searchEverywhere(String query)
    {
        Map<Track, Integer> matchesPerTrack = new HashMap<>();

        List<PlayItem> searchResultList = new ArrayList<>();
        MusicCollection collection = MusicCollection.getInstance();

        for(long trackId : collection.getSortedTracksByName())
        {
            Track track = collection.getTrack(trackId);

            matchesPerTrack.put(track, searchFieldMatchesAmount(track, query));
        }

        HashMap<Track, Integer> sortedMatches = sortResultsByMatches(matchesPerTrack);

        for(Track track : sortedMatches.keySet())
        {
            if(Objects.equals(sortedMatches.get(track), 0)) continue;
            searchResultList.add(playItemFromTrack(track));
        }

        if(searchResultList.isEmpty()) return null;

        return new SearchResult(searchResultList, 0);
    }

    private static int searchFieldMatchesAmount(Track track, String querySearch)
    {
        int matches = 0;

        String[] querySplit = querySearch.split("\\s");

        for(String query : querySplit)
        {
            for(String nameSplit : track.getName().split("\\s"))
            {
                if(nameSplit.equalsIgnoreCase(query)) matches ++;
            }

            for(String albumSplit : track.getAlbumName().split("\\s"))
            {
                if(albumSplit.equalsIgnoreCase(query)) matches ++;
            }

            for(String artistName : track.getArtistNames())
            {
                for(String artistSplit : artistName.split("\\s"))
                {
                    if(artistSplit.equalsIgnoreCase(query)) matches ++;
                }
            }
            for(String genreName : track.getGenreNames())
            {
                for(String genreSplit : genreName.split("\\s"))
                {
                    if(genreName.equalsIgnoreCase(query)) matches ++;
                }
            }
        }

        return matches;
    }

    private static boolean anyFieldMatches(Track track, String querySearch)
    {
        String[] querySplit = querySearch.split("\\s");

        for(String query : querySplit)
        {
            for(String nameSplit : track.getName().split("\\s"))
            {
                if(nameSplit.equalsIgnoreCase(query)) return true;
            }

            for(String albumSplit : track.getAlbumName().split("\\s"))
            {
                if(albumSplit.equalsIgnoreCase(query)) return true;
            }

            for(String artistName : track.getArtistNames())
            {
                if(artistName.equalsIgnoreCase(query)) return true;
            }
            for(String genreName : track.getGenreNames())
            {
                if(genreName.equalsIgnoreCase(query)) return true;
            }
        }

        return false;
    }

    private static PlayItem playItemFromTrack(Track track)
    {
        return new PlayItem(track.getId(), track.getUri());
    }

    private static List<PlayItem> playItemsFromAlbum(Album album)
    {
        List<PlayItem> playItems = new ArrayList<>();
        MusicCollection collection = MusicCollection.getInstance();

        for(long trackId : album.getContainedTracks())
        {
            playItems.add(playItemFromTrack(collection.getTrack(trackId)));
        }

        return playItems;
    }

    private static HashMap<Track, Integer> sortResultsByMatches(Map<Track, Integer> map)
    {
        List<Map.Entry<Track, Integer>> list = new ArrayList<>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Track, Integer>>()
        {
            @Override
            public int compare(Map.Entry<Track, Integer> o1, Map.Entry<Track, Integer> o2)
            {
                return o2.getValue().compareTo(o1.getValue()); // reverse order
            }
        });

        HashMap<Track, Integer> out = new LinkedHashMap<>();
        for(Map.Entry<Track, Integer> entry : list)
        {
            out.put(entry.getKey(), entry.getValue());
        }

        return out;
    }
}
