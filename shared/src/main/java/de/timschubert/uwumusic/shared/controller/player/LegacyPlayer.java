package de.timschubert.uwumusic.shared.controller.player;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.timschubert.uwumusic.shared.mediaitems.PlayItem;

public class LegacyPlayer implements MusicPlayer
{

    private boolean prepared;
    private boolean playOnReady;
    private boolean shuffleEnabled;

    private final Context context;
    private final MediaPlayer mediaPlayer;
    private List<PlayItem> queue;
    private RepeatMode repeatMode;
    private MusicPlayer.Callback callback;
    private PlayItem currentlyPlaying;

    public LegacyPlayer(Context c, MusicPlayer.Callback playerCallback)
    {
        context = c;
        callback = playerCallback;

        prepared = false;
        playOnReady = false;
        shuffleEnabled = false;
        repeatMode = RepeatMode.DONT_REPEAT;

        LegacyListener legacyListener = new LegacyListener();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
        mediaPlayer.setOnPreparedListener(legacyListener);
        mediaPlayer.setOnCompletionListener(legacyListener);
        mediaPlayer.setOnErrorListener(legacyListener);

        queue = new ArrayList<>();
        currentlyPlaying = null;
    }

    @Override
    public void play()
    {
        if(currentlyPlaying != null)
        {
            if(prepared)
            {
                mediaPlayer.start();
                callback.onPlay();
            }
            else
            {
                playOnReady = true;
            }
        }
        else if(!isQueueEmpty())
        {
            playInQueue(0);
        }
    }

    @Override
    public void pause()
    {
        if(!prepared || !isPlaying()) return;

        playOnReady = false;
        mediaPlayer.pause();
        callback.onPause();
    }

    @Override
    public void stop()
    {
        prepared = false;
        playOnReady = false;
        mediaPlayer.stop();
        callback.onStop();
    }

    @Override
    public void skipToNext()
    {
        if(hasNext())
        {
            int currentPosInQueue = queue.indexOf(currentlyPlaying);
            playInQueue(currentPosInQueue + 1);
        }
        else
        {
            pause();
            callback.onPlaylistEnd();
        }
    }

    @Override
    public void skipToPrevious()
    {
        if(!hasPrevious()) return;

        int currentPosInQueue = queue.indexOf(currentlyPlaying);
        playInQueue(currentPosInQueue - 1);
    }

    @Override
    public void setRepeatMode(@NonNull RepeatMode mode) { repeatMode = mode; }

    @Override
    public void setShuffleEnabled(boolean enabled) { shuffleEnabled = enabled; }

    @Override
    public void playInQueue(int index)
    {
        if(index > queue.size()-1) return;

        prepared = false;
        currentlyPlaying = queue.get(index);
        playOnReady = true;

        try
        {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, currentlyPlaying.uri);
            mediaPlayer.prepareAsync();

            callback.onPrepare();
        }
        catch (IOException e) { callback.onError(); }
    }

    @Override
    public void seekToMs(int ms)
    {
        mediaPlayer.seekTo(ms);
    }



    @Override
    public void setQueue(@NonNull List<PlayItem> list)
    {
        queue.clear();
        queue.addAll(list);
        currentlyPlaying = null;
    }

    @Override
    public void setQueueAndPlay(@NonNull List<PlayItem> queue, int index) {

    }

    @Override
    public void release() { mediaPlayer.release(); }

    @Override
    public boolean isShuffleEnabled() { return shuffleEnabled; }

    @Override
    public boolean hasNext()
    {
        if(currentlyPlaying == null) return false;

        int currentPosInQueue = queue.indexOf(currentlyPlaying);
        return queue.size() > currentPosInQueue+1;
    }

    @Override
    public boolean hasPrevious()
    {
        if(currentlyPlaying == null) return false;

        int currentPosInQueue = queue.indexOf(currentlyPlaying);
        return currentPosInQueue > 0;
    }

    @Override
    public boolean isPlaying() { return prepared && mediaPlayer.isPlaying(); }

    @Override
    public boolean isQueueEmpty() { return queue.isEmpty(); }

    @Override
    public int getPositionMs() { return prepared ? mediaPlayer.getCurrentPosition() : -1; }

    @Override
    public long getDuration() { return prepared ? mediaPlayer.getDuration() : -1L; }

    @Override
    public List<Long> getQueue()
    {
        List<Long> queueLong = new ArrayList<>();

        for(PlayItem item : queue) { queueLong.add(item.id); }

        return queueLong;
    }

    @Override
    @Nullable public PlayItem getPlayingItem() { return currentlyPlaying; }

    @Override
    public RepeatMode getRepeatMode() { return repeatMode; }

    private class LegacyListener implements
            MediaPlayer.OnErrorListener,
            MediaPlayer.OnPreparedListener,
            MediaPlayer.OnCompletionListener
    {

        @Override
        public void onCompletion(MediaPlayer mp)
        {
            switch(repeatMode)
            {
                case DONT_REPEAT:
                    skipToNext();
                    break;

                case REPEAT_SINGLE:
                    seekToMs(0);
                    break;

                case REPEAT_SELECTION:
                    if(hasNext())
                    {
                        skipToNext();
                    }
                    else
                    {
                        playInQueue(0);
                    }
                    break;
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra)
        {
            callback.onError();
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mp)
        {
            prepared = true;
            if(playOnReady)
            {
                mp.start();
                callback.onPlay();
                playOnReady = false;
            }
        }
    }
}
