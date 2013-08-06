package org.hsbp.burnstation3;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.HashSet;
import org.json.JSONObject;
import org.json.JSONException;

public class Track implements Runnable, API.Notifiable {
    protected String artistName, name, id;
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
    protected int downloadedBytes = 0;
    public final static int FULLY_DOWNLOADED = -1;
    protected final static int PLAY_TRESHOLD_BYTES = 300000;
    protected final Set<Notifiable> subscribers = new HashSet<Notifiable>();

    protected Track() {}

    public static Track fromJSONObject(Context ctx, JSONObject obj) throws JSONException,
           MalformedURLException {
        final Track track = new Track();
        track.id = obj.getString(ID);
        File cacheDir = new File(ctx.getCacheDir(), CACHE_DIR);
        cacheDir.mkdirs();
        track.localFile = new File(cacheDir, track.id + FILE_SUFFIX);
        track.name = obj.getString(NAME);
        track.artistName = obj.getString(ARTIST_NAME);
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
            API.download(audio, localFile, this);
        } catch (IOException ioe) {
            ioe.printStackTrace(); // TODO notify user
        } finally {
            synchronized (beingCached) {
                beingCached.remove(id);
            }
        }
    }

    public synchronized void downloaded(int bytes) {
        downloadedBytes = bytes;
        for (Notifiable subscriber : subscribers) {
            subscriber.trackInfoChanged();
        }
    }

    public synchronized void completed(File target) {
        downloaded(FULLY_DOWNLOADED);
    }

    public synchronized void subscribe(Notifiable subscriber) {
        subscribers.add(subscriber);
    }

    public interface Notifiable {
        public void trackInfoChanged();
    }

    public int getDownloadedBytes() {
        return downloadedBytes;
    }

    public synchronized boolean isReadyToPlay() {
        return downloadedBytes == FULLY_DOWNLOADED || downloadedBytes > PLAY_TRESHOLD_BYTES;
    }

    public Uri getUri() {
        run();
        return Uri.fromFile(localFile);
    }

    public int getDuration() {
        return duration;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getName() {
        return name;
    }
}
