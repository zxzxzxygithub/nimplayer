package com.netease.neliveplayer.demo.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.netease.neliveplayer.playerkit.sdk.LivePlayer;

/**
 * Created by netease on 17/4/6.
 */
public class PlayerService extends Service {
    private static LivePlayer sMediaPlayer;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, PlayerService.class);
        return intent;
    }

    public static void intentToStart(Context context) {
        context.startService(newIntent(context));
    }

    public static void intentToStop(Context context) {
        context.stopService(newIntent(context));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void setMediaPlayer(LivePlayer mp) {
        if (sMediaPlayer != null && sMediaPlayer != mp) {
            sMediaPlayer.stop();
            sMediaPlayer = null;
        }
        sMediaPlayer = mp;
    }

    public static LivePlayer getMediaPlayer() {
        return sMediaPlayer;
    }
}
