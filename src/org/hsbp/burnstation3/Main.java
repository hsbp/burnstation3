package org.hsbp.burnstation3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.*;
import android.view.View;
import android.view.Window;
import java.util.Map;

public class Main extends Activity implements AdapterView.OnItemClickListener,
	   AdapterView.OnItemSelectedListener {
	protected Player player;
	protected String currentAlbumZip;
	protected TrackListFillTask trackListFiller;
	protected AlbumFillTask albumListFiller;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		initPlayer();
		initAlbumsOrder();
		initListViewClickListeners();
	}

	public void initPlayer() {
		final ListView lv = (ListView)findViewById(R.id.playlist);
		final PlayerUI playerUi = new PlayerUiImpl(this);
		player = new Player(this, playerUi);
		lv.setAdapter(player);
        trackListFiller = new TrackListFillTask(this, playerUi);
		albumListFiller = new AlbumFillTask(this, playerUi);
	}

	public void initAlbumsOrder() {
		final Spinner albumsOrder = (Spinner)findViewById(R.id.albums_order);
		final ArrayAdapter<Album.Order> albumsOrderAdapter = new ArrayAdapter<Album.Order>(
				this, android.R.layout.simple_spinner_dropdown_item, Album.Order.values());
		albumsOrder.setAdapter(albumsOrderAdapter);
		albumsOrder.setOnItemSelectedListener(this);
		albumsOrder.setSelection(0);
	}

	public void initListViewClickListeners() {
		final int[] listViewIds = {R.id.albums, R.id.tracks, R.id.playlist};
		for (final int id : listViewIds) {
			final ListView view = (ListView)findViewById(id);
			view.setOnItemClickListener(this);
		}
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Album.Order order = (Album.Order)parent.getSelectedItem();
		albumListFiller.executeWithMessage(order, R.string.loading_param, order.toString());
    }

    public void onNothingSelected(AdapterView<?> parent) {}

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getItemAtPosition(position);
        switch (parent.getId()) {
            case R.id.albums:
                loadAlbumTracks((Map<String, String>)item);
                break;
            case R.id.tracks:
                enqueueTrack((Track)item);
                break;
            case R.id.playlist:
                player.play((Player.Item)item, true);
                break;
        }
    }

    protected void loadAlbumTracks(Map<String, String> album) {
        currentAlbumZip = album.get(Album.ZIP);
        trackListFiller.executeWithMessage(album.get(Album.ID),
				R.string.loading_param, album.get(Album.NAME));
    }

    protected void enqueueTrack(Track track) {
        player.add(track);
        playClicked(null);
    }

    public void enqueueAllTracks(View view) {
        AdapterView<?> av = (AdapterView<?>)findViewById(R.id.tracks);
        for (int i = 0; i < av.getCount(); i++) {
            onItemClick(av, null, i, 0);
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

    public void showAlbumZipAccess(View view) {
        new Album.ZipAccessTask(this, currentAlbumZip).execute();
    }
}
