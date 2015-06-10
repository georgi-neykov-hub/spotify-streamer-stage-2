package com.neykov.spotifystreamer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.neykov.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Track;

public class TracksAdapter extends BaseArrayAdapter<Track, TracksAdapter.TrackViewHolder> {

    public TracksAdapter() {
        super();
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        holder.onBind(getItemAt(position));
    }


    public class TrackViewHolder extends RecyclerView.ViewHolder {

        private static final String STRING_EMPTY_ALBUM = "(no album)";

        private ImageView mImageView;
        private TextView mTrackNameTextView;
        private TextView mAlbumTextView;

        public TrackViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.trackImage);
            mTrackNameTextView = (TextView) itemView.findViewById(R.id.trackName);
            mAlbumTextView = (TextView) itemView.findViewById(R.id.albumName);
        }

        protected void onBind(Track track) {
            mTrackNameTextView.setText(track.name);
            setAlbum(track);
            loadImage(track, mImageView);
        }

        private void setAlbum(Track track){
            String album;
            if(track.album != null){
                album = track.album.name;
            }else {
                album = STRING_EMPTY_ALBUM;
            }
            mAlbumTextView.setText(album);
        }

        private void loadImage(Track track, ImageView view) {
            Context appContext = view.getContext().getApplicationContext();
            Picasso picasso = Picasso.with(appContext);
            picasso.cancelRequest(view);

            if(track.album != null && !track.album.images.isEmpty()) {
                String url = track.album.images.get(0).url;
                picasso
                        .load(url)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.ic_av_equalizer)
                        .error(R.drawable.ic_av_equalizer)
                        .into(view);
            }else {
                picasso
                        .load(R.drawable.ic_av_equalizer)
                        .fit()
                        .centerCrop()
                        .noPlaceholder()
                        .into(view);
            }
        }
    }
}
