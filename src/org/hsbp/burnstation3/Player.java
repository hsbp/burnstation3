package org.hsbp.burnstation3;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.ArrayAdapter;
import java.util.*;

public class Player extends ArrayAdapter<PlaylistItem> implements Runnable,
        MediaPlayer.OnCompletionListener {

    protected MediaPlayer mp;
    protected PlaylistItem currentItem;
    protected final PlayerUI ui;
    protected final Handler handler = new Handler();

    public Player(Context ctx, PlayerUI ui) {
        super(ctx, android.R.layout.simple_list_item_1, new ArrayList<PlaylistItem>());
        ui.setPlayer(this);
        this.ui = ui;
    }

    public synchronized void play(final PlaylistItem item, boolean forceReplace) {
        if (mp != null && item != currentItem && forceReplace) {
            currentItem.setPlaying(false);
            releaseMediaPlayer();
        }
        if (mp == null) {
            currentItem = item;
            new Thread(new Runnable() {
                protected static final int TRIAL_COUNT_TRESHOLD = 50;

                public void run() {
                    synchronized (Player.this) {
                        int trialCount = 0;
                        final Track track = item.getTrack();
						waitForReadyTrack(track);
                        while (true) {
                            try {
								setupMediaPlayer(track);
								performPlay();
								return;
                            } catch (Exception e) {
								releaseMediaPlayer();
								try {
	                                Thread.sleep(200);
								} catch (InterruptedException ie) {}
                                trialCount++;
                                if (trialCount >= TRIAL_COUNT_TRESHOLD) {
                                    item.setPlaying(false);
                                    currentItem = null;
                                    ui.handleException(R.string.media_play_error, e);
                                    return;
                                }
                            }
                        }
                    }
                }

                protected void setupMediaPlayer(Track track) {
                    final Context ctx = getContext();
                    mp = MediaPlayer.create(ctx, track.getUri());
                    mp.setOnCompletionListener(Player.this);
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mp.setWakeMode(ctx.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                }

                protected void waitForReadyTrack(Track track) {
                    while (!track.isReadyToPlay()) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ie) {}
                    }
                }
            }).start();
        } else {
            performPlay();
        }
        item.setPlaying(true);
        ui.updateTotal(item.getTrack().duration);
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

    public synchronized boolean isPlaying() {
        return mp == null ? false : mp.isPlaying();
    }

    public synchronized void run() {
        if (mp == null) return;
        try {
            ui.updateElapsed(mp.getCurrentPosition());
            if (mp.isPlaying()) handler.postDelayed(this, 200);
        } catch (IllegalStateException ise) {}
    }

    public synchronized void seek(int time) {
        if (mp == null) return;
        try {
            mp.seekTo(time);
        } catch (IllegalStateException ise) {}
    }

    public void onCompletion(MediaPlayer mp) {
        currentItem.setPlaying(false);
        playNextTrack();
    }

    public synchronized void playPreviousTrack() {
        if (currentItem == null) return;
        int pos = getPosition(currentItem) - 1;
        if (pos >= 0) {
            play(getItem(pos), true);
        }
    }

    public synchronized void playNextTrack() {
        if (currentItem == null) return;
        int pos = getPosition(currentItem) + 1;
        if ((pos == getCount()) && ui.isRepeatEnabled()) pos = 0;
        if (pos < getCount()) {
            play(getItem(pos), true);
        }
    }

    public synchronized void add(final Iterable<Track> tracks) {
        boolean first = true;
        for (final Track track : tracks) {
            track.prepare(ui);
            final PlaylistItem i = new PlaylistItem(track, this);
            add(i);
            if (first) {
                if (!isPlaying()) play(i, true);
                first = false;
            }
        }
    }

    public synchronized void clear() {
        releaseMediaPlayer();
        ui.updateElapsed(0);
        super.clear();
    }

    protected synchronized void releaseMediaPlayer() {
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }
}
