package de.timschubert.uwumusic.shared.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import de.timschubert.uwumusic.shared.controller.MusicInstanceController;

public class BecomingNoisyReceiver extends BroadcastReceiver
{

    private Context context;
    private IntentFilter noisyIntentFilter;
    private boolean isRegistered;

    public BecomingNoisyReceiver(Context context)
    {
        this.context = context;
        noisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        isRegistered = false;
    }

    public void register()
    {
        if(isRegistered) return;
        context.registerReceiver(this, noisyIntentFilter);
        isRegistered = true;
    }

    public void unregister()
    {
        if(!isRegistered) return;
        context.unregisterReceiver(this);
        isRegistered = false;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
        {
            MusicInstanceController.getInstance().pause();
        }
    }
}
