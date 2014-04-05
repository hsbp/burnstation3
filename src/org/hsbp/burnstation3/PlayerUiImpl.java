package org.hsbp.burnstation3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.*;

public class PlayerUiImpl implements PlayerUI, SeekBar.OnSeekBarChangeListener {
	protected final Activity activity;
	public final static String TIME_FMT = "%d:%02d";
	protected boolean seekerUpdateEnabled = true;
	protected SeekBar sb;
	protected Player player;
	protected ProgressDialog progDlg;

	public PlayerUiImpl(Activity activity) {
		this.activity = activity;
		sb = (SeekBar)activity.findViewById(R.id.player_seek);
		sb.setOnSeekBarChangeListener(this);
	}

    public void handleException(final int message, final Exception e) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
        e.printStackTrace();
    }

    public void updateElapsed(int time) {
        if (seekerUpdateEnabled) {
            sb.setProgress(time);
        }
        updateTextViewTime(R.id.player_elapsed, time / 1000);
    }

    public void updateTotal(int time) {
        sb.setMax(time * 1000);
        updateTextViewTime(R.id.player_total, time);
    }

    protected void updateTextViewTime(int res, int time) {
        TextView tv = (TextView)activity.findViewById(res);
        tv.setText(String.format(TIME_FMT, time / 60, time % 60));
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) player.seek(progress);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        seekerUpdateEnabled = false;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        seekerUpdateEnabled = true;
    }

	public void setPlayer(Player p) {
		player = p;
	}

	public void showIndeterminateProgressDialog(String msg) {
		hideIndeterminateProgressDialog();
		progDlg = new ProgressDialog(activity);
		progDlg.setMessage(msg);
		progDlg.setIndeterminate(true);
		progDlg.show();
	}

	public void hideIndeterminateProgressDialog() {
		if (progDlg == null) return;
		progDlg.dismiss();
		progDlg = null;
	}
}
