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
import java.util.*;

public class Album extends HashMap<String, Object> {

    public final static String ID = "id";
    public final static String ZIP = "zip";
    public final static String NAME = "name";
    public final static String ARTIST_NAME = "artist_name";
    public final static String RELEASE_DATE = "releasedate";
    public final static String IMAGE = "image";
    public final static String[] FIELDS = {ARTIST_NAME, NAME, ZIP, ID, RELEASE_DATE};

    protected static Context staticContext;

	public static void setContext(Context ctx) {
		staticContext = ctx;
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
