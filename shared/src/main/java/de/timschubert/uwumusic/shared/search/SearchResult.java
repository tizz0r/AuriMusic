package de.timschubert.uwumusic.shared.search;

import java.util.List;

import de.timschubert.uwumusic.shared.mediaitems.PlayItem;

public class SearchResult
{
    private List<PlayItem> results;
    private int position;

    SearchResult(List<PlayItem> results, int position)
    {
        this.results = results;
        this.position = position;
    }

    public List<PlayItem> getResults() { return results; }
    public int getPosition() { return position; }
}
