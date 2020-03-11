package de.timschubert.uwumusic.shared.controller;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

class FocusManager
{
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private AudioManager.OnAudioFocusChangeListener focusChangeListener;

    FocusManager(Context context, AudioManager.OnAudioFocusChangeListener listener)
    {
        focusChangeListener = listener;

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();

            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setWillPauseWhenDucked(false)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .build();
        }
    }

    boolean requestFocus()
    {
        int focusRequest;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            focusRequest = audioManager.requestAudioFocus(audioFocusRequest);
        }
        else
        {
            focusRequest = audioManager.requestAudioFocus(focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }

        return focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    void abandonFocus()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        }
        else
        {
            audioManager.abandonAudioFocus(focusChangeListener);
        }
    }
}
