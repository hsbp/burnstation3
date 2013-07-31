package org.hsbp.burnstation3;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.os.PowerManager;

public class Player {

    protected MediaPlayer mp = null;

    public synchronized void play(final Context ctx, final Track track) {
        if (mp == null) {
            new Thread(new Runnable() {
                public void run() {
                    synchronized (Player.this) {
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
}
