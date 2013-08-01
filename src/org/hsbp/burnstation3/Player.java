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

    protected MediaPlayer mp = null;
    protected Item currentItem = null;
    protected final PlayerUI ui;
    protected final Handler handler = new Handler();

    public Player(Context ctx) {
        super(ctx, android.R.layout.simple_list_item_1, new ArrayList<Player.Item>());
        ui = (PlayerUI)ctx;
    }

    public synchronized void play(final Item item, boolean forceReplace) {
        if (mp != null && item != currentItem && forceReplace) {
            currentItem.setPlaying(false);
            mp.release();
            mp = null;
        }
        if (mp == null) {
            currentItem = item;
            new Thread(new Runnable() {
                protected static final int TRIAL_COUNT_TRESHOLD = 50;

                public void run() {
                    synchronized (Player.this) {
                        final Context ctx = getContext();
                        int trialCount = 0;
                        Uri track_uri = item.getTrack().getUri();
                        while (true) {
                            try {
                                mp = MediaPlayer.create(ctx, track_uri);
								mp.setOnCompletionListener(Player.this);
								mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
								mp.setWakeMode(ctx.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
								performPlay();
								return;
                            } catch (Exception e) {
								if (mp != null) {
									mp.release();
									mp = null;
								}
								try {
	                                Thread.sleep(200);
								} catch (InterruptedException ie) {}
                                trialCount++;
                                if (trialCount >= TRIAL_COUNT_TRESHOLD) {
                                    item.setPlaying(false);
                                    currentItem = null;
                                    return; // TODO notify user
                                }
                            }
                        }
                    }
                }
            }).start();
            item.setPlaying(true);
            ui.updateTotal(item.getTrack().getDuration());
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
        if (pos < getCount()) {
            play(getItem(pos), true);
        }
    }

    public void add(Track track) {
        add(new Item(track));
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
            return (playing ? "\u25B6 " : "") +
                track.getArtistName() + ": " + track.getName() +
                (db == Track.FULLY_DOWNLOADED ? "" :
                 getContext().getString(R.string.downloaded, db / 1024));
        }
    }
}
