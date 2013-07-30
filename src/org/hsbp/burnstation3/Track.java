package org.hsbp.burnstation3;

import android.content.Context;
import android.os.AsyncTask;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.util.Set;
import java.util.HashSet;
import org.json.JSONObject;
import org.json.JSONException;

public class Track implements Runnable {
    protected String artist_name, name, id;
    protected File localFile;
    protected URL audio;
    protected int duration;
    protected final static Set<String> beingCached = new HashSet<String>();
    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String ARTIST_NAME = "artist_name";
    public final static String AUDIO = "audio";
    public final static String DURATION = "duration";
    public final static String STR_FMT = "%s (%d:%02d)";
    public final static String CACHE_DIR = "org.hsbp.burnstation3.track.cache";
    public final static String FILE_SUFFIX = ".mp3";

    protected Track() {}

    public static Track fromJSONObject(Context ctx, JSONObject obj) throws JSONException,
           MalformedURLException {
        final Track track = new Track();
        track.id = obj.getString(ID);
        File cacheDir = new File(ctx.getCacheDir(), CACHE_DIR);
        cacheDir.mkdirs();
        track.localFile = new File(cacheDir, track.id + FILE_SUFFIX);
        track.name = obj.getString(NAME);
        track.artist_name = obj.getString(ARTIST_NAME);
        track.duration = obj.getInt(DURATION);
        track.audio = new URL(obj.getString(AUDIO));
        return track;
    }

    public String toString() {
        return String.format(STR_FMT, name, duration / 60, duration % 60);
    }

    public void prepare() {
        new Thread(this).start();
    }

    public void run() {
        synchronized (beingCached) {
            if (beingCached.contains(id)) return;
            beingCached.add(id);
        }
        try {
            download();
        } catch (IOException ioe) {
            ioe.printStackTrace(); // TODO notify user
        } finally {
            synchronized (beingCached) {
                beingCached.remove(id);
            }
        }
    }

    protected void download() throws IOException {
        if (localFile.exists() && localFile.length() != 0) return;
        HttpURLConnection urlConnection = (HttpURLConnection) audio.openConnection();
        try {
            InputStream input = new BufferedInputStream(urlConnection.getInputStream());
            try {
                OutputStream output = new FileOutputStream(localFile);
                try {
                    byte data[] = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                } finally {
                    output.close();
                }
            } finally {
                input.close();
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
