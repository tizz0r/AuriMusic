package de.timschubert.uwumusic.shared.mediaitems;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.timschubert.uwumusic.shared.collections.MusicCollection;

public class Artist extends MediaItem implements Playable
{

    private final long artistId;
    private final String artistName;
    private List<Long> publishedTrackIds;
    private List<Long> featuredAlbumIds;

    public Artist(long artistId, String artistName)
    {
        super(artistId, artistName);

        this.artistId = artistId;
        this.artistName = artistName;
        this.publishedTrackIds = new ArrayList<>();
        this.featuredAlbumIds = new ArrayList<>();
    }

    public void addTrack(Track track)
    {
        for(Long oId : publishedTrackIds) { if(oId.equals(track.getId())) return; }

        publishedTrackIds.add(track.getId());
        addToFeaturedAlbums(track);

        sortTracksAndAlbums();
    }

    public long getId() { return artistId; }
    public String getName() { return artistName; }
    public List<Long> getPublishedTrackIds() { return publishedTrackIds; }
    public List<Long> getFeaturedAlbumIds() { return featuredAlbumIds; }

    public List<Long> getArtistTracksForAlbum(long albumId)
    {
        List<Long> artistTrackIds = new ArrayList<>();
        if(!featuredAlbumIds.contains(albumId)) return artistTrackIds;

        MusicCollection collection = MusicCollection.getInstance();
        artistTrackIds.addAll(collection.getAlbum(albumId).getContainedTracks());

        return artistTrackIds;
    }


    private void addToFeaturedAlbums(Track track)
    {
        for(Long oId : featuredAlbumIds){ if(oId.equals(track.getAlbumId())) return; }
        featuredAlbumIds.add(track.getAlbumId());
    }

    private void sortTracksAndAlbums()
    {
        final MusicCollection collection = MusicCollection.getInstance();

        Collections.sort(featuredAlbumIds, new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                String o1Title = collection.getAlbum(o1).getAlbumName();
                String o2Title = collection.getAlbum(o2).getAlbumName();

                return o1Title.compareToIgnoreCase(o2Title);
            }
        });

        List<Long> publishedTrackIdsSorted = new ArrayList<>();
        for(Long albumId : featuredAlbumIds)
        {
            for(long trackId : collection.getAlbum(albumId).getContainedTracks())
            {
                if(publishedTrackIds.contains(trackId)) publishedTrackIdsSorted.add(trackId);
            }
        }

        publishedTrackIds.clear();
        publishedTrackIds.addAll(publishedTrackIdsSorted);
    }

    @Override
    public List<PlayItem> playables()
    {
        List<PlayItem> playItems = new ArrayList<>();
        for(long trackId : publishedTrackIds)
        {
            Uri uri = MusicCollection.getInstance().getTrack(trackId).getUri();
            playItems.add(new PlayItem(trackId, uri));
        }
        return playItems;
    }
}
