package org.hsbp.burnstation3;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

public class Player extends ArrayAdapter<Player.Item> implements Runnable,
        MediaPlayer.OnCompletionListener {

    protected MediaPlayer mp;
    protected Item currentItem;
    protected final PlayerUI ui;
    protected final Handler handler = new Handler();

    public Player(Context ctx, PlayerUI ui) {
        super(ctx, android.R.layout.simple_list_item_1, new ArrayList<Player.Item>());
        ui.setPlayer(this);
        this.ui = ui;
    }

    public synchronized void play(final Item item, boolean forceReplace) {
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
            item.setPlaying(true);
            ui.updateTotal(item.getTrack().duration);
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
            final Item i = new Item(track);
            add(i);
            if (first) {
                if (mp == null || !mp.isPlaying()) play(i, true);
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

    protected class Item implements Track.Notifiable, Runnable {
        protected final Track track;
        protected boolean playing = false;
        protected final Handler handler = new Handler();

        public Item(Track track) {
            this.track = track;
            track.subscribe(this);
        }

        public void trackInfoChanged() {
            handler.post(this);
        }

        public void run() {
            notifyDataSetChanged();
        }

        public Track getTrack() {
            return track;
        }

        public void setPlaying(boolean value) {
            if (value != playing) {
                playing = value;
                handler.post(this);
            }
        }

        @Override
        public String toString() {
			int db = track.getDownloadedBytes();
            StringBuilder sb = new StringBuilder();
            if (playing) sb.append(track.isReadyToPlay() ? "\u25B6 " : "\u231B ");
            sb.append(track.artistName).append(": ").append(track.name);
            if (db != Track.FULLY_DOWNLOADED) sb.append(' ').append(
                    getContext().getString(R.string.downloaded, db / 1024));
            return sb.toString();
        }
    }
}
