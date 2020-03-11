package de.timschubert.uwumusic.shared.service;

import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.uwumusic.shared.collections.CollectionUtil;
import de.timschubert.uwumusic.shared.collections.MusicCollection;
import de.timschubert.uwumusic.shared.controller.MusicController;
import de.timschubert.uwumusic.shared.controller.MusicInstanceController;
import de.timschubert.uwumusic.shared.mediaitems.PlayItem;
import de.timschubert.uwumusic.shared.mediaitems.Track;
import de.timschubert.uwumusic.shared.session.MetaQueueItem;
import de.timschubert.uwumusic.shared.session.MusicSession;
import de.timschubert.uwumusic.shared.session.SessionData;

public class MusicControllerCallback implements MusicController.Callback
{

    private MusicSession musicSession;
    private Context context;

    private BecomingNoisyReceiver noisyReceiver;

    MusicControllerCallback(Context context, MusicSession session)
    {
        this.context = context;
        musicSession = session;

        noisyReceiver = new BecomingNoisyReceiver(context);
    }

    @Override
    public void release()
    {
        noisyReceiver.unregister();
    }

    @Override
    public void onPrepare(PlayItem preparing)
    {
        SessionData data = generateSessionData(preparing);

        musicSession.updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING, data);
        musicSession.updateMetadata(data);
        noisyReceiver.register();
    }

    @Override
    public void onPlay(PlayItem playing)
    {
        SessionData data = generateSessionData(playing);

        musicSession.updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, data);
        musicSession.updateMetadata(data);
        noisyReceiver.register();
    }

    @Override
    public void onPause(PlayItem paused)
    {
        SessionData data = generateSessionData(paused);

        musicSession.updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, data);
        noisyReceiver.unregister();
    }

    @Override
    public void onStop()
    {
        musicSession.updatePlaybackState(PlaybackStateCompat.STATE_STOPPED,
                generateSessionData(null));
        noisyReceiver.unregister();
    }

    @Override
    public void onError()
    {
        musicSession.updatePlaybackState(PlaybackStateCompat.STATE_ERROR, null);
        noisyReceiver.unregister();
    }

    public static SessionData generateSessionData(@Nullable PlayItem playItem)
    {
        MusicInstanceController controller = MusicInstanceController.getInstance();
        MusicCollection collection = MusicCollection.getInstance();

        long id = playItem != null ? playItem.id : PlayItem.ERROR_ID;
        Track track = collection.getTrack(id);

        SessionData.Builder builder = new SessionData.Builder();
        builder.setItemId(id);

        if(track != null)
        {
            builder.setTitle(track.getName())
                    .setSubtitle(CollectionUtil.getArtistNamesComma(track.getId()))
                    .setArtwork(null)
                    .setDuration(controller.getDuration())
                    .setFavourite(collection.isFavourite(id))
                    .setPlaying(controller.isPlaying())
                    .setShuffleEnabled(controller.isShuffleEnabled())
                    .setRepeatMode(controller.getRepeatMode())
                    .setPosition(controller.getPositionMs());

            List<MetaQueueItem> queue = new ArrayList<>();
            for(Long queueId : controller.getQueue())
            {
                Track queueTrack = collection.getTrack(queueId);

                queue.add(new MetaQueueItem.Builder()
                        .setId(queueTrack.getId())
                        .setTitle(queueTrack.getName())
                        .setSubtitle(CollectionUtil.queueTrackPosition(queueTrack.getId()))
                        .build());
            }

            builder.setQueue(queue);
        }

        return builder.build();
    }
}
