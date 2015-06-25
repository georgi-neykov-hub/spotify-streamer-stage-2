package com.neykov.spotifystreamer.playback;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.neykov.spotifystreamer.R;
import com.neykov.spotifystreamer.TrackUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

public class PlaybackService extends Service implements PlaybackInterface {

    public static final String ACTION_SET_PLAYLIST = "PlaybackService.SetPlaylist";
    public static final String ACTION_PREVIOUS = "PlaybackService.Previous";
    public static final String ACTION_PLAY = "PlaybackService.Play";
    public static final String ACTION_PAUSE = "PlaybackService.Pause";
    public static final String ACTION_NEXT = "PlaybackService.Next";
    public static final String ACTION_CLOSE = "PlaybackService.Close";
    public static final String ACTION_SHOW_PLAYBACK_CONTROLS = "PlaybackService.ShowPlaybackControls";

    public static final String EXTRA_ARTIST = "PlaybackService.Artist";
    public static final String EXTRA_PLAYLIST = "PlaybackService.Playlist";
    public static final String EXTRA_NOTIFICATION_HANDLER = "PlaybackService.NotificationAction";
    public static final String EXTRA_TRACK_TO_PLAY = "PlaybackService.TrackToPlay";

    protected static final int NOTIFICATION_ID = 0x12345ff;

    private Artist mArtist;
    private ComponentName mNotificationClickHandler;

    private PlaylistHandler mPlaylistHandler;
    private PositionUpdater mPositionUpdater;
    private MediaPlayer mMediaPlayer;
    private PlaybackListener mPlaybackListener;

    protected NotificationCompat.Builder mNotificationBuilder;
    protected NotificationManager mNotificationManager;

