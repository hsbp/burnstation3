package org.hsbp.burnstation3;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import android.view.Window;
import java.util.*;

public class Main extends Activity implements AdapterView.OnItemClickListener {

	protected Player player;
	protected String currentAlbumZip;
	protected TrackListFillTask trackListFiller;
	protected PlayerUI playerUi;
	protected boolean wasPlaying;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		playerUi = new PlayerUiImpl(this);
		initPlayer();
		initAlbumsOrder();
		initListViewClickListeners();
	}

    @Override
    protected void onPause() {
        wasPlaying = player.isPlaying();
        if (wasPlaying) playPauseClicked(null);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasPlaying) playPauseClicked(null);
    }

	public void initPlayer() {
		final ListView lv = (ListView)findViewById(R.id.playlist);
		player = new Player(this, playerUi);
		lv.setAdapter(player);
        trackListFiller = new TrackListFillTask(this, playerUi);
	}

	public void initAlbumsOrder() {
		final Spinner albumsOrder = (Spinner)findViewById(R.id.albums_order);
		final ArrayAdapter<Album.Order> albumsOrderAdapter = new ArrayAdapter<Album.Order>(
				this, android.R.layout.simple_spinner_dropdown_item, Album.Order.values());
		albumsOrder.setAdapter(albumsOrderAdapter);
		albumsOrder.setOnItemSelectedListener(new AlbumFillTask(this, playerUi));
		albumsOrder.setSelection(0);
	}

	public void initListViewClickListeners() {
		final int[] listViewIds = {R.id.albums, R.id.tracks, R.id.playlist};
		for (final int id : listViewIds) {
			final ListView view = (ListView)findViewById(id);
			view.setOnItemClickListener(this);
		}
	}

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getItemAtPosition(position);
        switch (parent.getId()) {
            case R.id.albums:
                loadAlbumTracks((Map<String, String>)item);
                break;
            case R.id.tracks:
                player.add(Collections.singletonList((Track)item));
                break;
            case R.id.playlist:
                player.play((PlaylistItem)item, true);
                break;
        }
    }

    protected void loadAlbumTracks(Map<String, String> album) {
        currentAlbumZip = album.get(Album.ZIP);
        new TrackListFillTask(this, playerUi).executeWithMessage(
                R.string.loading_param, album.get(Album.NAME), album.get(Album.ID));
    }

    public void enqueueAllTracks(View view) {
        final AdapterView<?> av = (AdapterView<?>)findViewById(R.id.tracks);
        final int count = av.getCount();
        final List<Track> tracks = new ArrayList(count);
        for (int i = 0; i < count; i++) tracks.add((Track)av.getItemAtPosition(i));
        player.add(tracks);
    }

    public void playPauseClicked(View view) {
        if (player.getCount() == 0) return;
        switch (playerUi.getState()) {
            case PLAYING:
                player.pause();
                break;
            case PAUSED:
                player.play(player.getItem(0), false);
                break;
        }
    }

    public void previousClicked(View view) {
        player.playPreviousTrack();
    }

    public void nextClicked(View view) {
        player.playNextTrack();
    }

    public void emptyClicked(View view) {
        player.clear();
    }

    public void showAlbumZipAccess(View view) {
        new Album.ZipAccessTask(this, currentAlbumZip).execute();
    }
}
