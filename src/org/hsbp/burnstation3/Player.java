package org.hsbp.burnstation3;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.os.PowerManager;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

public class Player extends ArrayAdapter<Track> {

    protected MediaPlayer mp = null;
    protected Track currentTrack = null;

    public Player(Context ctx) {
        super(ctx, android.R.layout.simple_list_item_1, new ArrayList<Track>());
    }

    public synchronized void play(final Track track, boolean forceReplace) {
        if (mp != null && track != currentTrack && forceReplace) {
            mp.release();
            mp = null;
        }
        if (mp == null) {
            currentTrack = track;
            new Thread(new Runnable() {
                public void run() {
                    synchronized (Player.this) {
                        final Context ctx = getContext();
                        mp = MediaPlayer.create(ctx, track.getUri());
                        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mp.setWakeMode(ctx.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                        mp.start();
                    }
                }
            }).start();
        } else {
            mp.start();
        }
    }

    public synchronized void pause() {
        try {
            if (mp != null) mp.pause();
        } catch (IllegalStateException ise) {}
    }

    public synchronized Track getCurrentTrack() {
        return currentTrack;
    }
}
