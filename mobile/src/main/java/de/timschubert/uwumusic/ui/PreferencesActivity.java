package de.timschubert.uwumusic.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.List;
import java.util.Objects;

import de.timschubert.uwumusic.R;
import de.timschubert.uwumusic.StaticCursorTest;
import de.timschubert.uwumusic.shared.mediaitems.Playlist;

public class PreferencesActivity extends AppCompatActivity
{

    private static List<Playlist> allPlaylists;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.preferences_container, new PreferencesFragment())
                .commit();

        allPlaylists = StaticCursorTest.loadPlaylists(this);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null) return;

        actionBar.setTitle(getString(R.string.preferences_title));
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == android.R.id.home) super.onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public static class PreferencesFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            setPreferencesFromResource(R.xml.fragment_preferences, rootKey);

            String[] entries = {getString(R.string.error_message_no_playlists_found)};
            String[] entryValues = {"error"};

            if(allPlaylists != null && allPlaylists.size() > 0)
            {
                entries = new String[allPlaylists.size()];
                entryValues = new String[allPlaylists.size()];

                for(int i = 0; i < allPlaylists.size(); i++)
                {
                    entries[i] = allPlaylists.get(i).getName();
                    entryValues[i] = "id_"+allPlaylists.get(i).getId();
                }
            }

            MultiSelectListPreference excludePlaylistsPreference = findPreference(getString(R.string.preferences_excludedplaylists_key));
            Objects.requireNonNull(excludePlaylistsPreference);
            excludePlaylistsPreference.setEntries(entries);
            excludePlaylistsPreference.setDefaultValue("error");
            excludePlaylistsPreference.setEntryValues(entryValues);
        }
    }
}
