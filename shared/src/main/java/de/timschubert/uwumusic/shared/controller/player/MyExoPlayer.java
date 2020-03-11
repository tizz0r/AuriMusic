package de.timschubert.uwumusic.shared.controller.player;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.timschubert.uwumusic.shared.R;
import de.timschubert.uwumusic.shared.collections.MusicCollection;
import de.timschubert.uwumusic.shared.mediaitems.PlayItem;

public class MyExoPlayer implements MusicPlayer
{

    private SimpleExoPlayer player;
    private DataSource.Factory dataSourceFactory;
    private ConcatenatingMediaSource queue;

    private List<PlayItem> queueManual;
    private PlayItem playingManual;

    private MusicPlayer.Callback callback;

    public MyExoPlayer(Context context, MusicPlayer.Callback playerCallback)
    {
        callback = playerCallback;

        player = new SimpleExoPlayer.Builder(context).build();
        player.addListener(new ExoListener());

        queueManual = new ArrayList<>();

        queue = new ConcatenatingMediaSource();
        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getString(R.string.app_user_agent)));
    }

    @Override
    public void play() { player.setPlayWhenReady(true); }

    @Override
    public void pause() { player.setPlayWhenReady(false); }

    @Override
    public void stop() { player.stop(); }

    @Override
    public void skipToNext() { if(hasNext()) player.next(); }

    @Override
    public void skipToPrevious() { if(hasPrevious()) player.previous(); }

    @Override
    public void setRepeatMode(@NonNull RepeatMode mode)
    {
        switch (mode)
        {
            case DONT_REPEAT:
                player.setRepeatMode(Player.REPEAT_MODE_OFF);
                break;
            case REPEAT_SINGLE:
                player.setRepeatMode(Player.REPEAT_MODE_ONE);
                break;
            case REPEAT_SELECTION:
                player.setRepeatMode(Player.REPEAT_MODE_ALL);
                break;
        }
    }

    @Override
    public void setShuffleEnabled(boolean enabled) { player.setShuffleModeEnabled(enabled); }

    @Override
    public void playInQueue(int index)
    {
        if(index <= queue.getSize()-1)
        {
            player.seekTo(index, C.TIME_UNSET);
            play();
        }
    }

    @Override
    public void seekToMs(int ms) { player.seekTo(ms); }

    @Override
    public void setQueue(@NonNull List<PlayItem> list)
    {
        buildAndPrepareQueue(list, 0);
    }

    @Override
    public void setQueueAndPlay(@NonNull List<PlayItem> list, int index)
    {
        buildAndPrepareQueue(list, index);
        play();
    }

    private void buildAndPrepareQueue(List<PlayItem> list, int index)
    {
        queueManual.clear();
        queue.clear();

        for(PlayItem item : list)
        {
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .setTag(item.id)
                    .createMediaSource(item.uri);
            queue.addMediaSource(mediaSource);
            queueManual.add(item);
        }

        if(index >= queue.getSize()) index = 0;
        if(queue.getSize() == 0)
        {
            stop();
            return;
        }

        playingManual = list.get(index);
        player.prepare(queue);
        player.seekTo(index, C.TIME_UNSET);
    }

    @Override
    public void release() { player.release(); }

    @Override
    public boolean isShuffleEnabled() { return player.getShuffleModeEnabled(); }

    @Override
    public boolean hasNext() { return player.hasNext(); }

    @Override
    public boolean hasPrevious() { return player.hasPrevious(); }

    @Override
    public boolean isPlaying()
    {
        return player.getPlaybackState() == Player.STATE_READY && player.getPlayWhenReady();
    }

    @Override
    public boolean isQueueEmpty() { return queue.getSize() == 0; }

    @Override
    public int getPositionMs() { return (int)player.getCurrentPosition(); }

    @Override
    public long getDuration() { return player.getDuration(); }

    @Override
    public List<Long> getQueue()
    {
        List<Long> currentQueue = new ArrayList<>();

        for(int i = 0; i < queue.getSize(); i ++)
        {
            currentQueue.add((Long)queue.getMediaSource(i).getTag());
        }

        return currentQueue;
    }

    @Override
    public PlayItem getPlayingItem()
    {
        if(getQueuePosition() >= 0)
        {
            return queueManual.get(getQueuePosition());
        }

        try
        {
            return playingManual;
        } catch(Exception e) { return null; }
    }

    @Override
    public RepeatMode getRepeatMode()
    {
        switch (player.getRepeatMode())
        {
            default:
            case Player.REPEAT_MODE_OFF:
                return RepeatMode.DONT_REPEAT;
            case Player.REPEAT_MODE_ONE:
                return RepeatMode.REPEAT_SINGLE;
            case Player.REPEAT_MODE_ALL:
                return RepeatMode.REPEAT_SELECTION;
        }
    }

    private int getQueuePosition()
    {
        for(int i = 0; i < queue.getSize(); i ++)
        {
            if(Objects.equals(queue.getMediaSource(i).getTag(), player.getCurrentTag()))
            {
                return i;
            }
        }

        return -1;
    }


    private class ExoListener implements Player.EventListener
    {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState)
        {
            switch (playbackState)
            {
                case Player.STATE_IDLE:
                    callback.onStop();
                    break;
                case Player.STATE_BUFFERING:
                    callback.onPrepare();
                    break;
                case Player.STATE_READY:
                    if(playWhenReady) callback.onPlay();
                    else callback.onPause();
                    break;
                case Player.STATE_ENDED:
                    callback.onPlaylistEnd();
                    break;
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason)
        {
            if(reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION)
            {
                callback.onPlay();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            callback.onError();
        }
    }
}
