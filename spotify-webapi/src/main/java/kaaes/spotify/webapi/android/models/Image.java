package kaaes.spotify.webapi.android.models;

import java.io.Serializable;

/**
 * <a href="https://developer.spotify.com/web-api/object-model/#image-object">Image object model</a>
 */
public class Image implements Serializable {
    public Integer width;
    public Integer height;
    public String url;
}
