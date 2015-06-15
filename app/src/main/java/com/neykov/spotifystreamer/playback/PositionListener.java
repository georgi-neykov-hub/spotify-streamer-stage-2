package com.neykov.spotifystreamer.playback;

public interface PositionListener {
    void onPositionUpdate(int currentPosMs, int totalLengthMs);
}
