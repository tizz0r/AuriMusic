package de.timschubert.uwumusic.shared.controller.player;

import androidx.annotation.NonNull;

import java.util.List;

import de.timschubert.uwumusic.shared.mediaitems.PlayItem;

public interface MusicPlayer
{
    void play();
    void pause();
    void stop();
    void skipToNext();
    void skipToPrevious();
    void setRepeatMode(@NonNull RepeatMode mode);
    void setShuffleEnabled(boolean enabled);
    void playInQueue(int index);
    void seekToMs(int ms);
    void setQueue(@NonNull List<PlayItem> queue);
    void setQueueAndPlay(@NonNull List<PlayItem> queue, int index);
    void release();

    boolean isShuffleEnabled();
    boolean hasNext();
    boolean hasPrevious();
    boolean isPlaying();
    boolean isQueueEmpty();

    int getPositionMs();

    long getDuration();

    List<Long> getQueue();
    PlayItem getPlayingItem();
    RepeatMode getRepeatMode();

    enum RepeatMode
    {
        DONT_REPEAT, REPEAT_SELECTION, REPEAT_SINGLE
    }

    interface Callback
    {
        void onPrepare();
        void onPlay();
        void onPause();
        void onStop();
        void onPlaylistEnd();
        void onError();
    }
}
