package com.neykov.spotifystreamer.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.neykov.spotifystreamer.R;
import com.neykov.spotifystreamer.TrackUtils;
import com.neykov.spotifystreamer.ViewUtils;
import com.neykov.spotifystreamer.playback.PlaybackInterface;
import com.neykov.spotifystreamer.playback.PlaybackListener;
import com.neykov.spotifystreamer.playback.PlaybackService;
import com.neykov.spotifystreamer.ui.base.ActionBarConfigurable;
import com.neykov.spotifystreamer.ui.base.ActionbarConfigurator;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.models.Track;

public class TracksPlaybackFragment extends DialogFragment implements ActionbarConfigurator, PlaybackListener {

    public static final String TAG = TracksPlaybackFragment.class.getSimpleName();
    public static final String TIME_INTERVAL_STRING_FORMAT = "%02d:%02d";

    public static TracksPlaybackFragment newInstance() {
        TracksPlaybackFragment instance = new TracksPlaybackFragment();
        return instance;
    }

    private PlaybackInterface mPlaybackInterface;
    private ShareActionProvider mShareActionProvider;

    private ImageButton mPreviousButton;
    private ImageButton mPlayPauseButton;
    private ImageButton mNextButton;
    private SeekBar mSeekBar;
    private TextView mElapsedTextView;
    private TextView mDurationTextView;
    private TextView mTrackTitleView;
    private TextView mArtistNameView;
    private ImageView mAlbumArtView;
    private View mLoadingView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        if (activity instanceof ActionBarConfigurable && !getShowsDialog()) {
            ((ActionBarConfigurable) activity).onApplyConfiguratorOptions(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(!getShowsDialog());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new AppCompatDialog(getActivity(), getTheme());
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layoutId = getShowsDialog() ? R.layout.layout_track_player_dialog : R.layout.track_player_layout;
        View rootView = inflater.inflate(layoutId, container, false);
        initializeViewReferences(rootView);
        setEventListeners(rootView);
        togglePlaybackControls(false);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_track_player, menu);
        MenuItem item = menu.findItem(R.id.action_share_track);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
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

    private void initializeViewReferences(View rootView) {
        mAlbumArtView = (ImageView) rootView.findViewById(R.id.albumArtView);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        mPreviousButton = (ImageButton) rootView.findViewById(R.id.previousButton);
        mPlayPauseButton = (ImageButton) rootView.findViewById(R.id.playButton);
        mNextButton = (ImageButton) rootView.findViewById(R.id.nextButton);
        mElapsedTextView = (TextView) rootView.findViewById(R.id.timeElapsed);
        mDurationTextView = (TextView) rootView.findViewById(R.id.timeTotal);
        mLoadingView = rootView.findViewById(R.id.loadingView);
        mTrackTitleView = (TextView) rootView.findViewById(R.id.trackTitle);
        mArtistNameView = (TextView) rootView.findViewById(R.id.artistName);
    }

    private void setEventListeners(View rootView) {
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlaybackInterface != null) {
                    mPlaybackInterface.playPrevious();
                }
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlaybackInterface != null) {
                    mPlaybackInterface.playNext();
                }
            }
        });
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlaybackInterface != null) {
                    if (mPlaybackInterface.isPlaying()) {
                        mPlaybackInterface.pause();
                    } else {
                        mPlaybackInterface.play();
                    }
                }
            }
        });
        mSeekBar.setOnSeekBarChangeListener(mSeеkChangeListener);

        if (getShowsDialog()) {
            View shareButton = rootView.findViewById(R.id.action_share_track);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPlaybackInterface != null) {
                        Track currentTrack = mPlaybackInterface.getCurrentTrack();
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, currentTrack.href);
                        Intent chooserIntent = Intent.createChooser(sharingIntent, getString(R.string.label_share_via));
                        startActivity(chooserIntent);
                    }
                }
            });
        }
    }

    private void togglePlaybackControls(boolean enabled) {
        mPlayPauseButton.setEnabled(enabled);
        mPreviousButton.setEnabled(enabled);
        mNextButton.setEnabled(enabled);
        toggleSeekBar(enabled);
    }

    private void toggleLoadingView(boolean enabled) {
        int visibility = enabled ? View.VISIBLE : View.INVISIBLE;
        mLoadingView.setVisibility(visibility);
    }

    private void toggleSeekBar(boolean available) {
        mSeekBar.setEnabled(available);
    }

    private void togglePlayButtonState(boolean trackIsPlaying) {
        int iconResource = trackIsPlaying ? R.drawable.ic_playback_pause : R.drawable.ic_playback_play;
        Resources.Theme theme = getActivity().getTheme();
        Drawable iconDrawable = ViewUtils.getDrawable(iconResource, getResources(), theme);
        mPlayPauseButton.setImageDrawable(iconDrawable);
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
        if (mPlaybackInterface != null) {
            mPlaybackInterface.setPlaybackListener(null);
            if (mPlaybackInterface.isPlaying() || mPlaybackInterface.isLoading()) {
                mPlaybackInterface.placeNotificationControls();
            } else {
                Intent serviceIntent = new Intent(getActivity(), PlaybackService.class);
                getActivity().stopService(serviceIntent);
            }
        }
        getActivity().unbindService(mConnection);
    }

    private void connectToService() {
        togglePlaybackControls(false);
        Intent bindIntent = new Intent(getActivity(), PlaybackService.class);
        getActivity().bindService(bindIntent, mConnection, 0);
    }

    private void loadTrackImage(Track track) {
        Picasso instance = Picasso.with(getActivity().getApplicationContext());
        instance.cancelRequest(mAlbumArtView);

        if (track.album.images != null && !track.album.images.isEmpty()) {
            instance.load(track.album.images.get(0).url)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_av_equalizer)
                    .error(R.drawable.ic_av_equalizer)
                    .into(mAlbumArtView);
        } else {
            instance.load(R.drawable.ic_av_equalizer)
                    .into(mAlbumArtView);
        }
    }

    private void setShareIntent(Track track) {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent()
                    .setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, track.href)
                    .setType("text/plain");
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean hasBackNavigation() {
        return true;
    }

    @Override
    public boolean hasScreenTitle() {
        return true;
    }

    @Override
    public String getScreenTitle() {
        return getString(R.string.title_tracks);
    }

    @Override
    public String getScreenSubtitle() {
        return null;
    }

    @Override
    public void onLoadStart() {
        toggleSeekBar(false);
        toggleLoadingView(true);
    }

    @Override
    public void onPositionUpdate(int currentPosMs, int totalLengthMs) {
        setElapsedTime(currentPosMs);
        setTotalDuration(totalLengthMs);
        toggleSeekBar(true);
    }

    private void setTotalDuration(int durationMillis) {
        if (durationMillis != mSeekBar.getMax()) {
            mDurationTextView.setText(getLabelForTime(durationMillis));
            mSeekBar.setMax(durationMillis);
        }
    }

    private void setElapsedTime(int elapsedTimeMillis) {
        if (elapsedTimeMillis != mSeekBar.getProgress()) {
            mElapsedTextView.setText(getLabelForTime(elapsedTimeMillis));
            mSeekBar.setProgress(elapsedTimeMillis);
        }
    }

    @Override
    public void onLoadDone() {
        toggleSeekBar(true);
        toggleLoadingView(false);
        togglePlaybackControls(true);
    }

    @Override
    public void onError() {
        toggleSeekBar(false);
        toggleLoadingView(false);
    }

    @Override
    public void onTrackChanged(Track currentTrack) {
        loadTrackImage(currentTrack);
        setTrackNameLabels(currentTrack);
        setElapsedTime(0);
        setTotalDuration(0);
        toggleSeekBar(false);
        setShareIntent(currentTrack);
    }

    @Override
    public void onPlaybackStateChanged(boolean running) {
        togglePlayButtonState(running);
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
            if (mPlaybackInterface != null) {
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
            if (getView() != null) {
                Track currentTrack = mPlaybackInterface.getCurrentTrack();
                loadTrackImage(currentTrack);
                setTrackNameLabels(currentTrack);
                togglePlaybackControls(!mPlaybackInterface.isLoading());
                togglePlayButtonState(mPlaybackInterface.isPlaying());
                toggleLoadingView(mPlaybackInterface.isLoading());
                setElapsedTime(mPlaybackInterface.getCurrentPosition());
                setTotalDuration(mPlaybackInterface.getDuration());
                setShareIntent(currentTrack);
            }

            mPlaybackInterface.removeNotificationControls();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (getView() != null) {
                togglePlaybackControls(false);
            }
            mPlaybackInterface = null;
        }
    };

    private void setTrackNameLabels(Track currentTrack) {
        mArtistNameView.setText(TrackUtils.getArtistString(currentTrack));
        mTrackTitleView.setText(currentTrack.name);
    }
}
