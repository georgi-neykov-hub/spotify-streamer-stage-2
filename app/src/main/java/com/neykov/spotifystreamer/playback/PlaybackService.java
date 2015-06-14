package com.neykov.spotifystreamer.playback;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackService extends Service implements PlaybackInterface {

    public static final String ACTION_SET_TRACKS = "PlaybackService.SetTracks";
    public static final String ACTION_PREVIOUS = "PlaybackService.Previous";
    public static final String ACTION_PLAY = "PlaybackService.Play";
    public static final String ACTION_PAUSE = "PlaybackService.Pause";
    public static final String ACTION_NEXT = "PlaybackService.Next";

    public static final String EXTRA_TRACKLIST = "PlaybackService.Tracklist";
    public static final String EXTRA_TRACK_TO_PLAY = "PlaybackService.TrackToPlay";

    private TracklistHandler mTracklistHandler;
    private MediaPlayer mMediaPlayer;
    private PlaybackListener mPlaybackListener;

    private boolean mPlayerPrepared;

    @Override
    public void onCreate() {
        super.onCreate();
        mTracklistHandler = new TracklistHandler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        clearPlayer();
        mPlaybackListener = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_SET_TRACKS:
                handleSetTrackslistAction(intent);
                break;
            case ACTION_PREVIOUS:
                handlePreviousAction(intent);
                break;
            case ACTION_PLAY:
                handleNextAction(intent);
                break;
            case ACTION_PAUSE:
                handlePauseAction(intent);
                break;
            case ACTION_NEXT:
                handleNextAction(intent);
                break;
            default:
                throw new IllegalArgumentException("Unknown action " + intent.getAction());
        }

        return START_NOT_STICKY;
    }

    private void handlePauseAction(Intent intent) {
        this.pause();
    }

    private void handleNextAction(Intent intent) {
        this.playNext();
    }

    private void handlePreviousAction(Intent intent) {
        this.playPrevious();
    }

    private void handleSetTrackslistAction(Intent intent) {
        Track[] tracklist = (Track[]) intent.getSerializableExtra(EXTRA_TRACKLIST);
        setTracklist(tracklist);

        int trackToPlay = intent.getIntExtra(EXTRA_TRACK_TO_PLAY, -1);
        if (trackToPlay != -1) {
            setCurrentTrack(trackToPlay);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlaybackBinder(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void setTracklist(Track[] tracklist) {
        if (tracklist == null) {
            throw new IllegalArgumentException("Null tracks array provided.");
        } else if (tracklist.length == 0) {
            throw new IllegalArgumentException("Empty tracks array provided.");
        } else {
            resetPlayer();
            mTracklistHandler.setTrackslist(tracklist);
        }
    }

    @Override
    public void play() {
        if (mTracklistHandler.getTrackCount() == 0) {
            throw new IllegalStateException("The track list is empty.");
        }

        if(mPlayerPrepared){
            mMediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
    }

    @Override
    public void playNext() {
        if(mTracklistHandler.hasNext()){
            mTracklistHandler.moveToNext();
            play();
        }
    }

    @Override
    public void playPrevious() {

    }

    @Override
    public void setCurrentTrack(int trackToPlay) {
        if (mTracklistHandler.getTrackCount() == 0) {
            throw new IllegalStateException("The track list is empty.");
        }

        if (!(trackToPlay < mTracklistHandler.getTrackCount() && trackToPlay >= 0)) {
            throw new IllegalArgumentException("Track to be played is outside the permitted bounds.");
        }

        mTracklistHandler.setCurrentTrack(trackToPlay);
        resetPlayer();
        Track current = mTracklistHandler.getCurrentTrack();
        setPlayback(getUriFromTrack(current));
    }

    @Override
    public Track getCurrentTrack() {
        return null;
    }

    @Override
    public void setPlaybackListener(PlaybackListener listener) {
        mPlaybackListener = listener;
    }

    private void resetPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
        } else if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.reset();
        mPlayerPrepared = false;
    }

    private void clearPlayer() {
        if (mMediaPlayer != null) {
            mPlayerPrepared = false;
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private Uri getUriFromTrack(Track target) {
        Uri uri = Uri.parse(target.uri);
        return uri;
    }

    private void setPlayback(Uri uri) {
        resetPlayer();
        try {
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            mPlaybackListener.onError();
        }
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mPlayerPrepared = true;
            mp.start();
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mPlayerPrepared = false;
            if(mPlaybackListener != null){
                mPlaybackListener.onError();
            }
            return true;
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            playNext();
        }
    };

    private static class TracklistHandler {

        private List<Track> mTracklist;
        private Track mCurrentTrack;
        private int mCurrentTrackNumber;

        protected TracklistHandler(){
            mTracklist = new ArrayList<>();
        }

        protected boolean hasPrevious(){
            return mCurrentTrackNumber > 0;
        }

        protected boolean hasNext() {
            return mCurrentTrackNumber < (mTracklist.size() - 1);
        }

        protected Track providePrevious() {
            if (hasPrevious()) {
                setCurrentTrack(mCurrentTrackNumber - 1);
            }

            return getCurrentTrack();
        }

        protected Track moveToNext(){
            if (hasNext()) {
                setCurrentTrack(mCurrentTrackNumber + 1);
            }

            return getCurrentTrack();
        }

        protected int getTrackCount(){
            return mTracklist.size();
        }

        protected Track getCurrentTrack() {
            return mCurrentTrack;
        }

        protected void setTrackslist(Track[] tracks){
            if(tracks.length == 0){
                throw new IllegalArgumentException("No tracks provided, length = 0");
            }

            mTracklist.clear();
            mTracklist.addAll(Arrays.asList(tracks));
            mCurrentTrack = getTrack(0);
        }

        protected Track getTrack(int number) {
            return mTracklist.get(number);
        }

        private void setCurrentTrack(int number){
            mCurrentTrack = getTrack(number);
            mCurrentTrackNumber = number;
        }
    }
}
