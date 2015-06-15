package com.neykov.spotifystreamer.playback;

import android.os.Binder;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackBinder extends Binder implements PlaybackInterface {

    private PlaybackInterface mService;

    /*package*/ PlaybackBinder(PlaybackService service) {
        this.mService = service;
    }

    @Override
    public void setTracklist(Track[] tracklist) {
        mService.setTracklist(tracklist);
    }

    @Override
    public void placeGlobalControls() {
        mService.placeGlobalControls();
    }

    @Override
    public void play() {
        mService.play();
    }

    @Override
    public void pause() {
        mService.pause();
    }

    @Override
    public void playNext() {
        mService.pause();
    }

    @Override
    public void playPrevious() {
        mService.playPrevious();
    }

    @Override
    public void seekToPosition(int positionMillis) {
        mService.seekToPosition(positionMillis);
    }

    @Override
    public void setCurrentTrack(int trackNumber) {
        mService.setCurrentTrack(trackNumber);
    }

    @Override
    public Track getCurrentTrack() {
        return mService.getCurrentTrack();
    }

    @Override
    public boolean isPlaying() {
        return mService.isPlaying();
    }

    @Override
    public void setPlaybackListener(PlaybackListener listener) {
        mService.setPlaybackListener(listener);
    }
}
