package org.hsbp.burnstation3;

public interface PlayerUI {
    void updateElapsed(int time);
    void updateTotal(int time);
    void showIndeterminateProgressDialog(String msg);
    void hideIndeterminateProgressDialog();
    void handleException(int message, Exception e);
    void setPlayer(Player p);
    boolean isRepeatEnabled();
    void setState(PlayerState value);
    PlayerState getState();
}
