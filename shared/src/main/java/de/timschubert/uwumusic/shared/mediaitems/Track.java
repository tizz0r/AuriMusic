package de.timschubert.uwumusic.shared.mediaitems;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

public class Track extends MediaItem implements Playable
{
    public static final long UNKNOWN_ID = -1L;
    public static final long UNKNOWN_DATE = -1L;
    public static final String UNKNOWN_ALBUM = "Unknown Album";
    public static final String UNKNOWN_ARTIST = "Unknown Artist";

    private long trackId;
    private long albumId;
    private long trackPosition;
    private long dateAdded;
    private long[] artistIds;
    private long[] genreIds;

    private String trackName;
    private String albumName;
    private String[] artistNames;
    private String[] genreNames;
    private Uri trackUri;

    private Track(Track.Builder builder)
    {
        super(builder.trackId, builder.trackName);

        trackId = builder.trackId;
        albumId = builder.albumId;
        trackPosition = builder.trackPosition;
        dateAdded = builder.dateAdded;
        artistIds = builder.artistIds;
        genreIds = builder.genreIds;
        trackName = builder.trackName;
        albumName = builder.albumName;
        artistNames = builder.artistNames;
        genreNames = builder.genreNames;
        trackUri = builder.trackUri;
    }

    public void setGenres(long[] genreIds, String[] genreNames)
    {
        this.genreIds = genreIds;
        this.genreNames = genreNames;
    }

    public long getId() { return trackId; }
    public long getAlbumId() { return albumId; }
    public long getPosition() { return trackPosition; }
    public long getDateAdded() { return dateAdded; }
    public long[] getArtistIds() { return artistIds; }
    public long[] getGenreIds() { return genreIds; }

    public String getName() { return trackName; }
    public String getAlbumName() { return albumName; }
    public String[] getArtistNames() { return artistNames; }
    public String[] getGenreNames() { return genreNames; }
    public Uri getUri() { return trackUri; }

    @Override
    public List<PlayItem> playables()
    {
        return Collections.singletonList(new PlayItem(trackId, trackUri));
    }

    public static class Builder
    {
        private long trackId;
        private long albumId;
        private long trackPosition;
        private long dateAdded;
        private long[] artistIds;
        private long[] genreIds;

        private String trackName;
        private String albumName;
        private String[] artistNames;
        private String[] genreNames;
        private Uri trackUri;

        public Builder()
        {
            trackId = UNKNOWN_ID;
            albumId = UNKNOWN_ID;
            trackPosition = 0;
            dateAdded = UNKNOWN_DATE;
            artistNames = new String[]{ UNKNOWN_ARTIST };
            artistIds = new long[]{ UNKNOWN_ARTIST.hashCode() };
            genreIds = new long[0];
            trackName = null;
            albumName = UNKNOWN_ALBUM;
            genreNames = new String[0];
            trackUri = null;
        }

        public Builder setId(final long trackId)
        {
            this.trackId = trackId;
            return this;
        }

        public Builder setAlbumId(final long albumId)
        {
            this.albumId = albumId;
            return this;
        }

        public Builder setTrackPosition(final long trackPosition)
        {
            this.trackPosition = trackPosition;
            return this;
        }

        public Builder setDateAdded(final long dateAdded)
        {
            this.dateAdded = dateAdded;
            return this;
        }

        public Builder setArtistIds(final long[] artistIds)
        {
            this.artistIds = artistIds;
            return this;
        }

        public Builder setArtistNames(final String[] artistNames)
        {
            this.artistNames = artistNames;
            return this;
        }

        public Builder setGenreIds(final long[] genreIds)
        {
            this.genreIds = genreIds;
            return this;
        }

        public Builder setGenreNames(final String[] genreNames)
        {
            this.genreNames = genreNames;
            return this;
        }

        public Builder setName(final String trackName)
        {
            this.trackName = trackName;
            return this;
        }

        public Builder setAlbumName(final String albumName)
        {
            this.albumName = albumName;
            return this;
        }

        public Builder setUri(Uri trackUri)
        {
            this.trackUri = trackUri;
            return this;
        }

        public Track build()
        {
            if(trackUri == null) throw new IllegalStateException("Uri cannot be null");
            if(trackId == UNKNOWN_ID) throw new IllegalStateException("Id must be set");
            if(trackName == null) throw new IllegalStateException("Title must be set");

            return new Track(this);
        }
    }
}
