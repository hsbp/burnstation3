package org.hsbp.burnstation3;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.*;
import java.util.*;

public class Main extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String[] test = {"foo", "bar", "baz"};
        int[] widgets = {R.id.tracks, R.id.playlist};
        for (int widget : widgets) {
            ListView lv = (ListView)findViewById(widget);
            lv.setAdapter(new ArrayAdapter<String>(
                        this, android.R.layout.simple_list_item_1, test));
        }
        new AlbumListFillTask().execute();
    }

    private class AlbumListFillTask extends AsyncTask<Void, Void, List<? extends Map<String, ?>>> {
        @Override
        protected List<? extends Map<String, ?>> doInBackground(Void... ignored) {
            List<Map<String, String>> albums = new ArrayList<Map<String, String>>();
            // TODO download into albums
            Map<String, String> album = new HashMap<String, String>();
            album.put("name", "Test album title");
            album.put("artist", "Test album artist");
            albums.add(album);
            return albums;
        }

        @Override
        protected void onPostExecute(List<? extends Map<String, ?>> result) {
            ListView lv = (ListView)findViewById(R.id.albums);
            final String[] map_from = {"name", "artist"};
            final int[] map_to = {R.id.album_name, R.id.album_artist};
            lv.setAdapter(new SimpleAdapter(Main.this, result,
                        R.layout.albums_item, map_from, map_to));
        }
    }
}
