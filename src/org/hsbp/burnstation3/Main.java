package org.hsbp.burnstation3;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.*;
import android.view.View;
import java.util.*;
import java.net.*;
import java.io.*;
import org.apache.commons.io.IOUtils;
import org.json.*;

public class Main extends Activity implements AdapterView.OnItemClickListener
{
    public final static String CLIENT_ID = "5559df65";
    public final static String ID = "id";
    public static final String UTF_8 = "UTF-8";
    protected Player player;
    protected ArrayAdapter<Track> playList;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView lv = (ListView)findViewById(R.id.playlist);
        playList = new ArrayAdapter<Track>(
                    this, android.R.layout.simple_list_item_1, new ArrayList<Track>());
        lv.setAdapter(playList);
        new AlbumListFillTask().execute();
        player = new Player();
    }

    private class AlbumListFillTask extends AsyncTask<Void, Void, List<? extends Map<String, ?>>> {
        public final static String NAME = "name";
        public final static String ARTIST_NAME = "artist_name";

        @Override
        protected List<? extends Map<String, ?>> doInBackground(Void... ignored) {
            List<Map<String, String>> albums = new ArrayList<Map<String, String>>();
            try {
                JSONArray api_result = getArrayFromApi("albums", "&order=popularity_week");
                for (int i = 0; i < api_result.length(); i++) {
                    try {
                        Map<String, String> album = new HashMap<String, String>();
                        JSONObject item = api_result.getJSONObject(i);
                        album.put(ARTIST_NAME, item.getString(ARTIST_NAME));
                        album.put(NAME, item.getString(NAME));
                        album.put(ID, item.getString(ID));
                        albums.add(album);
                    } catch (JSONException je) {
                        je.printStackTrace(); // TODO report API error
                    }
                }
            } catch (JSONException je) {
                je.printStackTrace(); // TODO report API error
            } catch (IOException ioe) {
                ioe.printStackTrace(); // TODO report API error
            }
            return albums;
        }

        @Override
        protected void onPostExecute(List<? extends Map<String, ?>> result) {
            ListView lv = (ListView)findViewById(R.id.albums);
            final String[] map_from = {NAME, ARTIST_NAME};
            final int[] map_to = {R.id.album_name, R.id.album_artist};
            lv.setAdapter(new SimpleAdapter(Main.this, result,
                        R.layout.albums_item, map_from, map_to));
            lv.setOnItemClickListener(Main.this);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getItemAtPosition(position);
        switch (parent.getId()) {
            case R.id.albums:
                new TrackListFillTask().execute(((Map<String, String>)item).get(ID));
                break;
            case R.id.tracks:
                Track track = (Track)item;
                track.prepare();
                playList.add(track);
                // TODO start playing it if nothing else is being played
                break;
        }
    }

    private class TrackListFillTask extends AsyncTask<String, Void, List<Track>> {

        @Override
        protected List<Track> doInBackground(String... album_id) {
            List<Track> tracks = new ArrayList<Track>();
            try {
                JSONArray api_result = getArrayFromApi("tracks", "&album_id=" + album_id[0]);
                for (int i = 0; i < api_result.length(); i++) {
                    try {
                        JSONObject item = api_result.getJSONObject(i);
                        tracks.add(Track.fromJSONObject(Main.this, item));
                    } catch (JSONException je) {
                        je.printStackTrace(); // TODO report API error
                    }
                }
            } catch (JSONException je) {
                je.printStackTrace(); // TODO report API error
            } catch (IOException ioe) {
                ioe.printStackTrace(); // TODO report API error
            }
            return tracks;
        }

        @Override
        protected void onPostExecute(List<Track> result) {
            ListView lv = (ListView)findViewById(R.id.tracks);
            lv.setAdapter(new ArrayAdapter(Main.this,
                        android.R.layout.simple_list_item_1, result));
            lv.setOnItemClickListener(Main.this);
        }
    }

    private static JSONArray getArrayFromApi(String resource, String parameters)
        throws IOException, JSONException {
        URL api = new URL("http://api.jamendo.com/v3.0/" + resource +
                ("/?client_id=" + CLIENT_ID + "&format=json") + parameters);
        HttpURLConnection urlConnection = (HttpURLConnection) api.openConnection();
        try {
            String response = IOUtils.toString(urlConnection.getInputStream(), UTF_8);
            JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
            return object.getJSONArray("results");
        } finally {
            urlConnection.disconnect();
        }
    }

    public void playClicked(View view) {
        if (playList.getCount() == 0) return;
        player.play(this, playList.getItem(0), false);
    }

    public void pauseClicked(View view) {
        player.pause();
    }
}
