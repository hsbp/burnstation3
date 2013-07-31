package org.hsbp.burnstation3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.*;
import android.view.View;
import android.view.Display;
import android.view.Window;
import java.util.*;
import java.net.*;
import java.io.*;
import org.apache.commons.io.IOUtils;
import org.json.*;

public class Main extends Activity implements AdapterView.OnItemClickListener,
       PlayerUI, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener
{
    public final static String CLIENT_ID = "5559df65";
    public final static String ID = "id";
    public final static String ZIP = "zip";
    public final static String TIME_FMT = "%d:%02d";
    public final static String NAME = "name";
    public final static String ARTIST_NAME = "artist_name";
    public final static String RELEASE_DATE = "releasedate";
    public final static String IMAGE = "image";
    public final static String[] ALBUM_FIELDS = {ARTIST_NAME, NAME, ZIP, ID, RELEASE_DATE};
    public final static String ALBUM_COVER_CACHE_DIR = "org.hsbp.burnstation3.album_cover.cache";
    public final static String ALBUM_COVER_FILE_SUFFIX = ".jpg";
    public static final String UTF_8 = "UTF-8";
    protected static Context staticContext;
    protected Player player;
    protected boolean seeker_update_enabled = true;
    protected String currentAlbumZip = null;
    protected ProgressDialog progDlg = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        staticContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        ListView lv = (ListView)findViewById(R.id.playlist);
        player = new Player(this);
        lv.setAdapter(player);
        lv.setOnItemClickListener(this);
        SeekBar sb = (SeekBar)findViewById(R.id.player_seek);
        sb.setOnSeekBarChangeListener(this);
        Spinner albumsOrder = (Spinner)findViewById(R.id.albums_order);
        ArrayAdapter<AlbumsOrder> albumsOrderAdapter = new ArrayAdapter<AlbumsOrder>(
                this, android.R.layout.simple_spinner_dropdown_item, AlbumsOrder.values());
        albumsOrder.setAdapter(albumsOrderAdapter);
        albumsOrder.setOnItemSelectedListener(this);
        albumsOrder.setSelection(0);
    }

    public void showIndeterminateProgressDialog(String msg) {
        hideIndeterminateProgressDialog();
        progDlg = new ProgressDialog(this);
        progDlg.setMessage(msg);
        progDlg.setIndeterminate(true);
        progDlg.show();
    }

    public void hideIndeterminateProgressDialog() {
        if (progDlg == null) return;
        progDlg.dismiss();
        progDlg = null;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        AlbumsOrder order = (AlbumsOrder)parent.getSelectedItem();
        showIndeterminateProgressDialog(getString(R.string.loading_param, order));
        new AlbumListFillTask().execute(order);
    }

    public void onNothingSelected(AdapterView<?> parent) {}

    protected enum AlbumsOrder {
        POPULARITY_WEEK(R.string.albums_order_popularity_week),
        POPULARITY_MONTH(R.string.albums_order_popularity_month),
        POPULARITY_TOTAL(R.string.albums_order_popularity_total),
        RELEASEDATE_DESC(R.string.albums_order_releasedate_desc);

        private int res;

        private AlbumsOrder(int res) {
            this.res = res;
        }

        public String getValue() {
            return super.toString().toLowerCase();
        }

        @Override
        public String toString() {
            return staticContext.getString(res);
        }
    }

    private class AlbumListFillTask extends AsyncTask<AlbumsOrder, Void, List<? extends Map<String, ?>>> {

        @Override
        protected List<? extends Map<String, ?>> doInBackground(AlbumsOrder... order) {
            List<Map<String, Object>> albums = new ArrayList<Map<String, Object>>();
            try {
                JSONArray api_result = getArrayFromApi("albums", "&order=" + order[0].getValue());
                File cacheDir = new File(getCacheDir(), ALBUM_COVER_CACHE_DIR);
                cacheDir.mkdirs();
                for (int i = 0; i < api_result.length(); i++) {
                    try {
                        Map<String, Object> album = new HashMap<String, Object>();
                        JSONObject item = api_result.getJSONObject(i);
                        for (String field : ALBUM_FIELDS) {
                            album.put(field, item.getString(field));
                        }
                        try {
                            File cover = new File(cacheDir,
                                    (String)album.get(ID) + ALBUM_COVER_FILE_SUFFIX);
                            Downloader.download(new URL(item.getString(IMAGE)), cover);
                            album.put(IMAGE, cover.getAbsolutePath());
                        } catch (IOException ioe) {
                            album.put(IMAGE, R.drawable.burnstation);
                        }
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
            final String[] map_from = {NAME, ARTIST_NAME, IMAGE, RELEASE_DATE};
            final int[] map_to = {R.id.album_name, R.id.album_artist,
                R.id.album_image, R.id.album_release_date};
            lv.setAdapter(new SimpleAdapter(Main.this, result,
                        R.layout.albums_item, map_from, map_to));
            lv.setOnItemClickListener(Main.this);
            hideIndeterminateProgressDialog();
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getItemAtPosition(position);
        switch (parent.getId()) {
            case R.id.albums:
                Map<String, String> album = (Map<String, String>)item;
                currentAlbumZip = album.get(ZIP);
                new TrackListFillTask().execute(album.get(ID));
                showIndeterminateProgressDialog(getString(
                            R.string.loading_param, album.get(NAME)));
                break;
            case R.id.tracks:
                Track track = (Track)item;
                track.prepare();
                player.add(track);
                playClicked(view);
                break;
            case R.id.playlist:
                player.play((Player.Item)item, true);
                break;
        }
    }

    public void enqueueAllTracks(View view) {
        AdapterView<?> av = (AdapterView<?>)findViewById(R.id.tracks);
        for (int i = 0; i < av.getCount(); i++) {
            onItemClick(av, null, i, 0);
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
            hideIndeterminateProgressDialog();
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
        if (player.getCount() == 0) return;
        player.play(player.getItem(0), false);
    }

    public void pauseClicked(View view) {
        player.pause();
    }

    public void previousClicked(View view) {
        player.playPreviousTrack();
    }

    public void nextClicked(View view) {
        player.playNextTrack();
    }

    public void updateElapsed(int time) {
        if (seeker_update_enabled) {
            SeekBar sb = (SeekBar)findViewById(R.id.player_seek);
            sb.setProgress(time);
        }
        updateTextViewTime(R.id.player_elapsed, time / 1000);
    }

    public void updateTotal(int time) {
        SeekBar sb = (SeekBar)findViewById(R.id.player_seek);
        sb.setMax(time * 1000);
        updateTextViewTime(R.id.player_total, time);
    }

    protected void updateTextViewTime(int res, int time) {
        TextView tv = (TextView)findViewById(res);
        tv.setText(String.format(TIME_FMT, time / 60, time % 60));
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) player.seek(progress);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        seeker_update_enabled = false;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        seeker_update_enabled = true;
    }

    public void showAlbumZipAccess(View view) {
        new AlbumZipAccessTask().execute();
    }

    private class AlbumZipAccessTask extends AsyncTask<Void, Void, Drawable> {
        @Override
        protected Drawable doInBackground(Void... ignored) {
            try {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int dimension = Math.min(size.x, size.y) / 4 * 3;
                return new BitmapDrawable(getResources(),
                        QREncoder.encodeAsBitmap(currentAlbumZip, dimension));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result == null) return;
            final ImageView img = new ImageView(Main.this);
            img.setImageDrawable(result);
            new AlertDialog.Builder(Main.this)
                .setTitle(currentAlbumZip)
                .setView(img)
                .setNeutralButton(android.R.string.ok, null)
                .show();
        }
    }
}
