package org.hsbp.burnstation3;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

public class Player extends ArrayAdapter<Track> implements Runnable {

    protected MediaPlayer mp = null;
    protected Track currentTrack = null;
    protected final PlayerUI ui;
    protected final Handler handler = new Handler();

    public Player(Context ctx) {
        super(ctx, android.R.layout.simple_list_item_1, new ArrayList<Track>());
        ui = (PlayerUI)ctx;
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
                        performPlay();
                    }
                }
            }).start();
            ui.updateTotal(track.getDuration());
        } else {
            performPlay();
        }
    }

    public synchronized void pause() {
        try {
            if (mp != null) mp.pause();
        } catch (IllegalStateException ise) {}
    }

    protected synchronized void performPlay() {
        mp.start();
        handler.post(this);
    }

    public synchronized void run() {
        if (mp == null) return;
        try {
            ui.updateElapsed(mp.getCurrentPosition() / 1000);
            if (mp.isPlaying()) handler.postDelayed(this, 200);
        } catch (IllegalStateException ise) {}
    }

    public synchronized void seek(int time) {
        if (mp == null) return;
        try {
            mp.seekTo(time * 1000);
        } catch (IllegalStateException ise) {}
    }

    public synchronized void playPreviousTrack() {
        if (currentTrack == null) return;
        int pos = getPosition(currentTrack) - 1;
        if (pos >= 0) {
            play(getItem(pos), true);
        }
    }

    public synchronized void playNextTrack() {
        if (currentTrack == null) return;
        int pos = getPosition(currentTrack) + 1;
        if (pos < getCount()) {
            play(getItem(pos), true);
        }
    }
}
