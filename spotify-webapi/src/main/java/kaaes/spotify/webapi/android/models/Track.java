package kaaes.spotify.webapi.android.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Map;

/**
 * <a href="https://developer.spotify.com/web-api/object-model/#track-object-full">Track object model</a>
 */
public class Track extends TrackSimple implements Serializable {
    public AlbumSimple album;
    public Map<String, String> external_ids;
    public Integer popularity;

}