package com.neykov.spotifystreamer.playback;

import android.os.Binder;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackBinder extends Binder implements PlaybackInterface{

    private PlaybackInterface mService;

    /*package*/ PlaybackBinder(PlaybackService service){
        this.mService = service;
    }

    @Override
    public void setTracklist(Track[] tracklist) {
        mService.setTracklist(tracklist);
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
    public void setCurrentTrack(int trackNumber) {
        mService.setCurrentTrack(trackNumber);
    }

    @Override
    public Track getCurrentTrack() {
        return null;
    }

    @Override
    public void setPlaybackListener(PlaybackListener listener) {
        mService.setPlaybackListener(listener);
    }
}
