package de.timschubert.uwumusic.shared.collections;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LongSparseArray;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.uwumusic.shared.exceptions.SetupNotFinishedException;
import de.timschubert.uwumusic.shared.mediaitems.Album;
import de.timschubert.uwumusic.shared.mediaitems.Artist;
import de.timschubert.uwumusic.shared.mediaitems.Genre;
import de.timschubert.uwumusic.shared.mediaitems.Playlist;
import de.timschubert.uwumusic.shared.mediaitems.Track;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;
import de.timschubert.uwumusic.shared.prefs.PermissionHelper;

public class MusicCollection
{

    private static MusicCollection instance;

    private MusicCollection() {}

    private LongSparseArray<Track> allTracks;
    private LongSparseArray<Album> allAlbums;
    private LongSparseArray<Artist> allArtists;
    private LongSparseArray<Genre> allGenres;
    private LongSparseArray<Playlist> allPlaylists;

    private List<Long> sortedTracksByName;
    private List<Long> sortedTracksByDate;
    private List<Long> sortedAlbumsByName;
    private List<Long> sortedArtistsByName;
    private List<Long> sortedGenresByName;
    private List<Long> sortedPlaylistsByName;
    private List<Long> shuffleTracks;
    private List<Long> favouriteTracks;

    public void populateCollection(Context context, Size artworkSize) throws SecurityException
    {
        boolean permissionsGranted = PermissionHelper.arePermissionsGranted(context);
        boolean setupFinished = AuriPreferences.wasSetupCompleted(context);

        if(!setupFinished) throw new SetupNotFinishedException("Setup was not finished");
        if(!permissionsGranted) throw new SecurityException("Permissions were not granted");

        MusicLoader musicLoader = new MusicLoader(context);

        allTracks = musicLoader.loadAllTracks();
        allPlaylists = musicLoader.loadAllPlaylists();

        allAlbums = CollectionUtil.createAlbums(allTracks);
        allArtists = CollectionUtil.createArtists(allTracks);
        shuffleTracks = CollectionUtil.createShuffleTracks(allTracks);
        allGenres = new LongSparseArray<>();

        sortedTracksByName = CollectionUtil.sortByNameAsc(allTracks);
        sortedTracksByDate = CollectionUtil.sortByDateDesc(allTracks);
        sortedAlbumsByName = CollectionUtil.sortByNameAsc(allAlbums);
        sortedArtistsByName = CollectionUtil.sortByNameAsc(allArtists);
        sortedPlaylistsByName = CollectionUtil.sortByNameAsc(allPlaylists);
        sortedGenresByName = new ArrayList<>();

        favouriteTracks = musicLoader.loadFavouritesFromPrefs();
        musicLoader.loadArtworkAsync(context, allAlbums, artworkSize, null);
        musicLoader.loadGenresAsync(allTracks, allGenres, sortedGenresByName);

        musicLoader.release();
    }

    public void setFavourite(Context context, long trackId, boolean favourite)
    {
        if(favouriteTracks.contains(trackId) && !favourite) favouriteTracks.remove(trackId);
        else if(!favouriteTracks.contains(trackId) && favourite) favouriteTracks.add(trackId);
        else return;

        AuriPreferences.setHeartOnSong(context, trackId, favourite);
    }

    public boolean isFavourite(long trackId) { return favouriteTracks.contains(trackId); }

    public Track getTrack(long trackId) { return allTracks.get(trackId); }
    public Album getAlbum(long albumId) { return allAlbums.get(albumId); }
    public Artist getArtist(long artistId) { return allArtists.get(artistId); }
    public Genre getGenre(long genreId) { return allGenres.get(genreId); }
    public Playlist getPlaylist(long playlistId) { return allPlaylists.get(playlistId); }

    public List<Long> getSortedTracksByName() { return sortedTracksByName; }
    public List<Long> getSortedTracksByDate() { return sortedTracksByDate; }
    public List<Long> getSortedAlbumsByName() { return sortedAlbumsByName; }
    public List<Long> getSortedGenresByName() { return sortedGenresByName; }
    public List<Long> getSortedArtistsByName() { return sortedArtistsByName; }
    public List<Long> getSortedPlaylistsByName() { return sortedPlaylistsByName; }
    public List<Long> getShuffleTracks() { return shuffleTracks; }
    public List<Long> getFavouriteTracks() { return favouriteTracks; }

    public Bitmap getAlbumArt(long trackId)
    {
        return getAlbum(getTrack(trackId).getAlbumId()).getArtwork();
    }

    public static MusicCollection getInstance()
    {
        if(instance == null) instance = new MusicCollection();
        return instance;
    }
}
