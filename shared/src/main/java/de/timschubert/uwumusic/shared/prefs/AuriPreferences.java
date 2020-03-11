package de.timschubert.uwumusic.shared.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.timschubert.uwumusic.shared.R;
import de.timschubert.uwumusic.shared.controller.MusicInstanceController;
import de.timschubert.uwumusic.shared.controller.player.MusicPlayer;
import de.timschubert.uwumusic.shared.mediaitems.PlayItem;

public class AuriPreferences
{

    private static final int ENUM_REPEAT_NONE_INT = 0;
    private static final int ENUM_REPEAT_SINGLE = 1;
    private static final int ENUM_REPEAT_SELECTION = 2;

    public static boolean showAlbumArt;
    public static boolean searchEnabled;
    public static boolean shuffleOnError;
    public static boolean showAllArtists;
    public static boolean customActionFavEnabled;
    public static boolean customActionRepeatEnabled;
    public static boolean customActionShuffleEnabled;
    public static boolean customActionFastForwardEnabled;
    public static boolean lastPlayedShuffleEnabled;

    public static int excludeUnderSeconds;
    public static int lastPlayedPosInQueue;
    public static int lastPlayedPosMs;

    public static MusicPlayer.RepeatMode lastPlayedRepeatMode;
    public static List<Long> lastPlayedQueue;
    public static List<Long> shuffleExcludedPlaylistIds;


    public static void loadPreferences(Context context)
    {
        context = context.getApplicationContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        showAlbumArt = prefs.getBoolean(context.getString(R.string.preferences_showalbumart_key), true);
        searchEnabled = prefs.getBoolean(context.getString(R.string.preferences_showsearch_key), true);
        shuffleOnError = prefs.getBoolean(context.getString(R.string.preferences_shuffleonerror_key), true);
        showAllArtists = prefs.getBoolean(context.getString(R.string.preferences_showallartists_key), true);

        Set<String> excludedPlaylistsSet = prefs.getStringSet(context.getString(R.string.preferences_excludedplaylists_key),
                new HashSet<String>());
        Log.i("Excluded playlist ids", ""+excludedPlaylistsSet.toString());
        shuffleExcludedPlaylistIds = parseExcludedPlaylistIds(excludedPlaylistsSet);

        Set<String> enabledCustomActionsSet = prefs.getStringSet(context.getString(R.string.preferences_customaction_key),
                new HashSet<String>());

        customActionFavEnabled = enabledCustomActionsSet.contains(context.getString(R.string.preferences_customaction_value_favourite));
        customActionRepeatEnabled = enabledCustomActionsSet.contains(context.getString(R.string.preferences_customaction_value_repeat_mode));
        customActionShuffleEnabled = enabledCustomActionsSet.contains(context.getString(R.string.preferences_customaction_value_shuffle));
        customActionFastForwardEnabled = enabledCustomActionsSet.contains(context.getString(R.string.preferences_customaction_value_fast_forward));

        excludeUnderSeconds = prefs.getInt(context.getString(R.string.preferences_excludeshort_key), 0);

        String lastPlayedQueueString = prefs.getString(context.getString(R.string.lastplayed_queue_key), "");
        lastPlayedQueue = parseSavedQueue(lastPlayedQueueString);

        long lastPlayedSongId = prefs.getLong(context.getString(R.string.lastplayed_id_key), PlayItem.ERROR_ID);

        if(lastPlayedQueue.contains(lastPlayedSongId))
            lastPlayedPosInQueue = lastPlayedQueue.indexOf(lastPlayedSongId);
        else
            lastPlayedPosInQueue = 0;

        lastPlayedPosMs = prefs.getInt(context.getString(R.string.lastplayed_pos_key), 0);
        lastPlayedShuffleEnabled = prefs.getBoolean(context.getString(R.string.lastplayed_shuffle_key), false);

        int lastPlayedRepeatInt = prefs.getInt(context.getString(R.string.lastplayed_repeat_key), ENUM_REPEAT_NONE_INT);
        switch (lastPlayedRepeatInt)
        {
            default:
            case ENUM_REPEAT_NONE_INT:
                lastPlayedRepeatMode = MusicPlayer.RepeatMode.DONT_REPEAT;
                break;
            case ENUM_REPEAT_SINGLE:
                lastPlayedRepeatMode = MusicPlayer.RepeatMode.REPEAT_SINGLE;
                break;
            case ENUM_REPEAT_SELECTION:
                lastPlayedRepeatMode = MusicPlayer.RepeatMode.REPEAT_SELECTION;
                break;
        }
    }

