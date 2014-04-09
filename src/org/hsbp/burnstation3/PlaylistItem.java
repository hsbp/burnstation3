package org.hsbp.burnstation3;

import android.os.Handler;
import android.widget.ArrayAdapter;
import java.util.*;

public class PlaylistItem implements Observer, Runnable {
    protected final Track track;
    protected boolean playing;
    protected final Handler handler = new Handler();
    protected final ArrayAdapter<?> playlist;

    public PlaylistItem(final Track track, final ArrayAdapter<?> playlist) {
        this.track = track;
        this.playlist = playlist;
        track.addObserver(this);
    }

    public void update(final Observable observable, final Object data) {
        handler.post(this);
    }

    public void run() {
        playlist.notifyDataSetChanged();
    }

    public Track getTrack() {
        return track;
    }

    public void setPlaying(final boolean value) {
        if (value != playing) {
            playing = value;
            handler.post(this);
        }
    }

    @Override
    public String toString() {
        final int db = track.getDownloadedBytes();
        final StringBuilder sb = new StringBuilder();
        if (playing) sb.append(track.isReadyToPlay() ? "\u25B6 " : "\u231B ");
        sb.append(track.artistName).append(": ").append(track.name);
        if (db != Track.FULLY_DOWNLOADED) sb.append(' ').append(
                playlist.getContext().getString(R.string.downloaded, db / 1024));
        return sb.toString();
    }
}
