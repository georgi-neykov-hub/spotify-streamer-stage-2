package com.neykov.spotifystreamer;

import android.app.Application;

import com.neykov.spotifystreamer.networking.Constants;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.SpotifyService;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class SpotifyStreamerApplication extends Application{

    private static SpotifyStreamerApplication sInstance;

    public static SpotifyStreamerApplication getInstance(){
        return sInstance;
    }

    private SpotifyService mSpotifyAPIService;
    private OkHttpClient mHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        setupPicasso(getOkHttpClient());
        sInstance = this;
    }

    public SpotifyService getSpotifyAPIService(){
        if(mSpotifyAPIService == null){
            mSpotifyAPIService = createSpotifyService(getOkHttpClient());
        }

        return mSpotifyAPIService;
    }

    public OkHttpClient getOkHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = createHttpClient();
        }

        return mHttpClient;
    }

    private OkHttpClient createHttpClient(){
        OkHttpClient client = new OkHttpClient();

        //Set Cache size and Timeout limits
        Cache cache = new Cache(getCacheDir(), Constants.OKHTTP_CACHE_SIZE_BYTES);
        client.setCache(cache);

        client.setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        client.setReadTimeout(Constants.HTTP_READOUT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        client.setWriteTimeout(Constants.HTTP_WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        return client;
    }

    private void setupPicasso(OkHttpClient client) {
        Picasso instance = new Picasso.Builder(this)
                .downloader(new OkHttpDownloader(client))
                .build();

        Picasso.setSingletonInstance(instance);
    }

    private SpotifyService createSpotifyService(OkHttpClient okHttpClient){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.SPOTIFY_WEB_API_ENDPOINT)
                .setClient(new OkClient(okHttpClient))
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .build();
        return restAdapter.create(SpotifyService.class);
    }
}
