package de.timschubert.uwumusic.shared.browser;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaDescriptionCompat;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.uwumusic.shared.R;
import de.timschubert.uwumusic.shared.collections.MusicCollection;
import de.timschubert.uwumusic.shared.exceptions.SetupNotFinishedException;
import de.timschubert.uwumusic.shared.mediaitems.Album;
import de.timschubert.uwumusic.shared.mediaitems.Artist;
import de.timschubert.uwumusic.shared.mediaitems.Genre;
import de.timschubert.uwumusic.shared.mediaitems.Playlist;
import de.timschubert.uwumusic.shared.mediaitems.Track;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;
import de.timschubert.uwumusic.shared.prefs.PermissionHelper;
import de.timschubert.uwumusic.shared.search.MusicSearchUtil;
import de.timschubert.uwumusic.shared.service.MusicService;

public class MusicBrowser
{

    private Context context;
    private MusicCollection collection;
    private BrowseMenuLoader menuLoader;

    public MusicBrowser(Context c)
    {
        context = c;
        collection = MusicCollection.getInstance();
        menuLoader = new BrowseMenuLoader(context);
    }

    @Nullable
    public List<MediaItem> browse(String parentId)
    {
        boolean permissionsGranted = PermissionHelper.arePermissionsGranted(context);
        boolean setupFinished = AuriPreferences.wasSetupCompleted(context);

        if(!setupFinished || !permissionsGranted) return null;

        if(menuLoader.isTreeItem(parentId)) return menuLoader.loadTree(parentId);

        final String playlistsId = String.valueOf(R.id.browse_playlist);
        final String searchTracksId = String.valueOf(R.id.browse_search_tracks);
        final String searchAlbumsId = String.valueOf(R.id.browse_search_albums);
        final String searchArtistsId = String.valueOf(R.id.browse_search_artists);
        final String searchGenresId = String.valueOf(R.id.browse_search_genres);

        if(playlistsId.equals(parentId)) return populatePlaylists();
        if(searchTracksId.equals(parentId)) return populateAllTracks();
        if(searchAlbumsId.equals(parentId)) return populateAlbums();
        if(searchArtistsId.equals(parentId)) return populateArtists();
        if(searchGenresId.equals(parentId)) return populateGenres();

        if(parentId.startsWith("artist_")) return populateTracksForArtist(parentId);
        if(parentId.startsWith("genre_")) return populateTracksForGenre(parentId);

        return null;
    }

    @Nullable
    public List<MediaItem> search(String query, Bundle extras)
    {
        // TODO fix search
        return null;
    }


    private List<MediaItem> populateAllTracks()
    {
        ArrayList<MediaItem> toFill = new ArrayList<>();

        for(long songId : collection.getSortedTracksByName())
        {
            String artistNames;
            if(AuriPreferences.showAllArtists)
            {
                artistNames = TextUtils.join(", ", collection.getTrack(songId).getArtistNames());
            }
            else
            {
                artistNames = collection.getTrack(songId).getArtistNames()[0];
            }

            MediaDescriptionCompat.Builder mediaItemBuilder = new MediaDescriptionCompat.Builder();
            mediaItemBuilder.setMediaId("track_"+songId);
            mediaItemBuilder.setTitle(collection.getTrack(songId).getName());
            mediaItemBuilder.setSubtitle(artistNames);

            if(AuriPreferences.showAlbumArt)
            {
                mediaItemBuilder.setIconBitmap(collection.getAlbumArt(songId));
            }

            MediaItem mediaItem = new MediaItem(mediaItemBuilder.build(), MediaItem.FLAG_PLAYABLE);

            toFill.add(mediaItem);
        }

        return toFill;
    }

    private List<MediaItem> populateAlbums()
    {
        ArrayList<MediaItem> toFill = new ArrayList<>();

        for(long albumId : collection.getSortedAlbumsByName())
        {
            MediaDescriptionCompat.Builder mediaItemBuilder = new MediaDescriptionCompat.Builder();
            mediaItemBuilder.setMediaId("album_"+albumId);
            mediaItemBuilder.setTitle(collection.getAlbum(albumId).getAlbumName());
            mediaItemBuilder.setSubtitle(collection.getArtist(collection.getAlbum(albumId).getAlbumArtistId()).getName());

            if(AuriPreferences.showAlbumArt)
            {
                mediaItemBuilder.setIconBitmap(collection.getAlbumArt(collection.getAlbum(albumId).getContainedTracks().get(0)));
            }

            MediaItem mediaItem = new MediaItem(mediaItemBuilder.build(), MediaItem.FLAG_PLAYABLE);

            toFill.add(mediaItem);
        }

        return toFill;
    }

    private List<MediaItem> populateArtists()
    {
        ArrayList<MediaItem> toFill = new ArrayList<>();

        for(long artistId : collection.getSortedArtistsByName())
        {
            Artist artist = collection.getArtist(artistId);
            if(artist == null) continue;

            Bundle extras = new Bundle();
            extras.putInt(MusicService.CONTENT_STYLE_BROWSABLE_HINT,
                    MusicService.CONTENT_STYLE_LIST_ITEM_HINT_VALUE);
            extras.putInt(MusicService.CONTENT_STYLE_PLAYABLE_HINT,
                    MusicService.CONTENT_STYLE_LIST_ITEM_HINT_VALUE);

            MediaItem mediaItem = new MediaItem(new MediaDescriptionCompat.Builder()
                    .setMediaId("artist_"+artistId)
                    .setTitle(artist.getName())
                    .setExtras(extras)
                    .build(), MediaItem.FLAG_BROWSABLE);

            toFill.add(mediaItem);
        }

        return toFill;
    }

