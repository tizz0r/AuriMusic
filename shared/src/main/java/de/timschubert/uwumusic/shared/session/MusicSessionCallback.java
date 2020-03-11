package de.timschubert.uwumusic.shared.session;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.Collections;

import de.timschubert.uwumusic.shared.R;
import de.timschubert.uwumusic.shared.collections.MusicCollection;
import de.timschubert.uwumusic.shared.controller.MusicInstanceController;
import de.timschubert.uwumusic.shared.controller.player.MusicPlayer;
import de.timschubert.uwumusic.shared.mediaitems.PlayItem;

public class MusicSessionCallback extends MediaSessionCompat.Callback
{
    private final String HEART_ACTION;
    private final String REPEAT_ACTION;
    private final String SHUFFLE_ACTION;
    private final String FAST_FORWARD_ACTION;
    private final String REWIND_ACTION;

    private Context context;

    private MusicInstanceController instanceController;

    public MusicSessionCallback(Context context)
    {
        this.context = context;
        instanceController = MusicInstanceController.getInstance();

        HEART_ACTION = context.getString(R.string.custom_action_favourite);
        REPEAT_ACTION = context.getString(R.string.custom_action_repeat_mode);
        SHUFFLE_ACTION = context.getString(R.string.custom_action_shuffle);
        FAST_FORWARD_ACTION = context.getString(R.string.custom_action_fast_forward);
        REWIND_ACTION = context.getString(R.string.custom_action_rewind);
    }

    @Override
    public void onPlay() { instanceController.play(); }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras)
    {
        instanceController.playFromMediaId(mediaId);
    }

    @Override
    public void onPlayFromSearch(String query, Bundle extras)
    {
        instanceController.playFromSearch(query, extras);
    }

    @Override
    public void onSkipToQueueItem(long id) { instanceController.skipToQueueId(id); }

    @Override
    public void onPause() { instanceController.pause(); }

    @Override
    public void onSkipToNext() {
        instanceController.skipToNext();
    }

    @Override
    public void onSkipToPrevious() {
        instanceController.skipToPrevious();
    }

    @Override
    public void onStop() {
        instanceController.stop();
    }

    @Override
    public void onSeekTo(long pos) {
        instanceController.seekTo((int)pos);
    }

    @Override
    public void onSetRating(RatingCompat rating)
    {
        instanceController.setRating(context, rating.hasHeart());
    }

    @Override
    public void onSetRepeatMode(int repeatMode)
    {
        MusicPlayer.RepeatMode mode = MusicPlayer.RepeatMode.DONT_REPEAT;
        switch (repeatMode)
        {
            default:
            case PlaybackStateCompat.REPEAT_MODE_NONE:
            case PlaybackStateCompat.REPEAT_MODE_INVALID:
                break;

            case PlaybackStateCompat.REPEAT_MODE_GROUP:
            case PlaybackStateCompat.REPEAT_MODE_ALL:
                mode = MusicPlayer.RepeatMode.REPEAT_SELECTION;
                break;

            case PlaybackStateCompat.REPEAT_MODE_ONE:
                mode = MusicPlayer.RepeatMode.REPEAT_SINGLE;
                break;
        }
        instanceController.setRepeatMode(mode);
    }

    @Override
    public void onSetShuffleMode(int shuffleMode)
    {
        boolean shuffleEnabled = false;

        switch(shuffleMode)
        {
            default:
            case PlaybackStateCompat.SHUFFLE_MODE_NONE:
            case PlaybackStateCompat.SHUFFLE_MODE_INVALID:
                break;

            case PlaybackStateCompat.SHUFFLE_MODE_ALL:
            case PlaybackStateCompat.SHUFFLE_MODE_GROUP:
                shuffleEnabled = true;
                break;
        }
        instanceController.setShuffleEnabled(shuffleEnabled);
    }

    @Override
    public void onCustomAction(String action, Bundle extras)
    {
        if(HEART_ACTION.equals(action))
        {
            instanceController.onToggleFavourite(context);
        }
        else if(REPEAT_ACTION.equals(action))
        {
            instanceController.onToggleRepeatMode();
        }
        else if(SHUFFLE_ACTION.equals(action))
        {
            instanceController.onToggleShuffle();
        }
        else if(FAST_FORWARD_ACTION.equals(action))
        {
            instanceController.onFastForward();
        }
        else if(REWIND_ACTION.equals(action))
        {
            instanceController.onRewind();
        }
    }
}
