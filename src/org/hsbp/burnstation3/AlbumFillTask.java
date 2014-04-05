package org.hsbp.burnstation3;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class AlbumFillTask extends AsyncTask<Album.Order, Void, List<? extends Map<String, ?>>> {

	public final static String ALBUM_COVER_CACHE_DIR = "org.hsbp.burnstation3.album_cover.cache";
	public final static String ALBUM_COVER_FILE_SUFFIX = ".jpg";

	protected final ListView target;
	protected final Context ctx;
	protected final PlayerUI ui;

	public AlbumFillTask(ListView target, Context ctx, PlayerUI ui) {
		super();
		this.target = target;
		this.ctx = ctx;
		this.ui = ui;
	}

	@Override
	protected List<? extends Map<String, ?>> doInBackground(Album.Order... order) {
		List<Map<String, Object>> albums = new ArrayList<Map<String, Object>>();
		try {
			JSONArray api_result = API.getArray("albums", "&imagesize=75&order=" + order[0].getValue());
			File cacheDir = new File(ctx.getCacheDir(), ALBUM_COVER_CACHE_DIR);
			cacheDir.mkdirs();
			for (int i = 0; i < api_result.length(); i++) {
				try {
					Map<String, Object> album = new Album();
					JSONObject item = api_result.getJSONObject(i);
					for (String field : Album.FIELDS) {
						album.put(field, item.getString(field));
					}
					try {
						File cover = new File(cacheDir,
								(String)album.get(Album.ID) + ALBUM_COVER_FILE_SUFFIX);
						API.download(new URL(item.getString(Album.IMAGE)), cover, null);
						album.put(Album.IMAGE, cover.getAbsolutePath());
					} catch (IOException ioe) {
						album.put(Album.IMAGE, R.drawable.burnstation);
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
		final String[] map_from = {Album.NAME, Album.ARTIST_NAME, Album.IMAGE, Album.RELEASE_DATE};
		final int[] map_to = {R.id.album_name, R.id.album_artist,
			R.id.album_image, R.id.album_release_date};
		target.setAdapter(new SimpleAdapter(ctx, result,
					R.layout.albums_item, map_from, map_to));
		ui.hideIndeterminateProgressDialog();
	}
}
