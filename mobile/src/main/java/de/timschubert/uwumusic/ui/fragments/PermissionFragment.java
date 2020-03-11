package de.timschubert.uwumusic.ui.fragments;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.timschubert.uwumusic.shared.prefs.PermissionHelper;
import de.timschubert.uwumusic.R;
import de.timschubert.uwumusic.ui.SetupActivity;

public class PermissionFragment extends Fragment
{

    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 0x23;

    private Button nextButton;
    private Button grantPermissionButton;

    public static PermissionFragment newInstance() { return new PermissionFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_permission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        // if(savedInstanceState != null) return;

        ClickListener clickListener = new ClickListener();

        nextButton = view.findViewById(R.id.fragment_permission_next);
        nextButton.setOnClickListener(clickListener);
        Button backButton = view.findViewById(R.id.fragment_permission_back);
        backButton.setOnClickListener(clickListener);
        grantPermissionButton = view.findViewById(R.id.fragment_permission_grant_permission);
        grantPermissionButton.setOnClickListener(clickListener);

        if(PermissionHelper.arePermissionsGranted(getContext())) onPermissionsGranted();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if(requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE)
        {
            if(grantResults.length == 0) return;

            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                onPermissionsGranted();
            }
        }
    }

    private void askForPermissions()
    {
        PermissionHelper.askForPermissions(this,
                getContext(),
                READ_EXTERNAL_STORAGE_REQUEST_CODE);
    }

    private void onPermissionsGranted()
    {
        grantPermissionButton.setEnabled(false);
        grantPermissionButton.setText(R.string.setup_permission_grant_permission_disabled);
        nextButton.setEnabled(true);
    }


    private class ClickListener implements Button.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            SetupActivity rootActivity = (SetupActivity) getActivity();
            if(rootActivity == null) return;

            switch (v.getId())
            {
                case R.id.fragment_permission_next:
                    rootActivity.advanceSetup(SetupActivity.FRAGMENT_PERMISSION_ID);
                    break;

                case R.id.fragment_permission_back:
                    rootActivity.onBackPressed();
                    break;

                case R.id.fragment_permission_grant_permission:
                    askForPermissions();
                    break;
            }
        }
    }
}
