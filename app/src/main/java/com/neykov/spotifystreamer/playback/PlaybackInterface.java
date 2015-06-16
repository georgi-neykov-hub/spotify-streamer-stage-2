package com.neykov.spotifystreamer.playback;

import kaaes.spotify.webapi.android.models.Track;

public interface PlaybackInterface {

    void play();
    void pause();
    void playNext();
    void playPrevious();
    void seekToPosition(int positionMillis);

    void placeGlobalControls();

    Track getCurrentTrack();
    boolean isPlaying();
    boolean isActive();

    void setPlaybackListener(PlaybackListener listener);
}
