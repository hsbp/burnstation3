package org.hsbp.burnstation3;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;

public class Downloader {
    public static void download(URL source, File target) throws IOException {
        if (target.exists() && target.length() != 0) return;
        HttpURLConnection urlConnection = (HttpURLConnection) source.openConnection();
        try {
            InputStream input = new BufferedInputStream(urlConnection.getInputStream());
            try {
                OutputStream output = new FileOutputStream(target);
                try {
                    byte data[] = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
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
}
