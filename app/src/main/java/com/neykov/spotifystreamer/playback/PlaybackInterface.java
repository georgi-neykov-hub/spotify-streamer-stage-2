package com.neykov.spotifystreamer.playback;

import kaaes.spotify.webapi.android.models.Track;

public interface PlaybackInterface {

    void play();
    void pause();
    void playNext();
    void playPrevious();
    void seekToPosition(int positionMillis);

    void setCurrentTrack(int trackNumber);
    void setTracklist(Track[] tracklist);
    void placeGlobalControls();

    Track getCurrentTrack();
    boolean isPlaying();

    void setPlaybackListener(PlaybackListener listener);
}
