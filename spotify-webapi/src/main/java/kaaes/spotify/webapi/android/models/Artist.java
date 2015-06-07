package kaaes.spotify.webapi.android.models;

import java.io.Serializable;
import java.util.List;

/**
 * <a href="https://developer.spotify.com/web-api/object-model/#artist-object-full">Artist object model</a>
 */
public class Artist extends ArtistSimple  implements Serializable {
    public Followers followers;
    public List<String> genres;
    public List<Image> images;
    public Integer popularity;
}