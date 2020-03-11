package de.timschubert.uwumusic.shared.mediaitems;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.uwumusic.shared.collections.MusicCollection;

public class Genre extends MediaItem implements Playable
{
    private final long genreId;
    private final String genreName;
    private List<Long> containedTracks;

    public Genre(long genreId, @NonNull String genreName)
    {
        super(genreId, genreName);

        this.genreId = genreId;
        this.genreName = genreName;
        this.containedTracks = new ArrayList<>();
    }

    public void addTrack(Track track)
    {
        for(Long oId : containedTracks) { if(oId.equals(track.getId())) return; }

        containedTracks.add(track.getId());
    }

    public long getId() { return genreId; }
    public String getName() { return genreName; }
    public List<Long> getContainedTracks() { return containedTracks; }

    @Override
    @NonNull
    public List<PlayItem> playables()
    {
        MusicCollection collection = MusicCollection.getInstance();
        List<PlayItem> list = new ArrayList<>();

        for(Long trackId : containedTracks)
        {
            list.add(new PlayItem(trackId, collection.getTrack(trackId).getUri()));
        }
        return list;
    }
}
