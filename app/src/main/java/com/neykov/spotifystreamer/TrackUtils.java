package com.neykov.spotifystreamer;

import android.support.annotation.NonNull;

import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

public class TrackUtils {

    @NonNull
    public static String getArtistString(Track currentTrack) {
        StringBuilder nameBuilder = new StringBuilder();
        List<ArtistSimple> artists = currentTrack.artists;
        if(artists.size() == 1){
            nameBuilder.append(artists.get(0).name);
        }else{
            int currentArtist = 0;
            while (currentArtist < artists.size()){
                nameBuilder.append(artists.get(currentArtist).name);
                if(currentArtist < artists.size() - 1) {
                    nameBuilder.append(", ");
                }
                currentArtist++;
            }
        }
        return nameBuilder.toString();
    }
}
