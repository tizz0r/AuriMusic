package de.timschubert.uwumusic.shared.controller;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.timschubert.uwumusic.shared.R;
import de.timschubert.uwumusic.shared.browser.MusicBrowser;
import de.timschubert.uwumusic.shared.search.MusicSearchUtil;
import de.timschubert.uwumusic.shared.collections.CollectionUtil;
import de.timschubert.uwumusic.shared.collections.MusicCollection;
import de.timschubert.uwumusic.shared.controller.player.MusicPlayer;
import de.timschubert.uwumusic.shared.exceptions.SetupNotFinishedException;
import de.timschubert.uwumusic.shared.mediaitems.PlayItem;
import de.timschubert.uwumusic.shared.mediaitems.Playable;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;
import de.timschubert.uwumusic.shared.search.SearchResult;
import de.timschubert.uwumusic.shared.service.MusicControllerCallback;
import de.timschubert.uwumusic.shared.session.MusicSession;

public class MusicInstanceController
{

    private static MusicInstanceController instance;
    private static final int ARTWORK_SIZE = 256;

    private MusicSession session;
    private MusicCollection collection;
    private MusicBrowser browser;
    private MusicController controller;

    private MusicInstanceController() {}

    public void init(Context context,
                     MusicController.Callback controllerCallback,
                     MusicSession musicSession)
    {
        session = musicSession;
        collection = MusicCollection.getInstance();

        try
        {
            collection.populateCollection(context, new Size(ARTWORK_SIZE, ARTWORK_SIZE));
        }
        catch (SecurityException e)
        {
            session.errorState(context.getString(R.string.error_message_permission));
        }
        catch (SetupNotFinishedException e)
        {
            session.errorState(context.getString(R.string.error_message_setup));
        }

        browser = new MusicBrowser(context);
        controller = new MusicController(context, controllerCallback);
    }

    public void release() { controller.release(); }

    public void playFromMediaId(String mediaId)
    {
        String shuffleId = String.valueOf(R.id.browse_shuffle);
        String favouriteId = String.valueOf(R.id.browse_favourite);
        String recentId = String.valueOf(R.id.browse_recent);
        String playlistPrefix = "playlist_";
        String albumPrefix = "album_";
        String trackPrefix = "track_";
        String artistPrefix = "artist_";
        String artistTrackPrefix = "artisttrack_";
        String genreTrackPrefix = "genretrack_";

        if(shuffleId.equals(mediaId)) playShuffle();
        else if(favouriteId.equals(mediaId)) playFavourites();
        else if(recentId.equals(mediaId)) playRecent();

        if(mediaId.startsWith(playlistPrefix)) MusicInstanceUtils.playPlaylist(mediaId);
        else if(mediaId.startsWith(albumPrefix)) MusicInstanceUtils.playAlbum(mediaId);
        else if(mediaId.startsWith(trackPrefix)) MusicInstanceUtils.playTrack(mediaId);
        else if(mediaId.startsWith(artistPrefix)) MusicInstanceUtils.playArtist(mediaId);
        else if(mediaId.startsWith(artistTrackPrefix)) MusicInstanceUtils.playArtistTrack(mediaId);
        else if(mediaId.startsWith(genreTrackPrefix)) MusicInstanceUtils.playGenreTrack(mediaId);
    }

    public void playFromSearch(String query, Bundle extras)
    {
        if(TextUtils.isEmpty(query))
        {
            Log.e("Auri Search", "empty search query -> shuffle");
            playShuffle();
            return;
        }

        SearchResult searchResult = MusicSearchUtil.search(query, extras);
        if(searchResult == null)
        {
            stop();
            session.errorState(R.string.error_message_no_search_results);
        }
        else
        {
            setQueueAndPlay(searchResult.getResults(), searchResult.getPosition());
        }
    }

    public void setQueueAndPlay(List<PlayItem> queue, int index)
    {
        controller.setQueueAndPlay(queue, index);
    }

    public void setQueueAndPlay(Playable playable, int index)
    {
        controller.setQueueAndPlay(playable.playables(), index);
    }

    public void setRepeatMode(MusicPlayer.RepeatMode repeatMode)
    {
        controller.setRepeatMode(repeatMode);
        updatePlaybackDefault();
    }

    public void skipToQueueId(long trackId)
    {
        if(controller.getCurrentlyPlaying().id == trackId) return;

        int index = controller.getQueue().indexOf(trackId);
        if(index == -1) return;

        controller.skipToQueueItem(index);
    }

    public void playShuffle()
    {
        List<PlayItem> shuffleQueue =
                CollectionUtil.createQueueForIds(collection.getShuffleTracks());

        int pos = new Random().nextInt(shuffleQueue.size());

        setShuffleEnabled(true);
        setQueueAndPlay(shuffleQueue, pos);
    }

