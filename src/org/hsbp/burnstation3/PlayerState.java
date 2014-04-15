package org.hsbp.burnstation3;

public enum PlayerState {
	PAUSED(R.drawable.av_play),
	PLAYING(R.drawable.av_pause);

	public final int buttonIcon;

	PlayerState(final int buttonIcon) {
		this.buttonIcon = buttonIcon;
	}
}
