package de.timschubert.uwumusic.shared.mediaitems;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.timschubert.uwumusic.shared.collections.MusicCollection;

public class Album extends MediaItem implements Playable
{
    private final long albumId;
    private final long albumArtistId;
    private final String albumName;
    private List<Long> containedTracks;

    private Bitmap artwork;

    public Album(long albumId,
                 long albumArtistId,
                 @NonNull String albumName)
    {
        super(albumId, albumName);

        this.albumId = albumId;
        this.albumArtistId = albumArtistId;
        this.containedTracks = new ArrayList<>();
        this.albumName = albumName;
        this.artwork = null;
    }

    public void addTrack(long trackId)
    {
        for(Long oId : containedTracks) { if(oId.equals(trackId)) return; }

        containedTracks.add(trackId);
        sortByPosition();
    }

    public void setArtwork(Bitmap artwork) { this.artwork = artwork; }

    private void sortByPosition()
    {
        final MusicCollection collection = MusicCollection.getInstance();

        Collections.sort(containedTracks, new Comparator<Long>()
        {
            @Override
            public int compare(Long o1, Long o2)
            {
                long o1Pos = collection.getTrack(o1).getPosition();
                long o2Pos = collection.getTrack(o2).getPosition();

                return Long.compare(o1Pos, o2Pos);
            }
        });
    }

    public long getId() { return albumId; }
    public long getAlbumArtistId() { return albumArtistId; }
    @NonNull public String getAlbumName() { return albumName; }
    @Nullable public Bitmap getArtwork() { return artwork; }
    @NonNull public List<Long> getContainedTracks() { return containedTracks; }

    @Override
    public List<PlayItem> playables()
    {
        MusicCollection collection = MusicCollection.getInstance();

        List<PlayItem> list = new ArrayList<>();
        for(long trackId : containedTracks)
        {
            Track track = collection.getTrack(trackId);
            list.add(new PlayItem(track.getId(), track.getUri()));
        }
        return list;
    }
}
