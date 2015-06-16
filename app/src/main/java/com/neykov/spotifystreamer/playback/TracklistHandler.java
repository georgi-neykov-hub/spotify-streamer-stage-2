package com.neykov.spotifystreamer.playback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Georgi on 15.6.2015 г..
 */
class TracklistHandler {

    private List<Track> mTracklist;
    private Track mCurrentTrack;
    private int mCurrentTrackNumber;

    public TracklistHandler() {
        mTracklist = new ArrayList<>();
    }

    public  boolean hasPrevious() {
        return mCurrentTrackNumber > 0;
    }

    protected boolean hasNext() {
        return mCurrentTrackNumber < (mTracklist.size() - 1);
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
        return mTracklist.size();
    }

    protected Track getCurrentTrack() {
        return mCurrentTrack;
    }

    protected void setTrackslist(Track[] tracks) {
        if (tracks.length == 0) {
            throw new IllegalArgumentException("No tracks provided, length = 0");
        }

        mTracklist.clear();
        mTracklist.addAll(Arrays.asList(tracks));
        mCurrentTrack = getTrack(0);
        mCurrentTrackNumber = 0;
    }

    protected Track getTrack(int number) {
        return mTracklist.get(number);
    }

    public void setCurrentTrack(int number) {
        mCurrentTrack = getTrack(number);
        mCurrentTrackNumber = number;
    }

    public int getCurrentTrackNumber() {
        return mCurrentTrackNumber;
    }
}
