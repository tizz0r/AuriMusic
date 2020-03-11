package de.timschubert.uwumusic.shared.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.timschubert.uwumusic.shared.collections.CollectionUtil;
import de.timschubert.uwumusic.shared.collections.MusicCollection;
import de.timschubert.uwumusic.shared.mediaitems.Artist;
import de.timschubert.uwumusic.shared.mediaitems.PlayItem;

public class MusicInstanceUtils
{
    static void playPlaylist(String mediaId)
    {
        // pattern: playlist_$playlistId
        long playlistId = Long.parseLong(mediaId.substring(9));

        MusicInstanceController controller = MusicInstanceController.getInstance();
        controller.setShuffleEnabled(false);
        controller.setQueueAndPlay(MusicCollection.getInstance().getPlaylist(playlistId), 0);
    }

    static void playAlbum(String mediaId)
    {
        // pattern: album_$albumId
        long albumId = Long.parseLong(mediaId.substring(6));

        MusicInstanceController controller = MusicInstanceController.getInstance();
        controller.setShuffleEnabled(false);
        controller.setQueueAndPlay(MusicCollection.getInstance().getAlbum(albumId), 0);
    }

    static void playTrack(String mediaId)
    {
        // pattern: track_$songId
        long songId = Long.parseLong(mediaId.substring(6));
        List<Long> trackIds = MusicCollection.getInstance().getSortedTracksByName();
        int playlistPos = trackIds.indexOf(songId);

        List<PlayItem> queue = new ArrayList<>();
        MusicCollection collection = MusicCollection.getInstance();
        MusicInstanceController controller = MusicInstanceController.getInstance();

        for(long trackId : trackIds)
        {
            queue.add(new PlayItem(trackId, collection.getTrack(trackId).getUri()));
        }
        controller.setQueueAndPlay(queue, playlistPos);
    }

    static void playArtist(String mediaId)
    {
        // pattern: artist_$artistId
        long artistId = Long.parseLong(mediaId.substring(7));

        MusicInstanceController controller = MusicInstanceController.getInstance();
        List<PlayItem> artistQueue = MusicCollection.getInstance().getArtist(artistId).playables();
        int pos = new Random().nextInt(artistQueue.size());

        controller.setShuffleEnabled(true);
        controller.setQueueAndPlay(artistQueue, pos);
    }

    static void playArtistTrack(String mediaId)
    {
        // pattern: artisttrack_$songId_$artistId
        long trackId = Long.parseLong(mediaId.substring(12, mediaId.lastIndexOf("_")));
        long artistId = Long.parseLong(mediaId.substring(mediaId.lastIndexOf("_")+1));
        List<Long> artistQueue = getArtistAlbumQueue(trackId, artistId);
        int playlistPos = artistQueue.indexOf(trackId);

        MusicInstanceController controller = MusicInstanceController.getInstance();
        controller.setQueueAndPlay(CollectionUtil.createQueueForIds(artistQueue), playlistPos);
    }

    static void playGenreTrack(String mediaId)
    {
        // pattern: genretrack_$songId_$genreId

        MusicCollection collection = MusicCollection.getInstance();

        long songId = Long.parseLong(mediaId.substring(11, mediaId.lastIndexOf("_")));
        long genreId = Long.parseLong(mediaId.substring(mediaId.lastIndexOf("_")+1));
        List<Long> genreQueue = new ArrayList<>(collection.getGenre(genreId).getContainedTracks());
        int songPos = genreQueue.indexOf(songId);

        MusicInstanceController controller = MusicInstanceController.getInstance();
        controller.setQueueAndPlay(collection.getGenre(genreId), songPos);
    }

    private static List<Long> getArtistAlbumQueue(long trackId, long artistId)
    {
        MusicCollection collection = MusicCollection.getInstance();
        long albumId = collection.getTrack(trackId).getAlbumId();
        return collection.getArtist(artistId).getArtistTracksForAlbum(albumId);
    }
}
