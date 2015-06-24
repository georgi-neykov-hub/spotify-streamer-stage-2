package com.neykov.spotifystreamer.playback;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

public class PositionUpdater {

    private static final long DEFAULT_UPDATE_INTERVAL_MS = 200;

    private Handler mHandler;
    private long mUpdateInterval;

    private PositionListener mListener;
    private MediaPlayer mPlayer;

    public PositionUpdater() {
        mHandler = new Handler(Looper.getMainLooper());
        mUpdateInterval = DEFAULT_UPDATE_INTERVAL_MS;
    }

    public void setUpdateInterval(long milliseconds) {
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("Update interval must be > 0.");
        }
        mUpdateInterval = milliseconds;
    }

    public void startUpdating(MediaPlayer player) {
        mPlayer = player;
        mHandler.postDelayed(mUpdateRunnable, mUpdateInterval);
    }

    public void stopUpdating() {
        mHandler.removeCallbacks(mUpdateRunnable);
        mPlayer = null;
    }

    public void setPositionListener(PositionListener listener) {
        mListener = listener;
    }

    private Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayer != null && mPlayer.isPlaying()) {
                if (mListener != null) {
                    int currentPos = mPlayer.getCurrentPosition();
                    int duration = mPlayer.getDuration();
                    mListener.onPositionUpdate(currentPos, duration);
                }
            }

            //Re-schedule
            mHandler.postDelayed(this, mUpdateInterval);
        }
    };
}
