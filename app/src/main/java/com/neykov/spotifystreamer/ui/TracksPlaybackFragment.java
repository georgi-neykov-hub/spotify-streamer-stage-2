package com.neykov.spotifystreamer.ui;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neykov.spotifystreamer.R;
import com.neykov.spotifystreamer.playback.PlaybackInterface;
import com.neykov.spotifystreamer.playback.PlaybackService;
import com.neykov.spotifystreamer.ui.base.ActionbarConfigurator;

import kaaes.spotify.webapi.android.models.Track;

public class TracksPlaybackFragment extends Fragment implements ActionbarConfigurator {

    private static final String ARG_TRACKS = "TracksPlaybackFragment.Tracks";
    private static final String ARG_START_TRACK = "TracksPlaybackFragment.StartTrack";
    public static final String TAG = TracksPlaybackFragment.class.getSimpleName();

    public static TracksPlaybackFragment newInstance(Track[] tracks, int startTrackNumber){
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRACKS, tracks);
        args.putInt(ARG_START_TRACK, startTrackNumber);
        TracksPlaybackFragment instance = new TracksPlaybackFragment();
        instance.setArguments(args);
        return instance;
    }

    private ServiceConnection mConnection;
    private PlaybackInterface.PlaybackListener mPlaybackListener;
    private Track[] mTracks;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracks = (Track[]) getArguments().getSerializable(ARG_TRACKS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_track_player, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        startPlaybackService();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void startPlaybackService() {
        Intent serviceIntent = new Intent(getActivity(), PlaybackService.class)
                .setAction(PlaybackService.ACTION_SET_TRACKS)
                .putExtra(PlaybackService.EXTRA_TRACKLIST, mTracks);
        getActivity().startService(serviceIntent);
    }

    @Override
    public boolean hasBackNavigation() {
        return true;
    }

    @Override
    public boolean hasScreenTitle() {
        return false;
    }

    @Override
    public String getScreenTitle() {
        return null;
    }

    @Override
    public String getScreenSubtitle() {
        return null;
    }
}
