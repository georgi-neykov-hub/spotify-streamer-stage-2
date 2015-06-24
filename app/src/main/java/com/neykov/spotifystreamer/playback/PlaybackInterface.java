package com.neykov.spotifystreamer.playback;

import kaaes.spotify.webapi.android.models.Track;

public interface PlaybackInterface {

    void play();
    void pause();
    void playNext();
    void playPrevious();
    void seekToPosition(int positionMillis);

    void placeNotificationControls();
    void removeNotificationControls();

    Track getCurrentTrack();
    int getCurrentPosition();
    int getDuration();
    boolean isPlaying();
    boolean isActive();
    boolean isLoading();

    void setPlaybackListener(PlaybackListener listener);
}
