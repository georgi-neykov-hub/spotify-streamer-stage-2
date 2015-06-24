package com.neykov.spotifystreamer.playback;

import android.os.Binder;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackBinder extends Binder implements PlaybackInterface {

    private PlaybackInterface mService;

    /*package*/ PlaybackBinder(PlaybackService service) {
        this.mService = service;
    }

    @Override
    public void placeNotificationControls() {
        mService.placeNotificationControls();
    }

    @Override
    public void removeNotificationControls() {
        mService.removeNotificationControls();
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
        mService.playNext();
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
    public Track getCurrentTrack() {
        return mService.getCurrentTrack();
    }

    @Override
    public int getCurrentPosition() {
        return mService.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mService.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mService.isPlaying();
    }

    @Override
    public boolean isActive() {
        return mService.isActive();
    }

    @Override
    public boolean isLoading() {
        return mService.isLoading();
    }

    @Override
    public void setPlaybackListener(PlaybackListener listener) {
        mService.setPlaybackListener(listener);
    }
}
