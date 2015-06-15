package com.neykov.spotifystreamer.playback;

public interface PlaybackListener extends PositionListener {
    void onLoadStart();

    void onLoadDone();

    void onError();
}
