package de.timschubert.uwumusic.shared.controller;

import android.content.Context;
import android.media.AudioManager;

import java.util.List;

import de.timschubert.uwumusic.shared.controller.player.MusicPlayer;
import de.timschubert.uwumusic.shared.controller.player.MyExoPlayer;
import de.timschubert.uwumusic.shared.mediaitems.PlayItem;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;

public class MusicController implements AudioManager.OnAudioFocusChangeListener
{

    private boolean pausedByFocusLoss;

    private MusicPlayer player;
    private FocusManager focusManager;
    private MusicController.Callback callback;

    MusicController(Context context, MusicController.Callback controllerCallback)
    {
        callback = controllerCallback;

        player = new MyExoPlayer(context, new MusicPlayerCallback());
        focusManager = new FocusManager(context, this);
    }

    void play()
    {
        if(!focusManager.requestFocus()) return;

        if(!player.isQueueEmpty()) player.play();
        else MusicInstanceController.getInstance().playLastPlayed(); // Probably first play -> continue last played track
    }

    void pause()
    {
        player.pause();
    }

    void stop()
    {
        player.stop();
        focusManager.abandonFocus();
    }

    void restart()
    {
        if(!focusManager.requestFocus()) return;
        player.seekToMs(0);
        player.play();
    }

    public void skipToNext()
    {
        if(!focusManager.requestFocus()) return;
        player.skipToNext();
        player.play();
    }

    public void skipToPrevious()
    {
        if(!focusManager.requestFocus()) return;
        player.skipToPrevious();
        player.play();
    }

    public void skipToQueueItem(int index)
    {
        if(!focusManager.requestFocus()) return;
        if(index >= getQueue().size()) return;
        player.playInQueue(index);
    }

    public void setQueueAndPlay(List<PlayItem> queue, int index)
    {
        if(!focusManager.requestFocus()) return;
        player.setQueueAndPlay(queue, index);
    }

    public void seekTo(int ms)
    {
        player.seekToMs(ms);
    }

    public void release()
    {
        player.release();
        callback.release();
    }

    public void setQueue(List<PlayItem> queue)
    {
        player.setQueue(queue);
    }

    public void setRepeatMode(MusicPlayer.RepeatMode repeatMode)
    {
        player.setRepeatMode(repeatMode);
    }

    public void setShuffleEnabled(boolean enabled)
    {
        player.setShuffleEnabled(enabled);
    }

    public boolean isPlaying() { return player.isPlaying(); }
    public boolean isShuffleEnabled() { return player.isShuffleEnabled(); }
    public PlayItem getCurrentlyPlaying() { return player.getPlayingItem(); }
    public int getDuration() { return (int)player.getDuration(); }
    public MusicPlayer.RepeatMode getRepeatMode() { return player.getRepeatMode(); }
    public List<Long> getQueue() { return player.getQueue(); }
    public int getPositionMs()
    {
        if(getCurrentlyPlaying() != null) return player.getPositionMs();
        else return -1;
    }

    @Override
    public void onAudioFocusChange(int focusChange)
    {
        switch (focusChange)
        {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                if(player.isPlaying()) pausedByFocusLoss = true;
                pause();
                break;

            case AudioManager.AUDIOFOCUS_GAIN:

                if(pausedByFocusLoss) play();
                pausedByFocusLoss = false;
                break;
        }
    }

    private class MusicPlayerCallback implements MusicPlayer.Callback
    {
        @Override
        public void onPrepare() { callback.onPrepare(player.getPlayingItem()); }

        @Override
        public void onPlay() { callback.onPlay(player.getPlayingItem()); }

        @Override
        public void onPause() { callback.onPause(player.getPlayingItem()); }

        @Override
        public void onStop() { callback.onStop(); }

        @Override
        public void onPlaylistEnd()
        {
            if(AuriPreferences.shuffleOnError)
            {
                MusicInstanceController.getInstance().playShuffle();
            }
            else
            {
                stop();
            }
        }

        @Override
        public void onError() { callback.onError(); }
    }

    public interface Callback
    {
        void onPrepare(PlayItem preparing);
        void onPlay(PlayItem playing);
        void onPause(PlayItem paused);
        void onStop();
        void onError();
        void release();
    }
}
