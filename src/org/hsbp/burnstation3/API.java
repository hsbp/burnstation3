package org.hsbp.burnstation3;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import org.apache.commons.io.IOUtils;
import org.json.*;

public class API {

    public final static String CLIENT_ID = "5559df65";
    public static final String UTF_8 = "UTF-8";

    public static void download(URL source, File target, Notifiable watcher) throws IOException {
        if (target.exists() && target.length() != 0) {
            if (watcher != null) watcher.completed(target);
            return;
        }
        HttpURLConnection urlConnection = (HttpURLConnection) source.openConnection();
        try {
            InputStream input = new BufferedInputStream(urlConnection.getInputStream());
            try {
                OutputStream output = new FileOutputStream(target);
                try {
                    byte data[] = new byte[4096];
                    int count, total = 0;
                    while ((count = input.read(data)) != -1) {
                        if (watcher != null) {
                            total += count;
                            watcher.downloaded(total);
                        }
                        output.write(data, 0, count);
                    }
                    if (watcher != null) watcher.completed(target);
                } finally {
                    output.close();
                }
            } finally {
                input.close();
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public interface Notifiable {
        public void downloaded(int bytes);
        public void completed(File target);
    }

    public static JSONArray getArray(String resource, String parameters)
        throws IOException, JSONException {
        URL api = new URL("http://api.jamendo.com/v3.0/" + resource +
                ("/?client_id=" + CLIENT_ID + "&format=json") + parameters);
        HttpURLConnection urlConnection = (HttpURLConnection) api.openConnection();
        try {
            String response = IOUtils.toString(urlConnection.getInputStream(), UTF_8);
            JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
            return object.getJSONArray("results");
        } finally {
            urlConnection.disconnect();
        }
    }
}
