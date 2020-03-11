package de.timschubert.uwumusic.shared.mediaitems;

import android.net.Uri;

public class PlayItem
{
    public static final long ERROR_ID = -1;

    public long id;
    public Uri uri;

    public PlayItem(long id, Uri uri) { this.id = id; this.uri = uri; }
}
