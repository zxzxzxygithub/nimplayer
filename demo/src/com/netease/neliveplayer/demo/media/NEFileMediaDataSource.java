package com.netease.neliveplayer.demo.media;

import android.util.Log;

import com.netease.neliveplayer.sdk.NEMediaDataSource;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class NEFileMediaDataSource implements NEMediaDataSource {
    private static final String TAG = "NEFileMediaDataSource";
    private RandomAccessFile mFile;
    private long mFileSize;
    private String path;

    public NEFileMediaDataSource(File file) throws IOException {
        path = file.getPath();
        mFile = new RandomAccessFile(file, "r");
        mFileSize = mFile.length();
    }

    public NEFileMediaDataSource(String path) throws IOException {
        this.path = path;
        mFile = new RandomAccessFile(path, "r");
        mFileSize = mFile.length();
        Log.d(TAG, "path:" + path + ",fileSize" + mFileSize);
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        if (mFile.getFilePointer() != position)
            mFile.seek(position);

        if (size == 0)
            return 0;
        int result = mFile.read(buffer, 0, size);
        Log.d(TAG, "position:" + position + ",offset:" + offset+",size:"+"result:"+result);
        return result;
    }

    @Override
    public long getSize() throws IOException {
        Log.d(TAG, "getSize,mFileSize:" + mFileSize);
        return mFileSize;
    }

    @Override
    public void close() throws IOException {
        mFileSize = 0;
        mFile.close();
        mFile = null;
    }

    @Override
    public String getPath() {
        return path;
    }

}
