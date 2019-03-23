package com.netease.neliveplayer.demo.media;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.util.Log;

import com.netease.neliveplayer.demo.util.ThreadUtils;
import com.netease.neliveplayer.sdk.NEMediaDataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * APP包内部assets和raw文件夹下点播文件自定义DataSource示例代码
 * 示例代码中为了兼容mp4的头信息在文件头和文件尾两种情况的mp4将数据先读取到内存，因此对视频大小有限制，建议不要超过10M，用户可以根据自身情况修改示例代码
 * 支持mp4、flv格式
 * 设置的url路径格式：
 * (1)raw下文件："android.resource://" + 包名  + ":raw/" + 文件名不带后缀
 * (2)assets下文件：file:///android_asset/文件名带后缀
 */
public class NERawMediaDataSource implements NEMediaDataSource {
    private static final String TAG = "NERawMediaDataSource";

    private String path;
    private byte[] mediaBytes;
    private AssetFileDescriptor fileDescriptor;
    private long length = -1;

    public NERawMediaDataSource(Context context, String url) {
        Log.i(TAG, "NERawMediaDataSource new url:" + url + ThreadUtils.getThreadInfo());
        this.path = url;
        //"android.resource://" + 包名 + ":raw/" + 文件名不带后缀
        if (url.startsWith("android.resource://")) {
            try {
                int raw = context.getResources().getIdentifier(url.substring(url.lastIndexOf("/") + 1), "raw", context.getPackageName()); // R.raw.raw_video
                fileDescriptor = context.getResources().openRawResourceFd(raw);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
        //"file:///android_asset/文件名带后缀"
        if (url.startsWith("file:///android_asset/")) {

            try {
                fileDescriptor = context.getAssets().openFd(url.substring(url.lastIndexOf("/") + 1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        Log.i(TAG, "NERawMediaDataSource position：" + position + ",buffer length:" + buffer.length + ",offset:" + offset + ",size:" + size + ThreadUtils.getThreadInfo());
        if (fileDescriptor == null) {
            return -1;
        }
        if (mediaBytes == null) {
            InputStream inputStream = fileDescriptor.createInputStream();
            mediaBytes = readBytes(inputStream);
        }
        if (position + 1 >= mediaBytes.length) {
            return -1;
        }
        if (size == 0) {
            return 0;
        }
        int length;
        if (position + size < mediaBytes.length) {
            length = size;
        } else {
            length = (int) (mediaBytes.length - position);
            if (length > buffer.length) {
                length = buffer.length;
            }
        }
        Log.i(TAG, "NERawMediaDataSource mediaBytes size：" + mediaBytes.length + ",position:" + position + ",buffer size:" + buffer.length + ",offset:" + offset + ",length:" + length);
        System.arraycopy(mediaBytes, (int) position, buffer, offset, length);

        return length;
    }

    @Override
    public long getSize() throws IOException {
        if(fileDescriptor!=null){
            length = fileDescriptor.getLength();
        }
        Log.i(TAG, "NERawMediaDataSource getSize end:" + System.currentTimeMillis() + ",length:" + length + ThreadUtils.getThreadInfo());
        return length;
    }

    @Override
    public void close() throws IOException {
        Log.i(TAG, "NERawMediaDataSource close" + ThreadUtils.getThreadInfo());
        if (fileDescriptor != null) {
            fileDescriptor.close();
        }
        fileDescriptor = null;
        mediaBytes = null;
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }


    @Override
    public String getPath() {
        return path;
    }
}