    private boolean mPlayerPrepared;
    private boolean mPlayerLoading;
    private boolean mPlayerActive;
    private boolean mRunningInForeground;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlaylistHandler = new PlaylistHandler();
        mPositionUpdater = new PositionUpdater();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        moveToBackgroundState();
        clearPlayer();
        mPositionUpdater.stopUpdating();
        mPositionUpdater.setPositionListener(null);
        mPlaybackListener = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_SET_PLAYLIST:
                handleSetPlaylistAction(intent);
                break;
            case ACTION_PREVIOUS:
                this.playPrevious();
                break;
            case ACTION_PLAY:
                this.play();
                break;
            case ACTION_PAUSE:
               this.pause();
                break;
            case ACTION_NEXT:
                this.playNext();
                break;
            case ACTION_CLOSE:
                this.stopSelf();
            default:
                throw new IllegalArgumentException("Cannot handle action " + intent.getAction());
        }

        return START_NOT_STICKY;
    }

    private void handleSetPlaylistAction(Intent intent) {
        mNotificationClickHandler = intent.getParcelableExtra(EXTRA_NOTIFICATION_HANDLER);
        if(mNotificationClickHandler == null){
            throw new IllegalArgumentException("No "+ EXTRA_NOTIFICATION_HANDLER +" extra provided.");
        }

        mArtist = (Artist) intent.getSerializableExtra(EXTRA_ARTIST);
        if(mArtist == null){
            throw new IllegalArgumentException("No "+ EXTRA_ARTIST +" extra provided.");
        }

        Object[] objectArray = (Object[]) intent.getSerializableExtra(EXTRA_PLAYLIST);
        Track[] playlist = Arrays.copyOf(objectArray, objectArray.length, Track[].class);
        if (playlist == null) {
            throw new IllegalArgumentException("Null tracks array provided.");
        } else if (playlist.length == 0) {
            throw new IllegalArgumentException("Empty tracks array provided.");
        } else {
            resetPlayer();
            mPlaylistHandler.setPlaylist(playlist);
        }

        int trackToPlay = intent.getIntExtra(EXTRA_TRACK_TO_PLAY, 0);
        setCurrentTrack(trackToPlay);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlaybackBinder(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mPlaybackListener = null;
        return super.onUnbind(intent);
    }

    @Override
    public void placeNotificationControls() {
        moveToForegroundState();
    }

    @Override
    public void removeNotificationControls() {
        moveToBackgroundState();
    }

    @Override
    public void play() {
        if (mPlaylistHandler.getTrackCount() == 0) {
            throw new IllegalStateException("The track list is empty.");
        }

        if(mPlayerPrepared){
            mMediaPlayer.start();
            mPositionUpdater.startUpdating(mMediaPlayer);
            notifyPlaybackStateChange(true);
        }else {
            Track current = mPlaylistHandler.getCurrentTrack();
            setPlayback(getUriFromTrack(current));
        }
    }

    @Override
    public void pause() {
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            mPositionUpdater.stopUpdating();
            notifyPlaybackStateChange(false);
        }
    }

    @Override
    public void playNext() {
        if(mPlaylistHandler.hasNext()){
            mPlaylistHandler.moveToNext();
            setCurrentTrack(mPlaylistHandler.getCurrentTrackNumber());
        }
    }

    @Override
    public void playPrevious() {
        if(mPlaylistHandler.hasPrevious()){
            mPlaylistHandler.moveToPrevious();
            setCurrentTrack(mPlaylistHandler.getCurrentTrackNumber());
        }
    }

    @Override
    public void seekToPosition(int positionMillis) {
        if(mPlayerPrepared){
            mPlayerLoading = true;
            notifyLoadStart();
            mMediaPlayer.seekTo(positionMillis);
        }
    }

    private void moveToForegroundState(){
        mRunningInForeground = true;
        mNotificationBuilder = createNotificationBuilder();
        configureNotificationActions(mNotificationBuilder);
        configureNotificationText(mNotificationBuilder, getCurrentTrack());
        loadArtworkInNotification(mNotificationBuilder, getCurrentTrack());
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
        updateNotification(mNotificationBuilder);
    }

    private void moveToBackgroundState(){
        Picasso.with(this.getApplicationContext())
                .cancelTag(NOTIFICATION_ID);
        stopForeground(true);
        mNotificationManager.cancel(NOTIFICATION_ID);

        mNotificationBuilder = null;
        mRunningInForeground = false;
    }

    private NotificationCompat.Builder createNotificationBuilder(){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.ic_av_equalizer)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setAutoCancel(false);

        // Set click intent
        Intent controlsIntent = new Intent();
        controlsIntent.setComponent(mNotificationClickHandler)
                .setAction(ACTION_SHOW_PLAYBACK_CONTROLS)
                .putExtra(EXTRA_ARTIST, mArtist);
        PendingIntent clickIntent = PendingIntent.getActivity(this,
                ACTION_SHOW_PLAYBACK_CONTROLS.hashCode(),
                controlsIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(clickIntent);

        //Set dismiss action
        Intent closeIntent = new Intent(this, PlaybackService.class)
                .setAction(ACTION_CLOSE);
        PendingIntent closeButtonIntent = PendingIntent.getService( getApplicationContext(),
                NOTIFICATION_ID,
                closeIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.MediaStyle mediaStyle = new NotificationCompat.MediaStyle(builder);
        builder.setStyle(mediaStyle);
        mediaStyle.setCancelButtonIntent(closeButtonIntent);
        mediaStyle.setShowActionsInCompactView(0,1,2);

        return builder;
    }

    private void updateNotificationArtwork(NotificationCompat.Builder builder, Bitmap bitmap){
        if(mRunningInForeground){
            builder.setLargeIcon(bitmap);
            updateNotification(builder);
        }
    }

    private void updateNotification(NotificationCompat.Builder builder) {
       startForeground(NOTIFICATION_ID, builder.build());
    }

    private void loadArtworkInNotification(final NotificationCompat.Builder builder, Track track){
        Target loadTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                updateNotificationArtwork(builder, bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (errorDrawable instanceof BitmapDrawable) {
                    updateNotificationArtwork(builder, ((BitmapDrawable) errorDrawable).getBitmap());
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable instanceof BitmapDrawable) {
                    updateNotificationArtwork(builder, ((BitmapDrawable) placeHolderDrawable).getBitmap());
                }
            }
        };

        Picasso instance = Picasso.with(this.getApplicationContext());
        instance.cancelTag(NOTIFICATION_ID);

        if(track.album.images != null && !track.album.images.isEmpty()){
            instance.load(track.album.images.get(0).url)
                    .tag(NOTIFICATION_ID)
                    .resizeDimen(R.dimen.artwork_notification_size, R.dimen.artwork_notification_size)
                    .centerCrop()
                    .placeholder(R.drawable.ic_av_equalizer)
                    .error(R.drawable.ic_av_equalizer)
                    .into(loadTarget);
        }else {
            instance.load(R.drawable.ic_av_equalizer)
                    .into(loadTarget);
        }
    }

    private void configureNotificationText(NotificationCompat.Builder builder, Track track){
        builder.setContentTitle(track.name);
        builder.setContentText(TrackUtils.getArtistString(track));
    }

    private void configureNotificationActions(NotificationCompat.Builder builder) {
        builder.mActions.clear();
        addPrevButtonAction(builder, ACTION_PREVIOUS, R.drawable.ic_playback_previous, getString(R.string.label_previous));
        addPlayPauseAction(builder, this.isPlaying());
        addNextButtonAction(builder, ACTION_NEXT, R.drawable.ic_playback_next, getString(R.string.label_next));
    }

    private void addPrevButtonAction(NotificationCompat.Builder builder, String actionPrevious, int ic_media_previous, String string) {
        Intent previousIntent = new Intent(this, PlaybackService.class)
                .setAction(actionPrevious);
        PendingIntent prevButtonIntent = PendingIntent.getService(
                getApplicationContext(),
                NOTIFICATION_ID,
                previousIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(ic_media_previous, string, prevButtonIntent);
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder, boolean playbackIsRunning) {
        int iconId, labelId;
        String intentAction;
        if (playbackIsRunning) {
            iconId = R.drawable.ic_playback_pause;
            labelId = R.string.label_pause;
            intentAction = ACTION_PAUSE;
        } else {
            iconId =  R.drawable.ic_playback_play;
            labelId = R.string.label_play;
            intentAction = ACTION_PLAY;
        }
        Intent playIntent = new Intent(this, PlaybackService.class);
        playIntent.setAction(intentAction);
        PendingIntent playButtonIntent = PendingIntent.getService(
                getApplicationContext(),
                NOTIFICATION_ID,
                playIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(iconId, getString(labelId), playButtonIntent);
    }

    private void addNextButtonAction(NotificationCompat.Builder builder, String actionNext, int ic_media_next, String string) {
        Intent nextIntent = new Intent(this, PlaybackService.class)
                .setAction(actionNext);
        PendingIntent nextButtonIntent = PendingIntent.getService(
                getApplicationContext(),
                NOTIFICATION_ID,
                nextIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(ic_media_next, string, nextButtonIntent);
    }

    private void setCurrentTrack(int trackToPlay) {
        if (mPlaylistHandler.getTrackCount() == 0) {
            throw new IllegalStateException("The track list is empty.");
        }

        if (!(trackToPlay < mPlaylistHandler.getTrackCount() && trackToPlay >= 0)) {
            throw new IllegalArgumentException("Track to be played is outside the permitted bounds.");
        }

        mPlaylistHandler.setCurrentTrack(trackToPlay);
        Track current = mPlaylistHandler.getCurrentTrack();

        notifyPlaybackStateChange(false);
        notifyTrackChanged(getCurrentTrack());
        resetPlayer();
        setPlayback(getUriFromTrack(current));
    }

    @Override
    public Track getCurrentTrack() {
        return mPlaylistHandler.getCurrentTrack();
    }

    @Override
    public int getCurrentPosition() {
        return mPlayerPrepared? mMediaPlayer.getCurrentPosition(): 0;
    }

    @Override
    public int getDuration() {
        return mPlayerPrepared? mMediaPlayer.getDuration(): 0;
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public boolean isActive() {
        return isPlaying() || mPlayerActive;
    }

    @Override
    public boolean isLoading() {
        return mPlayerLoading;
    }

    @Override
    public void setPlaybackListener(PlaybackListener listener) {
        mPlaybackListener = listener;
        mPositionUpdater.setPositionListener(mPlaybackListener);
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
        mPlayerActive = false;
        mPlayerLoading = false;
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
            mPlayerActive = true;
            mPlayerLoading = true;
            notifyLoadStart();

            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            mPlayerActive = false;
            mPlayerLoading = false;
            notifyError();
        }
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mPlayerPrepared = true;
            mPlayerLoading = false;
            mp.start();
            notifyLoadDone();
            notifyPlaybackStateChange(true);
            mPositionUpdater.setPositionListener(mPlaybackListener);
            mPositionUpdater.startUpdating(mMediaPlayer);
        }
    };


    private void notifyLoadStart() {
        if(mPlaybackListener != null){
            mPlaybackListener.onLoadStart();
        }
    }

    private void notifyError() {
        if(mPlaybackListener != null){
            mPlaybackListener.onError();
        }
    }

    private void notifyLoadDone() {
        if(mPlaybackListener != null){
            mPlaybackListener.onLoadDone();
        }
    }

    private void notifyPlaybackStateChange(boolean running) {
        if(mPlaybackListener != null){
            mPlaybackListener.onPlaybackStateChanged(running);
        }

        if(mRunningInForeground){
            configureNotificationActions(mNotificationBuilder);
            updateNotification(mNotificationBuilder);
        }
    }

    private void notifyTrackChanged(Track newTrack) {
        if(mPlaybackListener != null){
            mPlaybackListener.onTrackChanged(newTrack);
        }

        if(mRunningInForeground){
            configureNotificationText(mNotificationBuilder, newTrack);
            loadArtworkInNotification(mNotificationBuilder, newTrack);
            updateNotification(mNotificationBuilder);
        }
    }

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mPlayerPrepared = false;
            mPlayerActive = false;
            mPlayerLoading = false;
            mPositionUpdater.stopUpdating();
            notifyError();
            notifyPlaybackStateChange(false);

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
            mPlayerLoading = false;
            notifyLoadDone();
        }
    };
}
