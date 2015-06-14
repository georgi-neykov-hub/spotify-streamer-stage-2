package com.neykov.spotifystreamer.playback;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public interface PlaybackInterface {
    interface PlaybackListener{
        void onLoadStart();
        void onSeekPositionUpdate(int currentPosMs, int totalLengthMs);
        void onLoadDone();
        void onError();
    }

    void setTracklist(Track[] tracklist);

    void play();
    void pause();
    void playNext();
    void playPrevious();

    void setCurrentTrack(int trackNumber);

    Track getCurrentTrack();
    void setPlaybackListener(PlaybackListener listener);
}
