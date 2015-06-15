package com.neykov.spotifystreamer.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.neykov.spotifystreamer.R;
import com.neykov.spotifystreamer.playback.PlaybackInterface;
import com.neykov.spotifystreamer.playback.PlaybackListener;
import com.neykov.spotifystreamer.playback.PlaybackService;
import com.neykov.spotifystreamer.ui.base.ActionbarConfigurator;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.models.Track;

public class TracksPlaybackFragment extends Fragment implements ActionbarConfigurator, PlaybackListener {

    private static final String ARG_TRACKS = "TracksPlaybackFragment.Tracks";
    private static final String ARG_START_TRACK = "TracksPlaybackFragment.StartTrack";
    public static final String TAG = TracksPlaybackFragment.class.getSimpleName();
    public static final String TIME_INTERVAL_STRING_FORMAT = "%02d:%02d";

    public static TracksPlaybackFragment newInstance(Track[] tracks, int startTrackNumber){
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRACKS, tracks);
        args.putInt(ARG_START_TRACK, startTrackNumber);
        TracksPlaybackFragment instance = new TracksPlaybackFragment();
        instance.setArguments(args);
        return instance;
    }

    private PlaybackInterface mPlaybackInterface;
    private Track[] mTracks;

    private View mPreviousButton;
    private View mPlayPauseButton;
    private View mNextButton;
    private SeekBar mSeekBar;
    private TextView mElapsedTextView;
    private TextView mDurationTextView;
    private ImageView mAlbumArtView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracks = (Track[]) getArguments().getSerializable(ARG_TRACKS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_track_player, container, false);
        initializeViewReferences(rootView);
        setEventListeners();
        togglePlaybackControls(false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        connectToService();
    }

    @Override
    public void onStop() {
        super.onStop();
        detachFromService();
    }

    private void initializeViewReferences(View rootView){
        mAlbumArtView = (ImageView) rootView.findViewById(R.id.albumArtView);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        mPreviousButton = rootView.findViewById(R.id.previousButton);
        mPlayPauseButton = rootView.findViewById(R.id.playButton);
        mNextButton = rootView.findViewById(R.id.nextButton);
        mElapsedTextView = (TextView) rootView.findViewById(R.id.timeElapsed);
        mDurationTextView = (TextView) rootView.findViewById(R.id.timeTotal);
    }

    private void setEventListeners(){
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPlaybackInterface != null){
                    mPlaybackInterface.playPrevious();
                }
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPlaybackInterface != null){
                    mPlaybackInterface.playNext();
                }
            }
        });
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlaybackInterface != null) {
                    mPlaybackInterface.play();
                }
            }
        });
        mSeekBar.setOnSeekBarChangeListener(mSeеkChangeListener);
    }

    private void togglePlaybackControls(boolean enabled){
        mPlayPauseButton.setEnabled(enabled);
        mPreviousButton.setEnabled(enabled);
        mNextButton.setEnabled(enabled);
        toggleSeekBar(enabled);
    }

    private void toggleSeekBar(boolean available){
        mSeekBar.setEnabled(available);
    }

    private String getLabelForTime(int millis) {
        return String.format(TIME_INTERVAL_STRING_FORMAT,
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private void detachFromService() {
        togglePlaybackControls(false);

        if(mPlaybackInterface != null && mPlaybackInterface.isPlaying()){
            mPlaybackInterface.placeGlobalControls();
            getActivity().unbindService(mConnection);
        }else{
            Intent serviceIntent = new Intent(getActivity(), PlaybackService.class);
            getActivity().stopService(serviceIntent);
        }
    }

    private void connectToService() {
        togglePlaybackControls(false);

        Intent serviceIntent = new Intent(getActivity(), PlaybackService.class)
                .setAction(PlaybackService.ACTION_SET_TRACKS)
                .putExtra(PlaybackService.EXTRA_TRACKLIST, mTracks);
        getActivity().startService(serviceIntent);

        Intent bindIntent = new Intent(getActivity(), PlaybackService.class);
        getActivity().bindService(bindIntent, mConnection, 0);
    }

    private void loadTrackImage(Track track){
        Picasso instance = Picasso.with(getActivity().getApplicationContext());
        instance.cancelRequest(mAlbumArtView);

        if(track.album.images != null && track.album.images.isEmpty()){
            instance.load(track.album.images.get(0).url)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.ic_av_equalizer)
                    .error( R.drawable.ic_av_equalizer)
                    .into(mAlbumArtView);
        }else {
            instance.load(R.drawable.ic_av_equalizer)
                    .into(mAlbumArtView);
        }
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

    @Override
    public void onLoadStart() {
        toggleSeekBar(false);
    }

    @Override
    public void onPositionUpdate(int currentPosMs, int totalLengthMs) {
        if(currentPosMs != mSeekBar.getProgress()){
            mElapsedTextView.setText(getLabelForTime(currentPosMs));
            mSeekBar.setProgress(currentPosMs);
        }

        if(totalLengthMs != mSeekBar.getMax()) {
            mDurationTextView.setText(getLabelForTime(totalLengthMs));
            mSeekBar.setMax(totalLengthMs);
        }

        toggleSeekBar(true);
    }

    @Override
    public void onLoadDone() {
        toggleSeekBar(true);
    }

    @Override
    public void onError() {
        toggleSeekBar(false);

    }

    private final SeekBar.OnSeekBarChangeListener mSeеkChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(mPlaybackInterface != null){
                mPlaybackInterface.seekToPosition(seekBar.getProgress());
            }

            mElapsedTextView.setText(getLabelForTime(seekBar.getProgress()));
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlaybackInterface = (PlaybackInterface) service;
            mPlaybackInterface.setPlaybackListener(TracksPlaybackFragment.this);
            if(getView() != null){
                togglePlaybackControls(true);
                loadTrackImage(mPlaybackInterface.getCurrentTrack());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(getView() != null){
                togglePlaybackControls(false);
            }
            mPlaybackInterface = null;
        }
    };
}
