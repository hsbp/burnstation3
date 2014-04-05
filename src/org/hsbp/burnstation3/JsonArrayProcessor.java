package org.hsbp.burnstation3;

import java.io.IOException;
import java.util.*;
import org.json.*;

public abstract class JsonArrayProcessor<E> {
	protected static final EnumMap<State, Integer> messages =
		new EnumMap<State, Integer>(State.class);
	protected final PlayerUI ui;

	public enum State { CONSTUCTION, EXTRACTION, IO }

	public JsonArrayProcessor(PlayerUI ui) {
		this.ui = ui;
	}

	public JsonArrayProcessor<E> setMessage(State s, Integer message) {
		messages.put(s, message);
		return this;
	}

	public List<E> process(String resource, String parameters) {
		final ArrayList<E> result = new ArrayList<E>();
		try {
			final JSONArray array = API.getArray(resource, parameters);
			final int count = array.length();
			result.ensureCapacity(count);
			for (int i = 0; i < count; i++) {
				try {
					result.add(mapItem(array.getJSONObject(i)));
				} catch (JSONException je) {
					ui.handleException(messages.get(State.CONSTUCTION), je);
				}
			}
		} catch (JSONException je) {
			ui.handleException(messages.get(State.EXTRACTION), je);
		} catch (IOException ioe) {
			ui.handleException(messages.get(State.IO), ioe);
		}
		return result;
	}

	public abstract E mapItem(JSONObject item) throws JSONException, IOException;
}
