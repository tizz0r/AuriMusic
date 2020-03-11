package de.timschubert.uwumusic.shared.session;

import de.timschubert.uwumusic.shared.mediaitems.PlayItem;

public class MetaQueueItem
{
    long id;
    String title;
    String subtitle;

    private MetaQueueItem(Builder builder)
    {
        id = builder.id;
        title = builder.title;
        subtitle = builder.subtitle;
    }

    public static class Builder
    {
        private long id;
        private String title;
        private String subtitle;

        public Builder()
        {
            id = PlayItem.ERROR_ID;
            title = "";
            subtitle = "";
        }

        public Builder setId(long id)
        {
            this.id = id;
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

        public MetaQueueItem build()
        {
            return new MetaQueueItem(this);
        }
    }
}
