package com.neykov.spotifystreamer.playback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

class PlaylistHandler {

    private List<Track> mPlaylist;
    private Track mCurrentTrack;
    private int mCurrentTrackNumber;

    public PlaylistHandler() {
        mPlaylist = new ArrayList<>();
    }

    public  boolean hasPrevious() {
        return mCurrentTrackNumber > 0;
    }

    protected boolean hasNext() {
        return mCurrentTrackNumber < (mPlaylist.size() - 1);
    }

    protected Track moveToPrevious() {
        if (hasPrevious()) {
            setCurrentTrack(mCurrentTrackNumber - 1);
        }

        return getCurrentTrack();
    }

    protected Track moveToNext() {
        if (hasNext()) {
            setCurrentTrack(mCurrentTrackNumber + 1);
        }

        return getCurrentTrack();
    }

    protected int getTrackCount() {
        return mPlaylist.size();
    }

    protected Track getCurrentTrack() {
        return mCurrentTrack;
    }

    protected void setPlaylist(Track[] tracks) {
        if (tracks.length == 0) {
            throw new IllegalArgumentException("No tracks provided, length = 0");
        }

        mPlaylist.clear();
        mPlaylist.addAll(Arrays.asList(tracks));
        mCurrentTrack = getTrack(0);
        mCurrentTrackNumber = 0;
    }

    protected Track getTrack(int number) {
        return mPlaylist.get(number);
    }

    public void setCurrentTrack(int number) {
        mCurrentTrack = getTrack(number);
        mCurrentTrackNumber = number;
    }

    public int getCurrentTrackNumber() {
        return mCurrentTrackNumber;
    }
}
