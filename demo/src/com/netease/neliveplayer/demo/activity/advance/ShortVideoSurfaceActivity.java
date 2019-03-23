package com.netease.neliveplayer.demo.activity.advance;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.netease.neliveplayer.demo.R;
import com.netease.neliveplayer.playerkit.sdk.PlayerManager;
import com.netease.neliveplayer.playerkit.sdk.VodPlayer;
import com.netease.neliveplayer.playerkit.sdk.VodPlayerObserver;
import com.netease.neliveplayer.playerkit.sdk.model.AutoRetryConfig;
import com.netease.neliveplayer.playerkit.sdk.model.CacheConfig;
import com.netease.neliveplayer.playerkit.sdk.model.DataSourceConfig;
import com.netease.neliveplayer.playerkit.sdk.model.VideoBufferStrategy;
import com.netease.neliveplayer.playerkit.sdk.model.VideoOptions;
import com.netease.neliveplayer.playerkit.sdk.model.VideoScaleMode;
import com.netease.neliveplayer.playerkit.sdk.view.AdvanceSurfaceView;
import com.netease.neliveplayer.sdk.model.NEAutoRetryConfig;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ShortVideoSurfaceActivity extends Activity {
    private String TAG = "ShortVideoSurfaceActivity";


    private List<String> mLiveUrlList;
    private boolean isHardWare = false;
    private boolean isErrorShow = false;
    private RecyclerView mRecyclerView;
    private ViewPagerLayoutManager mLayoutManager;
    private SurfaceVideoAdapter adapter;
    private int lastPosition;
    private boolean isDown;

    private LinkedList<PlayerInfo> playerInfos = new LinkedList<>();
    private AutoRetryConfig autoRetryConfig;
    private int bufferPageCount = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short_video_surface);
        parseIntent();
        initData();
        initView();
    }


    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAllPlayer();
    }


    private void parseIntent() {
        //接收MainActivity传过来的参数
        String mDecodeType = getIntent().getStringExtra("decode_type");

        if (mDecodeType != null && mDecodeType.equals("hardware")) {
            isHardWare = true;
        } else {
            isHardWare = false;
        }

    }

    private void initData() {
        mLiveUrlList = new ArrayList<>();
        mLiveUrlList.add("http://vodhj5bqn44.vod.126.net/vodhj5bqn44/7eSdPRKt_1818589543_shd.mp4");
        mLiveUrlList.add("http://vodhj5bqn44.vod.126.net/vodhj5bqn44/1BrIAtvV_1818587477_shd.mp4");
        mLiveUrlList.add("http://vodhj5bqn44.vod.126.net/vodhj5bqn44/FmdVOTqd_1818586962_shd.mp4");
        mLiveUrlList.add("http://vodhj5bqn44.vod.126.net/vodhj5bqn44/wq1e35cQ_1818588221_shd.mp4");

    }

    private void initView() {
        autoRetryConfig = new AutoRetryConfig();
        autoRetryConfig.count = 0;
        autoRetryConfig.delayDefault = 3000;
        autoRetryConfig.retryListener = onRetryListener;

        mRecyclerView = findViewById(R.id.rv_video_list);
        mLayoutManager = new ViewPagerLayoutManager(this, OrientationHelper.VERTICAL);
        mLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {
                Log.i(TAG, "onInitComplete");
                lastPosition = adapter.getItemCount() / 2;
                startPlayer(lastPosition);
                for (PlayerInfo playerInfo : playerInfos) {
                    if (playerInfo.position == lastPosition) {
                        playerInfo.vodPlayer.setBufferSize(50 * 1024 * 1024);
                        playerInfo.vodPlayer.setMute(false);
                        View view = mRecyclerView.getChildAt(0);
                        playerInfo.vodPlayer.setupRenderView((AdvanceSurfaceView) view.findViewById(R.id.live_surface), VideoScaleMode.FIT);
                    }
                }
                for (int i = 1; i <= bufferPageCount; i++) {
                    startPlayer(lastPosition + i);
                    startPlayer(lastPosition - i);
                }


            }

            @Override
            public void onPageRelease(boolean isNext, int position) {
                Log.i(TAG, "onPageRelease,position:" + position);
                releasePlayer(position);
            }

            @Override
            public void onPageSelected(int position, boolean isBottom) {
                Log.i(TAG, "onPageSelected,position:" + position);
                for (PlayerInfo playerInfo : playerInfos) {
                    if (playerInfo.position == lastPosition) {
                        playerInfo.vodPlayer.setMute(true);
                        playerInfo.vodPlayer.setBufferSize(20 * 1024 * 1024);
                        playerInfo.vodPlayer.seekTo(0);
                        playerInfo.vodPlayer.pause();
                    }
                }
                isDown = position > lastPosition ? true : false;
                lastPosition = position;
                reStartPlayer(position);
                startPlayer(isDown ? position + bufferPageCount : position - bufferPageCount);
                releasePlayer(isDown ? position - (bufferPageCount + 1) : position + (bufferPageCount + 1));
            }
        });
        adapter = new SurfaceVideoAdapter(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.scrollToPosition(adapter.getItemCount() / 2);
    }

    private void reStartPlayer(int position) {
        boolean isExist = false;
        for (PlayerInfo playerInfo : playerInfos) {
            if (playerInfo.position == position) {
                isExist = true;
                playerInfo.vodPlayer.setBufferSize(50 * 1024 * 1024);
                playerInfo.vodPlayer.setMute(false);
                View view = mRecyclerView.getChildAt(0);
                playerInfo.vodPlayer.setupRenderView((AdvanceSurfaceView) view.findViewById(R.id.live_surface), VideoScaleMode.FIT);
                playerInfo.vodPlayer.start();
            }
        }
        if(!isExist) {
            PlayerInfo playerInfo = startPlayer(position);
            View view = mRecyclerView.getChildAt(0);
            playerInfo.vodPlayer.setupRenderView((AdvanceSurfaceView) view.findViewById(R.id.live_surface), VideoScaleMode.FIT);
        }
    }

    private void releaseAllPlayer() {
        for (PlayerInfo playerInfo : playerInfos) {
            if (playerInfo != null && playerInfo.vodPlayer != null) {
                playerInfo.vodPlayer.setupRenderView(null, VideoScaleMode.FIT);
                playerInfo.vodPlayer.registerPlayerObserver(playerInfo.playerObserver, false);
                playerInfo.vodPlayer.setMute(true);
                playerInfo.vodPlayer.stop();
                playerInfo.vodPlayer = null;
                playerInfo.playerObserver = null;
            }
        }
        playerInfos.clear();
        playerInfos = null;
        autoRetryConfig = null;
    }


    private void releasePlayer(int position) {
        Log.i(TAG, "releasePlayer,position:" + position );
        PlayerInfo playerInfo = null;
        for (PlayerInfo curPlayerInfo : playerInfos) {
            if (curPlayerInfo.position == position) {
                playerInfo = curPlayerInfo;
            }
        }
        if (playerInfo != null && playerInfo.vodPlayer != null) {
            playerInfo.vodPlayer.setupRenderView(null, VideoScaleMode.FIT);
            playerInfo.vodPlayer.registerPlayerObserver(playerInfo.playerObserver, false);
            playerInfo.vodPlayer.setMute(true);
            playerInfo.vodPlayer.stop();
            playerInfo.vodPlayer = null;
            playerInfo.playerObserver = null;
            playerInfos.remove(playerInfo);
        }

    }

    public PlayerInfo findPlayerInfo(int position) {
        for (int i = 0; i < playerInfos.size(); i++) {
            PlayerInfo playerInfo = playerInfos.get(i);
            if (playerInfo.position == position) {
                return playerInfo;
            }
        }
        return null;
    }

    private PlayerInfo startPlayer(final int position) {
        VideoOptions options = new VideoOptions();
        options.bufferSize = 50 * 1024 * 1024;
        options.hardwareDecode = isHardWare;
        options.bufferStrategy = VideoBufferStrategy.ANTI_JITTER;
        options.loopCount = -1;
        options.isAccurateSeek = true;
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.cacheConfig= new CacheConfig(true,null);
        options.dataSourceConfig = dataSourceConfig;
        VodPlayer vodPlayer = PlayerManager.buildVodPlayer(this, mLiveUrlList.get(position % mLiveUrlList.size()), options);
        VodPlayerObserver playerObserver = new ShortPlayerObserver(position) {

            @Override
            public void onError(int code, int extra) {
                Log.i(TAG, "player mCurrentPosition:" + position + " ,onError code:" + code + " extra:" + extra);
                if (!isErrorShow && !findPlayerInfo(getPosition()).vodPlayer.isPlaying()) {
                    isErrorShow = true;
                    //发生错误时，这里Demo会弹出对话框提示错误，用户可以在此根据错误码进行重试或者其他操作
                    AlertDialog.Builder build = new AlertDialog.Builder(ShortVideoSurfaceActivity.this);
                    build.setTitle("播放错误").setMessage("错误码：" + code)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    isErrorShow = false;
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            }

            @Override
            public void onFirstVideoRendered() {
                Log.i(TAG, "onFirstVideoRendered，mCurrentPosition->" + position + ",getPosition()->" + getPosition());
                if (lastPosition != getPosition()) {
                    for (PlayerInfo curPlayerInfo : playerInfos) {
                        if (curPlayerInfo.position == getPosition()) {
                            curPlayerInfo.vodPlayer.setMute(true);
                            curPlayerInfo.vodPlayer.pause();
                        }
                    }

                }
            }

        };
        vodPlayer.setAutoRetryConfig(autoRetryConfig);
        vodPlayer.registerPlayerObserver(playerObserver, true);
        Log.i(TAG, "play start,instantiatePlayerInfo position" + position + "，vodPlayer" + vodPlayer);
        vodPlayer.start();
        PlayerInfo playerInfo = new PlayerInfo(vodPlayer, playerObserver, position);
        playerInfos.add(playerInfo);
        return playerInfo;
    }

    private NEAutoRetryConfig.OnRetryListener onRetryListener = new NEAutoRetryConfig.OnRetryListener() {

        @Override
        public void onRetry(int what, int extra) {
            showToast("开始重试，错误类型："+what+ "，附加信息："+extra);
        }
    };

    private class PlayerInfo {
        public VodPlayer vodPlayer;
        public VodPlayerObserver playerObserver;
        public int position;

        public PlayerInfo(VodPlayer vodPlayer, VodPlayerObserver playerObserver, int position) {
            this.vodPlayer = vodPlayer;
            this.playerObserver = playerObserver;
            this.position = position;
        }
    }


    private void showToast(String msg) {
        Log.d(TAG, "showToast" + msg);
        try {
            Toast.makeText(ShortVideoSurfaceActivity.this, msg, Toast.LENGTH_SHORT).show();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

}