    private List<MediaItem> populateGenres()
    {
        ArrayList<MediaItem> toFill = new ArrayList<>();

        if(collection.getSortedGenresByName() == null) return toFill;

        for(long genreId : collection.getSortedGenresByName())
        {
            Genre genre = collection.getGenre(genreId);
            if(genre == null) continue;

            MediaItem mediaItem = new MediaItem(new MediaDescriptionCompat.Builder()
                    .setMediaId("genre_"+genreId)
                    .setTitle(genre.getName())
                    .build(), MediaItem.FLAG_BROWSABLE);

            toFill.add(mediaItem);
        }

        return toFill;
    }

    private List<MediaItem> populatePlaylists()
    {
        ArrayList<MediaItem> toFill = new ArrayList<>();

        if(collection.getSortedPlaylistsByName() == null) return toFill;

        for(long playlistId : collection.getSortedPlaylistsByName())
        {
            Playlist playlist = collection.getPlaylist(playlistId);
            if(playlist == null) continue;

            MediaDescriptionCompat.Builder mediaItemBuilder = new MediaDescriptionCompat.Builder();
            mediaItemBuilder.setMediaId("playlist_"+playlistId);
            mediaItemBuilder.setTitle(playlist.getName());
            mediaItemBuilder.setSubtitle(playlist.getContainedTracks().size() + " " + context.getString(R.string.tracks));

            if(AuriPreferences.showAlbumArt && playlist.getContainedTracks().size() > 0)
            {
                mediaItemBuilder.setIconBitmap(collection.getAlbumArt(playlist.getContainedTracks().get(0)));
            }

            MediaItem mediaItem = new MediaItem(mediaItemBuilder.build(), MediaItem.FLAG_PLAYABLE);

            toFill.add(mediaItem);
        }

        return toFill;
    }

    private List<MediaItem> populateTracksForArtist(String parentId)
    {
        ArrayList<MediaItem> toFill = new ArrayList<>();

        // pattern: artist_123
        long artistId = Long.parseLong(parentId.substring(7));

        Artist artist = collection.getArtist(artistId);
        if(artist == null) return toFill;

        toFill.add(shuffleAllTracksByArtistItem(artistId, artist.getName()));

        for(long albumId : artist.getFeaturedAlbumIds())
        {
            Album album = collection.getAlbum(albumId);
            if(album == null) continue;

            for(long songId : album.getContainedTracks())
            {
                Track song = collection.getTrack(songId);
                if(song == null) continue;

                Bundle extras = new Bundle();
                extras.putString(MusicService.CONTENT_STYLE_GROUP_TITLE_HINT, album.getAlbumName());

                MediaDescriptionCompat.Builder mediaItemBuilder = new MediaDescriptionCompat.Builder();
                mediaItemBuilder.setMediaId("artisttrack_" + songId + "_" + artistId);
                mediaItemBuilder.setTitle(song.getName());
                mediaItemBuilder.setExtras(extras);
                //mediaItemBuilder.setSubtitle(song.getAlbumName());

                if(AuriPreferences.showAlbumArt)
                {
                    mediaItemBuilder.setIconBitmap(collection.getAlbumArt(songId));
                }

                MediaItem mediaItem = new MediaItem(mediaItemBuilder.build(), MediaItem.FLAG_PLAYABLE);

                toFill.add(mediaItem);
            }
        }

        return toFill;
    }

    private MediaItem shuffleAllTracksByArtistItem(long artistId, String artistName)
    {
        Bundle extras = new Bundle();
        //extras.putString(MusicService.CONTENT_STYLE_GROUP_TITLE_HINT, "Shuffle all tracks");

        MediaDescriptionCompat.Builder mediaItemBuilder = new MediaDescriptionCompat.Builder();
        mediaItemBuilder.setMediaId("artist_" + artistId);
        mediaItemBuilder.setTitle(context.getString(R.string.shuffle_artist_title) + artistName);
        mediaItemBuilder.setExtras(extras);

        return new MediaItem(mediaItemBuilder.build(), MediaItem.FLAG_PLAYABLE);
    }

    private List<MediaItem> populateTracksForGenre(String parentId)
    {
        ArrayList<MediaItem> toFill = new ArrayList<>();

        // pattern: genre_12345
        long genreId = Long.parseLong(parentId.substring(6));

        Genre genre = collection.getGenre(genreId);
        if(genre == null) return toFill;

        for(long songId : genre.getContainedTracks())
        {
            String artistNames;
            if(AuriPreferences.showAllArtists)
            {
                artistNames = TextUtils.join(", ", collection.getTrack(songId).getArtistNames());
            }
            else
            {
                artistNames = collection.getTrack(songId).getArtistNames()[0];
            }

            MediaDescriptionCompat.Builder mediaItemBuilder = new MediaDescriptionCompat.Builder();
            mediaItemBuilder.setMediaId("genretrack_" + songId + "_" + genreId);
            mediaItemBuilder.setTitle(collection.getTrack(songId).getName());
            mediaItemBuilder.setSubtitle(artistNames);

            if(AuriPreferences.showAlbumArt)
            {
                mediaItemBuilder.setIconBitmap(collection.getAlbumArt(songId));
            }

            MediaItem mediaItem = new MediaItem(mediaItemBuilder.build(), MediaItem.FLAG_PLAYABLE);

            toFill.add(mediaItem);
        }

        return toFill;
    }
}
