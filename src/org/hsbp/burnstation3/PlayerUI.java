package org.hsbp.burnstation3;

public interface PlayerUI {
    public void updateElapsed(int time);
    public void updateTotal(int time);
    public void showIndeterminateProgressDialog(String msg);
    public void hideIndeterminateProgressDialog();
    public void handleException(int message, Exception e);
    public void setPlayer(Player p);
    public boolean isRepeatEnabled();
}
