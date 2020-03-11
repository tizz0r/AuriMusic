package de.timschubert.uwumusic.shared.mediaitems;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.uwumusic.shared.collections.MusicCollection;

public class Playlist extends MediaItem implements Playable
{
    private final long playlistId;
    private final String playlistName;
    private final List<Long> containedTracks;

    private Playlist(Builder builder)
    {
        super(builder.playlistId, builder.playlistName);

        playlistId = builder.playlistId;
        playlistName = builder.playlistName;
        containedTracks = builder.containedTracks;
    }

    public long getId() { return playlistId; }
    public String getName() { return playlistName; }
    public List<Long> getContainedTracks() { return containedTracks; }

    @Override
    public List<PlayItem> playables()
    {
        MusicCollection collection = MusicCollection.getInstance();
        List<PlayItem> playItems = new ArrayList<>();

        for(long trackId : containedTracks)
        {
            Track track = collection.getTrack(trackId);
            playItems.add(new PlayItem(track.getId(), track.getUri()));
        }
        return playItems;
    }

    public static class Builder
    {
        private long playlistId;
        private String playlistName;
        private List<Long> containedTracks;

        public Builder(long playlistId)
        {
            this.playlistId = playlistId;
            playlistName = "";
            containedTracks = new ArrayList<>();
        }

        public Builder setName(String playlistName)
        {
            this.playlistName = playlistName;
            return this;
        }

        public Builder setContainedTracks(List<Long> containedTracks)
        {
            this.containedTracks = containedTracks;
            return this;
        }

        public Playlist build()
        {
            return new Playlist(this);
        }
    }
}