    public void playRecent()
    {
        List<PlayItem> recentQueue =
                CollectionUtil.createQueueForIds(collection.getSortedTracksByDate());

        setShuffleEnabled(false);

        setQueueAndPlay(recentQueue, 0);
    }

    public void playFavourites()
    {
        List<PlayItem> favQueue =
                CollectionUtil.createQueueForIds(collection.getFavouriteTracks());

        int pos = new Random().nextInt(favQueue.size());

        setShuffleEnabled(true);
        setQueueAndPlay(favQueue, pos);
    }

    public void playLastPlayed()
    {
        List<Long> lastQueue = new ArrayList<>();

        for(long trackId : AuriPreferences.lastPlayedQueue)
        {
            if(MusicCollection.getInstance().getTrack(trackId) != null) lastQueue.add(trackId);
        }

        if(lastQueue.isEmpty()) return;

        setShuffleEnabled(AuriPreferences.lastPlayedShuffleEnabled);
        setRepeatMode(AuriPreferences.lastPlayedRepeatMode);

        setQueueAndPlay(CollectionUtil.createQueueForIds(lastQueue),
                AuriPreferences.lastPlayedPosInQueue);

        seekTo(AuriPreferences.lastPlayedPosMs);
    }

    public void onFastForward()
    {
        if(getPositionMs() + 10000 > getDuration()) return;
        seekTo(getPositionMs() + 10000);
    }

    public void onRewind()
    {
        if(getPositionMs() - 10000 < 0) return;
        seekTo(getPositionMs() - 10000);
    }

    public void seekTo(int ms)
    {
        controller.seekTo(ms);
        updatePlaybackDefault();
    }

    public void play() { controller.play(); }
    public void pause() { controller.pause(); }
    public void stop() { controller.stop(); }
    public void skipToNext() { controller.skipToNext(); }

    public void skipToPrevious()
    {
        if(controller.getPositionMs() > 5000) controller.restart();
        else controller.skipToPrevious();
    }

    public void setRating(Context context, boolean favourite)
    {
        if(getPlaying() == null) return;
        collection.setFavourite(context, getPlaying().id, favourite);
        updatePlaybackDefault();
    }

    public void setShuffleEnabled(boolean shuffleEnabled)
    {
        controller.setShuffleEnabled(shuffleEnabled);
        updatePlaybackDefault();
    }

    public void onToggleFavourite(Context context)
    {
        boolean favourite = !collection.isFavourite(controller.getCurrentlyPlaying().id);
        setRating(context, favourite);
        updatePlaybackDefault();
    }

    public void onToggleRepeatMode()
    {
        switch (controller.getRepeatMode())
        {
            case DONT_REPEAT:
                controller.setRepeatMode(MusicPlayer.RepeatMode.REPEAT_SELECTION);
                break;
            case REPEAT_SINGLE:
                controller.setRepeatMode(MusicPlayer.RepeatMode.DONT_REPEAT);
                break;
            case REPEAT_SELECTION:
                controller.setRepeatMode(MusicPlayer.RepeatMode.REPEAT_SINGLE);
                break;
        }
        updatePlaybackDefault();
    }

    public void onToggleShuffle()
    {
        controller.setShuffleEnabled(!controller.isShuffleEnabled());
        updatePlaybackDefault();
    }

    public boolean isPlaying()
    {
        return controller.isPlaying();
    }

    public boolean isShuffleEnabled()
    {
        return controller.isShuffleEnabled();
    }

    public int getPositionMs()
    {
        return controller.getPositionMs();
    }

    public int getDuration()
    {
        return controller.getDuration();
    }

    public MusicPlayer.RepeatMode getRepeatMode()
    {
        return controller.getRepeatMode();
    }

    public List<Long> getQueue()
    {
        return controller.getQueue();
    }

    public PlayItem getPlaying()
    {
        return controller.getCurrentlyPlaying();
    }

    private void updatePlaybackDefault()
    {
        session.updatePlaybackState(getPlaybackState(),
                MusicControllerCallback.generateSessionData(controller.getCurrentlyPlaying()));
    }

    private int getPlaybackState()
    {
        if(controller.isPlaying())
        {
            return PlaybackStateCompat.STATE_PLAYING;
        }
        else if(controller.getCurrentlyPlaying() != null && !controller.isPlaying())
        {
            return PlaybackStateCompat.STATE_PAUSED;
        }
        else
        {
            return PlaybackStateCompat.STATE_STOPPED;
        }
    }

    public MusicBrowser getBrowser()
    {
        return browser;
    }

    public static MusicInstanceController getInstance()
    {
        if(instance == null) instance = new MusicInstanceController();
        return instance;
    }
}
