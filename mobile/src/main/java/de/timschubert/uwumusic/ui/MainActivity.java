package de.timschubert.uwumusic.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import de.timschubert.uwumusic.shared.prefs.PermissionHelper;
import de.timschubert.uwumusic.R;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.fragment_preferences, false);

        if(!AuriPreferences.wasSetupCompleted(getApplicationContext()))
        {
            Intent setupIntent = new Intent(this, SetupActivity.class);
            finish();
            startActivity(setupIntent);
        }

        PermissionHelper.askForPermissions(this, this, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.toolbar_preferences)
        {
            startActivity(new Intent(this, PreferencesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
