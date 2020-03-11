package de.timschubert.uwumusic.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import de.timschubert.uwumusic.R;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;
import de.timschubert.uwumusic.ui.fragments.OtherPreferencesFragment;
import de.timschubert.uwumusic.ui.fragments.PermissionFragment;
import de.timschubert.uwumusic.ui.fragments.SelectFolderFragment;
import de.timschubert.uwumusic.ui.fragments.WelcomeFragment;

public class SetupActivity extends AppCompatActivity
{

    public static final String FRAGMENT_WELCOME_ID = "setup_welcome";
    public static final String FRAGMENT_PERMISSION_ID = "setup_permission";
    public static final String FRAGMENT_SELECT_FOLDER_ID = "setup_select_folder";
    public static final String FRAGMENT_OTHER_PREFERENCES_ID = "setup_other_preferences";

    private SetupState currentSetupState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        if(savedInstanceState != null) return;

        currentSetupState = SetupState.WELCOME;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.setup_container, WelcomeFragment.newInstance())
                .commitNow();
    }

    public void advanceSetup(String currentId)
    {
        currentSetupState = nextSetupState(currentId);

        Fragment nextFragment = WelcomeFragment.newInstance();
        String fragmentId = FRAGMENT_WELCOME_ID;

        switch (currentSetupState)
        {
            case PERMISSION:
                nextFragment = PermissionFragment.newInstance();
                fragmentId = FRAGMENT_PERMISSION_ID;
                break;

            case SELECT_FOLDER:
                nextFragment = SelectFolderFragment.newInstance();
                fragmentId = FRAGMENT_SELECT_FOLDER_ID;
                break;

            case OTHER_PREFERENCES:
                nextFragment = OtherPreferencesFragment.newInstance();
                fragmentId = FRAGMENT_OTHER_PREFERENCES_ID;
                break;

            case DONE:
                endSetup();
                return;
        }

        Log.e("Auri Music", "fragment opening: "+fragmentId);
        changeFragment(nextFragment, fragmentId);
    }

    private void changeFragment(Fragment fragment, String id)
    {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right)
                .replace(R.id.setup_container, fragment)
                .addToBackStack(id)
                .commit();
    }

    private void endSetup()
    {
        AuriPreferences.setupCompleted(getApplicationContext());

        Intent mainIntent = new Intent(this, MainActivity.class);
        finish();
        startActivity(mainIntent);
    }

    private SetupState nextSetupState(String currentId)
    {
        switch (currentId)
        {
            default:
            case FRAGMENT_WELCOME_ID:
                return SetupState.PERMISSION;
            case FRAGMENT_PERMISSION_ID:
                return SetupState.SELECT_FOLDER;
            case FRAGMENT_SELECT_FOLDER_ID:
                return SetupState.OTHER_PREFERENCES;
            case FRAGMENT_OTHER_PREFERENCES_ID:
                return SetupState.DONE;
        }
    }

    public enum SetupState { WELCOME, PERMISSION, SELECT_FOLDER, OTHER_PREFERENCES, DONE }
}
