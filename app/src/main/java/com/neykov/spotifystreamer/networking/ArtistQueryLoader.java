package com.neykov.spotifystreamer.networking;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.neykov.spotifystreamer.networking.NetworkResult;

import java.util.LinkedHashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

public class ArtistQueryLoader extends AsyncTaskLoader<NetworkResult<ArtistsPager>> {

    private String mQuery;
    private SpotifyService mApiService;

    public ArtistQueryLoader(Context context, SpotifyService apiService, String query) {
        super(context);
        this.mApiService = apiService;
        this.mQuery = query;
    }

    @Override
    public NetworkResult<ArtistsPager> loadInBackground() {
        NetworkResult<ArtistsPager> result;
        try {
            Map<String, Object> options = new LinkedHashMap<>();
            options.put("limit", 50);
            ArtistsPager artistsPager = mApiService.searchArtists(mQuery, options);
            result = new NetworkResult<>(artistsPager);
        } catch (RetrofitError error) {
            result = new NetworkResult<>(error);
        }

        return result;
    }
}
