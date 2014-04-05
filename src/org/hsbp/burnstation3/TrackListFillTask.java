package org.hsbp.burnstation3;

import android.app.Activity;
import android.widget.*;
import java.util.*;
import java.io.*;
import org.json.*;

public class TrackListFillTask extends ListFillTask<String, Track> {

    public TrackListFillTask(final Activity activity, final PlayerUI ui) {
        super(R.id.tracks, activity, ui);
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

    protected BaseAdapter getAdapter(final List<Track> result) {
        return new ArrayAdapter<Track>(ctx, android.R.layout.simple_list_item_1, result);
    }
}