    public static void saveQueueInPrefs(Context context)
    {
        context = context.getApplicationContext();

        SharedPreferences.Editor preferenceEditor =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
        MusicInstanceController controller = MusicInstanceController.getInstance();

        if(controller.getQueue().isEmpty())
        {
            preferenceEditor.putLong(context.getString(R.string.lastplayed_id_key), PlayItem.ERROR_ID);
            preferenceEditor.putString(context.getString(R.string.lastplayed_queue_key), "");
            preferenceEditor.putInt(context.getString(R.string.lastplayed_pos_key), 0);
            preferenceEditor.putBoolean(context.getString(R.string.lastplayed_shuffle_key), false);
            preferenceEditor.putInt(context.getString(R.string.lastplayed_repeat_key), ENUM_REPEAT_NONE_INT);
            preferenceEditor.apply();
            return;
        }

        ArrayList<String> currentQueueSet = new ArrayList<>();

        for(long songId : controller.getQueue())
        {
            currentQueueSet.add(String.valueOf(songId));
        }

        String currentQueueString = TextUtils.join(";", currentQueueSet);

        int repeatInt = ENUM_REPEAT_NONE_INT;
        switch (controller.getRepeatMode())
        {
            default:
            case DONT_REPEAT:
                break;
            case REPEAT_SINGLE:
                repeatInt = ENUM_REPEAT_SINGLE;
                break;
            case REPEAT_SELECTION:
                repeatInt = ENUM_REPEAT_SELECTION;
                break;
        }

        preferenceEditor.putLong(context.getString(R.string.lastplayed_id_key), controller.getPlaying().id);
        preferenceEditor.putString(context.getString(R.string.lastplayed_queue_key), currentQueueString);
        preferenceEditor.putInt(context.getString(R.string.lastplayed_pos_key), controller.getPositionMs());
        preferenceEditor.putBoolean(context.getString(R.string.lastplayed_shuffle_key), controller.isShuffleEnabled());
        preferenceEditor.putInt(context.getString(R.string.lastplayed_repeat_key), repeatInt);
        preferenceEditor.apply();
    }

    public static void setSearchFolder(Context context, @Nullable String path)
    {
        context = context.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        path = path != null ? path : context.getString(R.string.preferences_search_everywhere);
        prefs.edit().putString(context.getString(R.string.preferences_search_folder_key), path).apply();
    }

    public static String getSearchFolder(Context context)
    {
        context = context.getApplicationContext();

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.preferences_search_folder_key), context.getString(R.string.preferences_search_everywhere));
    }

    public static void setupCompleted(Context context)
    {
        context = context.getApplicationContext();

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.preferences_setup_complete_key), true)
                .apply();
    }

    public static boolean wasSetupCompleted(Context context)
    {
        context = context.getApplicationContext();

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.preferences_setup_complete_key), false);
    }

    public static void setHeartOnSong(Context context, long trackId, boolean heart)
    {
        context = context.getApplicationContext();

        SharedPreferences heartPreferences = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_favourite), Context.MODE_PRIVATE);
        heartPreferences.edit().putBoolean("heart_"+trackId, heart).apply();
    }


    private static List<Long> parseExcludedPlaylistIds(Set<String> idsRaw)
    {
        List<Long> excludedPlaylistIds = new ArrayList<>();

        if(idsRaw.isEmpty()) return excludedPlaylistIds;

        for(String playlistIdString : idsRaw)
        {
            // pattern: id_$playlistId

            if(!playlistIdString.startsWith("id_")) continue;

            long playlistId = Long.parseLong(playlistIdString.substring(3));
            excludedPlaylistIds.add(playlistId);
        }

        return excludedPlaylistIds;
    }

    private static List<Long> parseSavedQueue(String savedQueueRaw)
    {
        List<Long> savedQueue = new ArrayList<>();

        if(TextUtils.isEmpty(savedQueueRaw)) return savedQueue;

        String[] savedQueueArray = savedQueueRaw.split(";");

        for(String songIdString : savedQueueArray)
        {
            try
            {
                long songId = Long.parseLong(songIdString);
                savedQueue.add(songId);
            }
            catch (NumberFormatException ignore) { }
        }

        return savedQueue;
    }
}
