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
    public final static String ID = "id";
    public static final String UTF_8 = "UTF-8";

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
        Map<String, String> item = (Map<String, String>)parent.getItemAtPosition(position);
        System.err.println("ID: " + item.get(ID)); // TODO load tracks
    }

    private static JSONArray getArrayFromApi(String resource, String parameters)
        throws IOException, JSONException {
        URL api = new URL("http://10.0.2.2:5000/albums.json"); // TODO use real API
        HttpURLConnection urlConnection = (HttpURLConnection) api.openConnection();
        try {
            String response = IOUtils.toString(urlConnection.getInputStream(), UTF_8);
            JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
            return object.getJSONArray("results");
        } finally {
            urlConnection.disconnect();
        }
    }
}
