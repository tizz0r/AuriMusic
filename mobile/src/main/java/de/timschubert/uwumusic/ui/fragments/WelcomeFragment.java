package de.timschubert.uwumusic.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.timschubert.uwumusic.R;
import de.timschubert.uwumusic.ui.SetupActivity;

public class WelcomeFragment extends Fragment
{

    public static WelcomeFragment newInstance() { return new WelcomeFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        Button beginButton = view.findViewById(R.id.fragment_welcome_begin);
        beginButton.setOnClickListener(new ClickListener());
    }

    private class ClickListener implements Button.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            SetupActivity rootActivity = (SetupActivity) getActivity();
            if(rootActivity == null) return;

            if(v.getId() == R.id.fragment_welcome_begin)
            {
                rootActivity.advanceSetup(SetupActivity.FRAGMENT_WELCOME_ID);
            }
        }
    }
}
