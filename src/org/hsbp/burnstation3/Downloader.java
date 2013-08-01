package org.hsbp.burnstation3;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;

public class Downloader {
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
}
