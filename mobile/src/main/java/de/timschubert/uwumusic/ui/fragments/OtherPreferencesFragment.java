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

public class OtherPreferencesFragment extends Fragment
{

    public static OtherPreferencesFragment newInstance() { return new OtherPreferencesFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_other_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        ClickListener clickListener = new ClickListener();

        Button finishButton = view.findViewById(R.id.fragment_other_preferences_finish);
        finishButton.setOnClickListener(clickListener);
        Button backButton = view.findViewById(R.id.fragment_other_preferences_back);
        backButton.setOnClickListener(clickListener);
    }

    private class ClickListener implements Button.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            SetupActivity rootActivity = (SetupActivity) getActivity();
            if(rootActivity == null) return;

            switch(v.getId())
            {
                case R.id.fragment_other_preferences_finish:
                    rootActivity.advanceSetup(SetupActivity.FRAGMENT_OTHER_PREFERENCES_ID);
                    break;

                case R.id.fragment_other_preferences_back:
                    rootActivity.onBackPressed();
                    break;
            }
        }
    }
}
