package org.hsbp.burnstation3;

import android.content.Context;
import java.net.URL;
import java.net.MalformedURLException;
import org.json.JSONObject;
import org.json.JSONException;

public class Track {
    protected String artist_name, name, id;
    protected URL audio;
    protected int duration;
    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String ARTIST_NAME = "artist_name";
    public final static String AUDIO = "audio";
    public final static String DURATION = "duration";
    public final static String STR_FMT = "%s (%d:%02d)";

    protected Track() {}

    public static Track fromJSONObject(Context ctx, JSONObject obj) throws JSONException,
           MalformedURLException {
        final Track track = new Track();
        track.id = obj.getString(ID);
        track.name = obj.getString(NAME);
        track.artist_name = obj.getString(ARTIST_NAME);
        track.duration = obj.getInt(DURATION);
        track.audio = new URL(obj.getString(AUDIO));
        return track;
    }

    public String toString() {
        return String.format(STR_FMT, name, duration / 60, duration % 60);
    }
}
