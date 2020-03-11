package de.timschubert.uwumusic.shared.service;

import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import java.util.List;

import de.timschubert.uwumusic.shared.R;
import de.timschubert.uwumusic.shared.controller.MusicInstanceController;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;
import de.timschubert.uwumusic.shared.session.MusicSession;
import de.timschubert.uwumusic.shared.session.MusicSessionCallback;

public class MusicService extends MediaBrowserServiceCompat
{

    public static String rootId;

    private static final String SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED";
    private static final String CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED";
    public static final String CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT";
    public static final String CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT";
    public static final String CONTENT_STYLE_GROUP_TITLE_HINT = "android.media.browse.CONTENT_STYLE_GROUP_TITLE_HINT";
    public static final int CONTENT_STYLE_LIST_ITEM_HINT_VALUE = 1;
    public static final int CONTENT_STYLE_GRID_ITEM_HINT_VALUE = 2;

    private MusicSession mSession;
    private MusicInstanceController instanceController;

    @Override
    public void onCreate()
    {
        super.onCreate();

        rootId = getString(R.string.browser_root_id);
        AuriPreferences.loadPreferences(this);

        mSession = new MusicSession(this, getString(R.string.music_service_tag));
        mSession.setCallback(new MusicSessionCallback(this));
        setSessionToken(mSession.getSessionToken());

        instanceController = MusicInstanceController.getInstance();
        instanceController.init(this,
                new MusicControllerCallback(this, mSession),
                mSession);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        AuriPreferences.saveQueueInPrefs(this);
        mSession.release();
        instanceController.release();
    }

    @Override
    public void onSearch(@NonNull String query, Bundle extras,
                         @NonNull Result<List<MediaBrowserCompat.MediaItem>> result)
    {
        Log.e("Auri Search", "onSearch");
        result.sendResult(null);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid,
                                 @Nullable Bundle rootHints)
    {
        int playableStyleHint = AuriPreferences.showAlbumArt ?
                CONTENT_STYLE_GRID_ITEM_HINT_VALUE :
                CONTENT_STYLE_LIST_ITEM_HINT_VALUE;

        Bundle extras = new Bundle();
        extras.putBoolean(SEARCH_SUPPORTED, false);
        extras.putBoolean(CONTENT_STYLE_SUPPORTED, true);
        extras.putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_LIST_ITEM_HINT_VALUE);
        extras.putInt(CONTENT_STYLE_PLAYABLE_HINT, playableStyleHint);

        return new BrowserRoot(rootId, extras);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId,
                               @NonNull Result<List<MediaBrowserCompat.MediaItem>> result)
    {
        List<MediaBrowserCompat.MediaItem> resultList;

        result.detach();
        resultList = instanceController.getBrowser().browse(parentId);
        result.sendResult(resultList);
    }
}
