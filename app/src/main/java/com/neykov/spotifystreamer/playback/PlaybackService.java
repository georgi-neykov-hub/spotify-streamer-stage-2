package com.neykov.spotifystreamer.playback;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import java.io.IOException;

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
    private PositionUpdater mPositionUpdater;
    private MediaPlayer mMediaPlayer;
    private PlaybackListener mPlaybackListener;

    private boolean mPlayerPrepared;

    @Override
    public void onCreate() {
        super.onCreate();
        mTracklistHandler = new TracklistHandler();
        mPositionUpdater = new PositionUpdater();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        clearPlayer();
        mPositionUpdater.stopUpdating();
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
    public void placeGlobalControls() {

    }

    @Override
    public void play() {
        if (mTracklistHandler.getTrackCount() == 0) {
            throw new IllegalStateException("The track list is empty.");
        }

        if(mPlayerPrepared){
            mMediaPlayer.start();
            mPositionUpdater.startUpdating(mMediaPlayer, mPlaybackListener);
        }else {
            Track current = mTracklistHandler.getCurrentTrack();
            setPlayback(getUriFromTrack(current));
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
            Track nextTrack = mTracklistHandler.moveToNext();
            setPlayback(getUriFromTrack(nextTrack));
        }
    }

    @Override
    public void playPrevious() {
        if(mTracklistHandler.hasPrevious()){
            Track nextTrack = mTracklistHandler.moveToPrevious();
            setPlayback(getUriFromTrack(nextTrack));
        }
    }

    @Override
    public void seekToPosition(int positionMillis) {
        if(mPlayerPrepared){
            mMediaPlayer.seekTo(positionMillis);
            if(mPlaybackListener != null){
                mPlaybackListener.onLoadStart();
            }
        }
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
        return mTracklistHandler.getCurrentTrack();
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
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
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
        } else if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mPositionUpdater.stopUpdating();
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
        return Uri.parse(target.preview_url);
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
            mPositionUpdater.startUpdating(mMediaPlayer, mPlaybackListener);
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mPlayerPrepared = false;
            mPositionUpdater.stopUpdating();
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

    private MediaPlayer.OnSeekCompleteListener mSeekListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if(mPlaybackListener != null){
                mPlaybackListener.onLoadDone();
            }
        }
    };

}
