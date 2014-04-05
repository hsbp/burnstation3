package org.hsbp.burnstation3;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.*;
import java.util.*;
import java.io.*;
import org.json.*;

public class TrackListFillTask extends AsyncTask<String, Void, List<Track>> {
    
    protected final ListView view;
    protected final Context ctx;
    protected final PlayerUI ui;

    public TrackListFillTask(final ListView view, final Context ctx, final PlayerUI ui) {
        this.view = view;
        this.ctx = ctx;
        this.ui = ui;
    }

    @Override
    protected List<Track> doInBackground(final String... albumId) {
        final JsonArrayProcessor<Track> jap = new JsonArrayProcessor<Track>(ui) {
            public Track mapItem(final JSONObject item) throws JSONException, IOException {
                return Track.fromJSONObject(ctx, item);
            }
        };
        return jap
            .setMessage(JsonArrayProcessor.State.CONSTUCTION, R.string.api_track_construction_error)
            .setMessage(JsonArrayProcessor.State.EXTRACTION, R.string.api_track_extraction_error)
            .setMessage(JsonArrayProcessor.State.IO, R.string.api_track_io_error)
            .process("tracks", "&album_id=" + albumId[0]);
    }

    @Override
    protected void onPostExecute(List<Track> result) {
        if (!result.isEmpty()) {
            view.setAdapter(new ArrayAdapter<Track>(ctx,
                        android.R.layout.simple_list_item_1, result));
        }
        ui.hideIndeterminateProgressDialog();
    }
}
