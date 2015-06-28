package com.neykov.spotifystreamer.ui;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.neykov.spotifystreamer.R;
import com.neykov.spotifystreamer.playback.PlaybackService;
import com.neykov.spotifystreamer.ui.base.ActionBarConfigurable;
import com.neykov.spotifystreamer.ui.base.ActionbarConfigurator;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

public class MainActivity extends AppCompatActivity implements ActionBarConfigurable, ArtistListFragment.OnArtistSelectedListener, ArtistTopTracksFragment.OnTrackSelectedListener {

    private boolean mDualPaneMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDualPaneMode = getResources().getBoolean(R.bool.has_two_panes);
        setContentView(R.layout.activity_main_layout);
        initializeToolbar();
        repositionFragments();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, ArtistListFragment.newInstance(), ArtistListFragment.TAG)
                    .commit();

            if(PlaybackService.ACTION_SHOW_PLAYBACK_CONTROLS.equals(getIntent().getAction())){
                handleShowPlaybackAction(getIntent());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onApplyConfiguratorOptions(ActionbarConfigurator configurator) {
        boolean hasTitle = configurator.hasScreenTitle();
        getSupportActionBar().setDisplayShowTitleEnabled(hasTitle);

        if (hasTitle) {
            this.setTitle(configurator.getScreenTitle());
            getSupportActionBar().setSubtitle(configurator.getScreenSubtitle());
        }

        if (configurator.hasBackNavigation()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void initializeToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
    }

    private void handleShowPlaybackAction(Intent intent){
        Artist artist = (Artist) intent.getSerializableExtra(PlaybackService.EXTRA_ARTIST);
        if(artist == null){
            throw new IllegalArgumentException("No "+ PlaybackService.EXTRA_ARTIST +" extra provided.");
        }

        //Restore the back stack;
        ArtistTopTracksFragment topTracksFragment = ArtistTopTracksFragment.newInstance(artist);
        showArtistTopTracksFragment(topTracksFragment);
        TracksPlaybackFragment playbackFragment = TracksPlaybackFragment.newInstance();
        showTrackPlayerFragment(playbackFragment);
    }

    @Override
    public void onArtistSelected(Artist artist) {
        ArtistTopTracksFragment fragment = ArtistTopTracksFragment.newInstance(artist);
        showArtistTopTracksFragment(fragment);
    }

    private void showArtistTopTracksFragment(ArtistTopTracksFragment fragment) {
        int containerId = mDualPaneMode? R.id.detail_frame : R.id.content_frame;
        getSupportFragmentManager().beginTransaction()
                .replace(containerId, fragment, ArtistTopTracksFragment.TAG)
                .addToBackStack(ArtistTopTracksFragment.TAG)
                .commit();

        //Execute the transaction synchronously to avoid
        // any multiple instances of the fragment by doing fast-clicks.
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public void onTrackSelected(int trackNumber, Track[] tracks, Artist artist) {
        //Start the playback service, providing the playlist & target artist.

        Intent serviceIntent = new Intent(this, PlaybackService.class)
                .setAction(PlaybackService.ACTION_SET_PLAYLIST)
                .putExtra(PlaybackService.EXTRA_PLAYLIST, tracks)
                .putExtra(PlaybackService.EXTRA_ARTIST, artist)
                .putExtra(PlaybackService.EXTRA_TRACK_TO_PLAY, trackNumber)
                .putExtra(PlaybackService.EXTRA_NOTIFICATION_HANDLER, new ComponentName(this, MainActivity.class));
        this.startService(serviceIntent);

        //Start the playback fragment that visualizes the service controls
        TracksPlaybackFragment fragment = TracksPlaybackFragment.newInstance();
        showTrackPlayerFragment(fragment);
    }

    private void showTrackPlayerFragment(TracksPlaybackFragment fragment) {
        if(mDualPaneMode){
            @SuppressLint("CommitTransaction")
            FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(TracksPlaybackFragment.TAG);
            fragment.show(ft, TracksPlaybackFragment.TAG);
        }else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, TracksPlaybackFragment.TAG)
                    .addToBackStack(TracksPlaybackFragment.TAG)
                    .commit();
        }

        //Execute the transaction synchronously to avoid
        // any multiple instances of the fragment by doing fast-clicks.
        getSupportFragmentManager().executePendingTransactions();
    }

    private void repositionFragments(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.getBackStackEntryCount() == 0){
            return;
        }

        TracksPlaybackFragment playbackFragment = (TracksPlaybackFragment) fragmentManager.findFragmentByTag(TracksPlaybackFragment.TAG);
        if(playbackFragment != null){
            fragmentManager.popBackStackImmediate(TracksPlaybackFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction()
                    .remove(playbackFragment)
                    .detach(playbackFragment)
                    .commit();
            fragmentManager.executePendingTransactions();
        }

        ArtistTopTracksFragment topTracksFragment = (ArtistTopTracksFragment) fragmentManager.findFragmentByTag(ArtistTopTracksFragment.TAG);
        if(topTracksFragment != null){
            fragmentManager.popBackStackImmediate(ArtistTopTracksFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        if (topTracksFragment != null) {
            showArtistTopTracksFragment(topTracksFragment);
        }

        if (playbackFragment != null) {
            TracksPlaybackFragment instance = TracksPlaybackFragment.newInstance();
            showTrackPlayerFragment(instance);
        }
    }
}
