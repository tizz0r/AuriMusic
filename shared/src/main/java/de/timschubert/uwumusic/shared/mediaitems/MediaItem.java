package de.timschubert.uwumusic.shared.mediaitems;

public abstract class MediaItem
{
    private long mediaId;
    private String mediaName;

    MediaItem(long id, String displayName)
    {
        mediaId = id;
        mediaName = displayName;
    }

    public long getMediaId() { return mediaId; }
    public String getMediaName() { return mediaName; }
}
