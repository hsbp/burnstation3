package org.hsbp.burnstation3;

import android.app.Activity;
import android.widget.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import org.json.*;

public class AlbumFillTask extends ListFillTask<Album.Order, Map<String, ?>> {

	public final static String ALBUM_COVER_CACHE_DIR = "org.hsbp.burnstation3.album_cover.cache";
	public final static String ALBUM_COVER_FILE_SUFFIX = ".jpg";

	public AlbumFillTask(final Activity activity, final PlayerUI ui) {
		super(R.id.albums, activity, ui);
	}

	@Override
	protected List<Map<String, ?>> doInBackground(final Album.Order... order) {
		final File cacheDir = new File(ctx.getCacheDir(), ALBUM_COVER_CACHE_DIR);
		cacheDir.mkdirs();
		final JsonArrayProcessor<Map<String, ?>> jap = new JsonArrayProcessor<Map<String, ?>>(ui) {
			public Map<String, ?> mapItem(final JSONObject item) throws JSONException, IOException {
				final Map<String, Object> album = new Album();
				for (String field : Album.FIELDS) {
					album.put(field, item.getString(field));
				}
				try {
					final File cover = new File(cacheDir,
							(String)album.get(Album.ID) + ALBUM_COVER_FILE_SUFFIX);
					API.download(new URL(item.getString(Album.IMAGE)), cover, null);
					album.put(Album.IMAGE, cover.getAbsolutePath());
				} catch (IOException ioe) {
					album.put(Album.IMAGE, R.drawable.burnstation);
				}
				return album;
			}
		};
		return jap
			.setMessage(JsonArrayProcessor.State.CONSTUCTION, R.string.api_album_construction_error)
			.setMessage(JsonArrayProcessor.State.EXTRACTION, R.string.api_album_extraction_error)
			.setMessage(JsonArrayProcessor.State.IO, R.string.api_album_io_error)
			.process("albums", "&imagesize=75&order=" + order[0].getValue());
	}

	@Override
	protected void onPostExecute(List<Map<String, ?>> result) {
		if (!result.isEmpty()) {
			final String[] mapFrom = {Album.NAME, Album.ARTIST_NAME, Album.IMAGE, Album.RELEASE_DATE};
			final int[] mapTo = {R.id.album_name, R.id.album_artist,
				R.id.album_image, R.id.album_release_date};
			view.setAdapter(new SimpleAdapter(ctx, result,
						R.layout.albums_item, mapFrom, mapTo));
		}
		ui.hideIndeterminateProgressDialog();
	}
}
