package de.timschubert.uwumusic.shared.session;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.uwumusic.shared.mediaitems.PlayItem;
import de.timschubert.uwumusic.shared.controller.player.MusicPlayer;

public class SessionData
{
    boolean isPlaying;
    boolean isFavourite;
    boolean shuffleEnabled;
    int positionMs;
    long itemId;
    long duration;
    String title;
    String subtitle;
    MusicPlayer.RepeatMode repeatMode;
    Bitmap artwork;
    List<MetaQueueItem> queue;

    private SessionData(final Builder builder)
    {
        isPlaying = builder.isPlaying;
        isFavourite = builder.isFavourite;
        shuffleEnabled = builder.shuffleEnabled;
        positionMs = builder.positionMs;
        itemId = builder.itemId;
        duration = builder.duration;
        title = builder.title;
        subtitle = builder.subtitle;
        repeatMode = builder.repeatMode;
        artwork = builder.artwork;
        queue = builder.queue;
    }

    public static class Builder
    {
        private boolean isPlaying;
        private boolean isFavourite;
        private boolean shuffleEnabled;
        private int positionMs;
        private long itemId;
        private long duration;
        private String title;
        private String subtitle;
        private MusicPlayer.RepeatMode repeatMode;
        private Bitmap artwork;
        private List<MetaQueueItem> queue;

        public Builder()
        {
            isPlaying = false;
            isFavourite = false;
            shuffleEnabled = false;
            positionMs = -1;
            itemId = PlayItem.ERROR_ID;
            duration = -1L;
            title = "";
            subtitle = "";
            repeatMode = MusicPlayer.RepeatMode.DONT_REPEAT;
            artwork = null;
            queue = new ArrayList<>();
        }

        public Builder setPlaying(boolean isPlaying)
        {
            this.isPlaying = isPlaying;
            return this;
        }

        public Builder setFavourite(boolean isFavourite)
        {
            this.isFavourite = isFavourite;
            return this;
        }

        public Builder setShuffleEnabled(boolean shuffleEnabled)
        {
            this.shuffleEnabled = shuffleEnabled;
            return this;
        }

        public Builder setPosition(int positionMs)
        {
            this.positionMs = positionMs;
            return this;
        }

        public Builder setItemId(long itemId)
        {
            this.itemId = itemId;
            return this;
        }

        public Builder setDuration(long duration)
        {
            this.duration = duration;
            return this;
        }

        public Builder setTitle(String title)
        {
            this.title = title;
            return this;
        }

        public Builder setSubtitle(String subtitle)
        {
            this.subtitle = subtitle;
            return this;
        }

        public Builder setRepeatMode(MusicPlayer.RepeatMode repeatMode)
        {
            this.repeatMode = repeatMode;
            return this;
        }

        public Builder setArtwork(Bitmap artwork)
        {
            this.artwork = artwork;
            return this;
        }

        public Builder setQueue(List<MetaQueueItem> queue)
        {
            this.queue = queue;
            return this;
        }

        public SessionData build()
        {
            return new SessionData(this);
        }
    }
}
