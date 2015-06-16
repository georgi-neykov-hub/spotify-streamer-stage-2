package com.neykov.spotifystreamer.playback;

import kaaes.spotify.webapi.android.models.Track;

public interface PlaybackListener extends PositionListener {
    void onLoadStart();

    void onLoadDone();

    void onError();

    void onTrackChanged(Track currentTrack);

    void onPlaybackStateChanged(boolean running);
}
