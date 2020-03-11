package de.timschubert.uwumusic.shared.mediaitems;

import androidx.annotation.NonNull;

import java.util.List;

public interface Playable
{
    @NonNull List<PlayItem> playables();
}
