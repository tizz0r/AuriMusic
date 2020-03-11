package de.timschubert.uwumusic.shared.session;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.io.IOException;
import java.util.ArrayList;

import de.timschubert.uwumusic.shared.R;
import de.timschubert.uwumusic.shared.collections.MusicCollection;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;

public class MusicSession extends MediaSessionCompat
{
    private static String ACTION_FAST_FORWARD;
    private static String ACTION_REWIND;
    private static String ACTION_FAVOURITE;
    private static String ACTION_REPEAT_MODE;
    private static String ACTION_SHUFFLE;

    private Context context;

    public MusicSession(Context context, String tag)
    {
        super(context, tag);
        this.context = context;

        ACTION_FAST_FORWARD = context.getString(R.string.custom_action_fast_forward);
        ACTION_REWIND = context.getString(R.string.custom_action_rewind);
        ACTION_FAVOURITE = context.getString(R.string.custom_action_favourite);
        ACTION_REPEAT_MODE = context.getString(R.string.custom_action_repeat_mode);
        ACTION_SHUFFLE = context.getString(R.string.custom_action_shuffle);

        setRatingType(RatingCompat.RATING_HEART);
        setActive(true);
    }

    public void updatePlaybackState(int state, SessionData data)
    {
        if(state == PlaybackStateCompat.STATE_ERROR || data == null)
        {
            errorState(null);
            return;
        }

        long availableActions = PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_STOP |
                PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_SEEK_TO |
                PlaybackStateCompat.ACTION_SET_RATING |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE |
                PlaybackStateCompat.ACTION_SET_REPEAT_MODE;

        if(data.positionMs < 0)
        {
            data.positionMs = (int) PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        }

        availableActions |= data.isPlaying ? PlaybackStateCompat.ACTION_PAUSE :
                PlaybackStateCompat.ACTION_PLAY;

        boolean favEnabled = AuriPreferences.customActionFavEnabled;
        boolean repeatEnabled = AuriPreferences.customActionRepeatEnabled;
        boolean shuffleEnabled = AuriPreferences.customActionShuffleEnabled;
        boolean fastForwardEnabled = AuriPreferences.customActionFastForwardEnabled;

        int heartIcon = R.drawable.ic_hearts_outline;
        int repeatIcon = R.drawable.ic_repeat_none;
        int shuffleIcon = R.drawable.ic_shuffle_disabled_24dp;
        int fastForwardIcon = R.drawable.ic_fast_forward_24dp;
        int rewindIcon = R.drawable.ic_fast_rewind_24dp;

        if(favEnabled && data.isFavourite) heartIcon = R.drawable.ic_hearts_filled;
        if(shuffleEnabled && data.shuffleEnabled) shuffleIcon = R.drawable.ic_shuffle_24dp;

        if(repeatEnabled)
        {
            switch (data.repeatMode)
            {
                default:
                case DONT_REPEAT:
                    break;
                case REPEAT_SINGLE:
                    repeatIcon = R.drawable.ic_repeat_one;
                    break;
                case REPEAT_SELECTION:
                    repeatIcon = R.drawable.ic_repeat;
                    break;
            }
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(availableActions);
        stateBuilder.setState(state, data.positionMs, 1f);
        stateBuilder.setActiveQueueItemId(data.itemId);
        if(favEnabled) stateBuilder.addCustomAction(ACTION_FAVOURITE, "HEART", heartIcon);
        if(repeatEnabled) stateBuilder.addCustomAction(ACTION_REPEAT_MODE, "REPEAT", repeatIcon);
        if(shuffleEnabled) stateBuilder.addCustomAction(ACTION_SHUFFLE, "SHUFFLE", shuffleIcon);

        if(fastForwardEnabled)
        {
            stateBuilder.addCustomAction(ACTION_REWIND, "RW", rewindIcon)
                    .addCustomAction(ACTION_FAST_FORWARD, "FF", fastForwardIcon);
        }

        setPlaybackState(stateBuilder.build());
    }

    public void errorState(@Nullable String customMsg)
    {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setState(PlaybackStateCompat.STATE_ERROR,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1f);
        if(customMsg != null)
        {
            stateBuilder.setErrorMessage(PlaybackStateCompat.ERROR_CODE_APP_ERROR, customMsg);
        }
        setPlaybackState(stateBuilder.build());
    }

    public void errorState(@StringRes int resId)
    {
        String error = context.getString(resId);
        errorState(error);
    }

    public void updateMetadata(SessionData data)
    {
        // TODO CHANGE
        data.artwork = loadThumbnail(context, MusicCollection.getInstance().getTrack(data.itemId).getAlbumId());

        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, data.title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, data.subtitle)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, data.artwork)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, data.duration)
                .build();

        ArrayList<QueueItem> musicQueue = new ArrayList<>();

        for(MetaQueueItem item : data.queue)
        {
            musicQueue.add(new QueueItem(new MediaDescriptionCompat.Builder()
                    .setMediaId(String.valueOf(item.id))
                    .setTitle(item.title)
                    .setSubtitle(item.subtitle)
                    .build(), item.id));
        }

        setMetadata(metadata);
        setQueue(musicQueue);
        setQueueTitle(context.getString(R.string.queue_title));
    }

    private static Bitmap loadThumbnail(Context context, long albumId)
    {
        Bitmap albumArt;

        try
        {
            Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(artworkUri, albumId);

            albumArt = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        }
        catch(IOException | NullPointerException e)
        {
            albumArt = null;
        }

        return albumArt;
    }
}
