package org.hsbp.burnstation3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.*;
import android.view.View;
import android.view.Window;
import java.util.*;
import java.net.*;
import java.io.*;
import org.json.*;

public class Main extends Activity implements AdapterView.OnItemClickListener,
	   PlayerUI, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener
{
	public final static String TIME_FMT = "%d:%02d";
	protected Player player;
	protected boolean seeker_update_enabled = true;
	protected String currentAlbumZip = null;
	protected ProgressDialog progDlg = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		ListView lv = (ListView)findViewById(R.id.playlist);
		player = new Player(this);
		lv.setAdapter(player);
		lv.setOnItemClickListener(this);
		SeekBar sb = (SeekBar)findViewById(R.id.player_seek);
		sb.setOnSeekBarChangeListener(this);
		Spinner albumsOrder = (Spinner)findViewById(R.id.albums_order);
		ArrayAdapter<Album.Order> albumsOrderAdapter = new ArrayAdapter<Album.Order>(
				this, android.R.layout.simple_spinner_dropdown_item, Album.Order.values());
		albumsOrder.setAdapter(albumsOrderAdapter);
		albumsOrder.setOnItemSelectedListener(this);
		albumsOrder.setSelection(0);
		lv = (ListView)findViewById(R.id.albums);
		lv.setOnItemClickListener(this);
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
		Album.Order order = (Album.Order)parent.getSelectedItem();
		showIndeterminateProgressDialog(getString(R.string.loading_param, order));
		new AlbumFillTask((ListView)findViewById(R.id.albums), this, this).execute(order);
    }

    public void onNothingSelected(AdapterView<?> parent) {}

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getItemAtPosition(position);
        switch (parent.getId()) {
            case R.id.albums:
                Map<String, String> album = (Map<String, String>)item;
                currentAlbumZip = album.get(Album.ZIP);
                new TrackListFillTask().execute(album.get(Album.ID));
                showIndeterminateProgressDialog(getString(
                            R.string.loading_param, album.get(Album.NAME)));
                break;
            case R.id.tracks:
                Track track = (Track)item;
                track.prepare(this);
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
                JSONArray api_result = API.getArray("tracks", "&album_id=" + album_id[0]);
                for (int i = 0; i < api_result.length(); i++) {
                    try {
                        JSONObject item = api_result.getJSONObject(i);
                        tracks.add(Track.fromJSONObject(Main.this, item));
                    } catch (JSONException je) {
                        handleException(R.string.api_track_construction_error, je);
                    }
                }
            } catch (JSONException je) {
                handleException(R.string.api_track_extraction_error, je);
            } catch (IOException ioe) {
                handleException(R.string.api_track_io_error, ioe);
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

    public void handleException(final int message, final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Main.this, message, Toast.LENGTH_LONG).show();
            }
        });
        e.printStackTrace();
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
        new Album.ZipAccessTask(this, currentAlbumZip).execute();
    }
}
