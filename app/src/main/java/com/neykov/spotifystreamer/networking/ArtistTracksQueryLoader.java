package com.neykov.spotifystreamer.networking;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.LinkedHashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

public class ArtistTracksQueryLoader extends AsyncTaskLoader<NetworkResult<Tracks>> {

    private Artist mArtist;
    private SpotifyService mApiService;

    public ArtistTracksQueryLoader(Context context, SpotifyService apiService, Artist artist) {
        super(context);
        this.mApiService = apiService;
        this.mArtist = artist;
    }

    @Override
    public NetworkResult<Tracks> loadInBackground() {
        NetworkResult<Tracks> result;
        try {
            Map<String, Object> options = new LinkedHashMap<>();
            options.put("limit", 10);
            Tracks topTenTracks = mApiService.getArtistTopTrack(mArtist.id, options);
            result = new NetworkResult<>(topTenTracks);
        } catch (RetrofitError error) {
            result = new NetworkResult<>(error);
        }

        return result;
    }
}
