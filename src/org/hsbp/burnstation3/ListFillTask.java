package org.hsbp.burnstation3;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.*;
import java.util.List;

public abstract class ListFillTask<K, V> extends AsyncTask<K, Void, List<V>> {

    protected final ListView view;
    protected final Context ctx;
    protected final PlayerUI ui;

    public ListFillTask(final int id, final Activity activity, final PlayerUI ui) {
        this.view = (ListView)activity.findViewById(id);
        this.ctx = activity;
        this.ui = ui;
    }

    protected void executeWithMessage(K value, int msg, String param) {
        ui.showIndeterminateProgressDialog(ctx.getString(msg, param));
        execute(value);
    }

    @Override
    protected void onPostExecute(final List<V> result) {
        if (!result.isEmpty()) view.setAdapter(getAdapter(result));
        ui.hideIndeterminateProgressDialog();
    }

    protected abstract BaseAdapter getAdapter(List<V> result);
}
