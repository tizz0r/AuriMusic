package de.timschubert.uwumusic.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import de.timschubert.uwumusic.R;
import de.timschubert.uwumusic.StaticCursorTest;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;
import de.timschubert.uwumusic.ui.SetupActivity;
import de.timschubert.uwumusic.PathHelper;

public class SelectFolderFragment extends Fragment
{

    private static final int FOLDER_REQUEST_CODE = 0x13;

    private RadioButton searchEverywhereBox;

    private String searchPath = null;

    public static SelectFolderFragment newInstance() { return new SelectFolderFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_select_folder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        ClickListener clickListener = new ClickListener();
        CheckedListener checkedListener = new CheckedListener();

        Button nextButton = view.findViewById(R.id.fragment_select_folder_next);
        nextButton.setOnClickListener(clickListener);
        Button backButton = view.findViewById(R.id.fragment_select_folder_back);
        backButton.setOnClickListener(clickListener);

        searchEverywhereBox =
                view.findViewById(R.id.fragment_select_folder_search_everywhere);
        searchEverywhereBox.setOnCheckedChangeListener(checkedListener);
        RadioButton searchOnlyFolderBox =
                view.findViewById(R.id.fragment_select_folder_search_folder);
        searchOnlyFolderBox.setOnCheckedChangeListener(checkedListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if(requestCode != FOLDER_REQUEST_CODE) return;
        if(resultCode == Activity.RESULT_CANCELED)
        {
            fallBackToEverywhere();
        }
        else if(resultCode == Activity.RESULT_OK)
        {
            Uri uri = data != null ? data.getData() : null;
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri));

            String path;
            try
            {
                path = PathHelper.getPath(Objects.requireNonNull(getContext()), docUri);
            }
            catch (Exception e)
            {
                fallBackToEverywhere();
                Toast.makeText(getContext(),
                        R.string.error_message_wrong_path,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if(!StaticCursorTest.isMusicInDir(getContext(), path))
            {
                fallBackToEverywhere();
                Toast.makeText(getContext(),
                        getString(R.string.error_message_no_music_found) + path,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            searchPath = path;
        }
    }

    private void fallBackToEverywhere()
    {
        if(!searchEverywhereBox.isChecked()) searchEverywhereBox.setChecked(true);

        searchPath = null;
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
                case R.id.fragment_select_folder_next:

                    AuriPreferences.setSearchFolder(getContext(), searchPath);

                    rootActivity.advanceSetup(SetupActivity.FRAGMENT_SELECT_FOLDER_ID);
                    break;

                case R.id.fragment_select_folder_back:
                    rootActivity.onBackPressed();
                    break;
            }
        }
    }

    private class CheckedListener implements RadioButton.OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            switch (buttonView.getId())
            {
                case R.id.fragment_select_folder_search_everywhere:
                    if(!isChecked) return;
                    fallBackToEverywhere();
                    break;

                case R.id.fragment_select_folder_search_folder:

                    if(!isChecked) return;
                    Intent browseIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(browseIntent, FOLDER_REQUEST_CODE);
                    break;
            }
        }
    }
}
