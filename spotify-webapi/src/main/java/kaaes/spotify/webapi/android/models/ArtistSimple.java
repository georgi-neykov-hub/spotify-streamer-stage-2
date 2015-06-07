package kaaes.spotify.webapi.android.models;

import java.io.Serializable;
import java.util.Map;

public class ArtistSimple implements Serializable{
    public Map<String, String> external_urls;
    public String href;
    public String id;
    public String name;
    public String type;
    public String uri;
}
