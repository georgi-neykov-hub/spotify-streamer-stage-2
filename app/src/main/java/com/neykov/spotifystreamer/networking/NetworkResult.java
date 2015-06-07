package com.neykov.spotifystreamer.networking;

import retrofit.RetrofitError;

/**
 * Simple wrapper object to be used with a {@link android.content.Loader}
 * as it does not offer error handling callbacks.
 * @param <ResponseType>  the type parameter
 */
public class NetworkResult<ResponseType> {
    private ResponseType response;
    private RetrofitError error;

    public NetworkResult(ResponseType response) {
        this.response = response;
    }

    public NetworkResult(RetrofitError error) {
        this.error = error;
    }

    public ResponseType getResponse() {
        return response;
    }

    public RetrofitError getError() {
        return error;
    }

    public boolean isSuccessful(){
        return error == null;
    }
}
