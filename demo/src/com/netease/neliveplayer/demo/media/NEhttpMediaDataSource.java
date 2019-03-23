package com.netease.neliveplayer.demo.media;


import android.util.Log;

import com.netease.neliveplayer.demo.util.ThreadUtils;
import com.netease.neliveplayer.sdk.NEMediaDataSource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NEhttpMediaDataSource implements NEMediaDataSource {
    private static final String TAG = "NEhttpMediaDataSource";

    private URL url;
    private HttpURLConnection connection;
    private BufferedInputStream inputStream;
    private long length = -1;
    private long locate;

    public NEhttpMediaDataSource(URL url) throws IOException {
        Log.i(TAG, "NEhttpMediaDataSource:new" + ThreadUtils.getThreadInfo());
        this.url = url;
    }

    public NEhttpMediaDataSource(String urlStr) throws IOException {
        this(new URL(urlStr));
    }

    @Override
    public long getSize() throws IOException {
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept-Encoding", "identity");
        connection.setConnectTimeout(10 * 1000);
        connection.setReadTimeout(10 * 1000);
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("http response code " + responseCode);
        }
        length = connection.getContentLength();
        if (inputStream == null) {
            inputStream = new BufferedInputStream(connection.getInputStream());
        }
        Log.i(TAG, "getSize" + length + ThreadUtils.getThreadInfo());
        return length > 0 ? length : -1;
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        if (locate != position) {
            disconnect();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("range", "bytes=" + position + "-");
            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(10 * 1000);
            int responseCode = connection.getResponseCode();
            if (responseCode != 200 &&responseCode != 206) {
                throw new IOException("http response code " + responseCode);
            }
            if (inputStream == null) {
                inputStream = new BufferedInputStream(connection.getInputStream());
            }
        }
        if (size == 0) {
            return 0;
        }
        int result = inputStream.read(buffer, offset, size);
        if (locate == position) {
            locate += result;
        } else {
            locate = position + result;
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        Log.i(TAG, "close " + ThreadUtils.getThreadInfo());
        disconnect();
    }

    @Override
    public String getPath() {
        return url.toString();
    }

    private void disconnect ()throws IOException {
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }
}