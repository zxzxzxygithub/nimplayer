package com.netease.neliveplayer.demo.activity.advance;

import com.netease.neliveplayer.playerkit.sdk.VodPlayerObserver;
import com.netease.neliveplayer.playerkit.sdk.model.MediaInfo;
import com.netease.neliveplayer.playerkit.sdk.model.StateInfo;

public class ShortPlayerObserver implements VodPlayerObserver {
        private int position;

        public ShortPlayerObserver(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        @Override
        public void onCurrentPlayProgress(long currentPosition, long duration, float percent, long cachedPosition) {

        }

        @Override
        public void onSeekCompleted() {

        }

        @Override
        public void onCompletion() {

        }

        @Override
        public void onAudioVideoUnsync() {

        }

        @Override
        public void onNetStateBad() {

        }

        @Override
        public void onDecryption(int ret) {

        }

        @Override
        public void onPreparing() {

        }

        @Override
        public void onPrepared(MediaInfo mediaInfo) {

        }

        @Override
        public void onError(int code, int extra) {

        }

        @Override
        public void onFirstVideoRendered() {

        }

        @Override
        public void onFirstAudioRendered() {

        }

        @Override
        public void onBufferingStart() {

        }

        @Override
        public void onBufferingEnd() {

        }

        @Override
        public void onBuffering(int percent) {

        }

        @Override
        public void onVideoDecoderOpen(int value) {

        }

        @Override
        public void onStateChanged(StateInfo stateInfo) {

        }

        @Override
        public void onHttpResponseInfo(int code, String header) {

        }
    }