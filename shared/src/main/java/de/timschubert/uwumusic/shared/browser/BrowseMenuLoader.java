package de.timschubert.uwumusic.shared.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.appcompat.view.menu.MenuBuilder;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.uwumusic.shared.R;
import de.timschubert.uwumusic.shared.prefs.AuriPreferences;
import de.timschubert.uwumusic.shared.service.MusicService;

public class BrowseMenuLoader
{

    private Menu rootMenu;

    @SuppressLint("RestrictedApi")
    public BrowseMenuLoader(Context context)
    {
        rootMenu = new MenuBuilder(context);
        MenuInflater inflater = new MenuInflater(context);
        inflater.inflate(R.menu.music_browse_tree, rootMenu);
    }

    public boolean isTreeItem(String parentId)
    {
        if(MusicService.rootId.equals(parentId)) return true;

        int id;

        try
        {
            id = Integer.parseInt(parentId);
        }
        catch (NumberFormatException e){ return false; }

        MenuItem menuItem = rootMenu.findItem(id);
        if(menuItem != null)
        {
            return menuItem.hasSubMenu() && menuItem.getSubMenu().size() > 0;
        }

        return false;
    }

    public List<MediaBrowserCompat.MediaItem> loadTree(String parentId)
    {
        ArrayList<MediaBrowserCompat.MediaItem> list = new ArrayList<>();

        if(MusicService.rootId.equals(parentId))
        {
            list = createItemsForMenu(rootMenu);
        }
        else
        {
            SubMenu subMenu = rootMenu.findItem(Integer.parseInt(parentId)).getSubMenu();

            if(subMenu != null) list = createItemsForMenu(subMenu);
        }

        return list;
    }

    private MediaBrowserCompat.MediaItem createItemForMenuItem(MenuItem menuItem)
    {
        Size size = new Size(128, 128);

        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        builder.setTitle(menuItem.getTitle());
        builder.setIconBitmap(getBitmapFromDrawable(menuItem.getIcon(), size));
        builder.setMediaId(String.valueOf(menuItem.getItemId()));

        boolean isMenu = menuItem.hasSubMenu();
        int flag = isMenu ? MediaBrowserCompat.MediaItem.FLAG_BROWSABLE : MediaBrowserCompat.MediaItem.FLAG_PLAYABLE;

        return new MediaBrowserCompat.MediaItem(builder.build(), flag);
    }

    private ArrayList<MediaBrowserCompat.MediaItem> createItemsForMenu(Menu menu)
    {
        ArrayList<MediaBrowserCompat.MediaItem> list = new ArrayList<>();

        for(int i = 0; i < menu.size(); i++)
        {
            MenuItem menuItem = menu.getItem(i);

            if(menuItem.getItemId() == R.id.browse_search && !AuriPreferences.searchEnabled) continue;

            MediaBrowserCompat.MediaItem mediaItem = createItemForMenuItem(menuItem);
            list.add(mediaItem);
        }

        return list;
    }

    private Bitmap getBitmapFromDrawable(Drawable d, Size s)
    {
        Bitmap b = Bitmap.createBitmap(s.getWidth(), s.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, c.getWidth(), c.getHeight());
        d.draw(c);

        return b;
    }
}
