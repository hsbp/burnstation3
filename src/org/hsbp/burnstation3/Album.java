package org.hsbp.burnstation3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.Display;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class Album extends HashMap<String, Object> {

    public final static String ID = "id";
    public final static String ZIP = "zip";
    public final static String NAME = "name";
    public final static String ARTIST_NAME = "artist_name";
    public final static String RELEASE_DATE = "releasedate";
    public final static String IMAGE = "image";
    public final static String[] ALBUM_FIELDS = {ARTIST_NAME, NAME, ZIP, ID, RELEASE_DATE};
    public final static String ALBUM_COVER_CACHE_DIR = "org.hsbp.burnstation3.album_cover.cache";
    public final static String ALBUM_COVER_FILE_SUFFIX = ".jpg";

    protected static Context staticContext;

	public static void setContext(Context ctx) {
		staticContext = ctx;
	}

    public static class FillTask extends AsyncTask<Order, Void, List<? extends Map<String, ?>>> {

        protected final ListView target;
		protected final Context ctx;
		protected final PlayerUI ui;

        public FillTask(ListView target, Context ctx, PlayerUI ui) {
            super();
            this.target = target;
			this.ctx = ctx;
			this.ui = ui;
        }

        @Override
        protected List<? extends Map<String, ?>> doInBackground(Order... order) {
            List<Map<String, Object>> albums = new ArrayList<Map<String, Object>>();
            try {
                JSONArray api_result = API.getArray("albums", "&imagesize=75&order=" + order[0].getValue());
                File cacheDir = new File(ctx.getCacheDir(), ALBUM_COVER_CACHE_DIR);
                cacheDir.mkdirs();
                for (int i = 0; i < api_result.length(); i++) {
                    try {
                        Map<String, Object> album = new Album();
                        JSONObject item = api_result.getJSONObject(i);
                        for (String field : ALBUM_FIELDS) {
                            album.put(field, item.getString(field));
                        }
                        try {
                            File cover = new File(cacheDir,
                                    (String)album.get(ID) + ALBUM_COVER_FILE_SUFFIX);
                            API.download(new URL(item.getString(IMAGE)), cover, null);
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
            final String[] map_from = {NAME, ARTIST_NAME, IMAGE, RELEASE_DATE};
            final int[] map_to = {R.id.album_name, R.id.album_artist,
                R.id.album_image, R.id.album_release_date};
            target.setAdapter(new SimpleAdapter(ctx, result,
                        R.layout.albums_item, map_from, map_to));
            ui.hideIndeterminateProgressDialog();
        }
    }

    public enum Order {
        POPULARITY_WEEK(R.string.albums_order_popularity_week),
        POPULARITY_MONTH(R.string.albums_order_popularity_month),
        POPULARITY_TOTAL(R.string.albums_order_popularity_total),
        RELEASEDATE_DESC(R.string.albums_order_releasedate_desc);

        private int res;

        private Order(int res) {
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

    public static class ZipAccessTask extends AsyncTask<Void, Void, Drawable> {

		protected final Activity parent;
		protected final String zipUrl;

		public ZipAccessTask(Activity parent, String zipUrl) {
			super();
			this.parent = parent;
			this.zipUrl = zipUrl;
		}

        @Override
        protected Drawable doInBackground(Void... ignored) {
            try {
                Display display = parent.getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int dimension = Math.min(size.x, size.y) / 4 * 3;
                return new BitmapDrawable(parent.getResources(),
                        QREncoder.encodeAsBitmap(zipUrl, dimension));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result == null) return;
            final ImageView img = new ImageView(parent);
            img.setImageDrawable(result);
            new AlertDialog.Builder(parent)
                .setTitle(zipUrl)
                .setView(img)
                .setNeutralButton(android.R.string.ok, null)
                .show();
        }
    }
}
