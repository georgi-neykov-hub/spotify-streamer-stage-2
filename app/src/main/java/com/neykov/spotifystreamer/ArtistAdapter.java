package com.neykov.spotifystreamer;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    private static final String KEY_ITEMS_ARRAY = "ArtistAdapter.Items";

    private static final String STRING_EMPTY_GENRE = "(no genre)";

    private List<Artist> mArtists;

    public ArtistAdapter() {
        mArtists = new ArrayList<>();
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artist, parent, false);
        return new ArtistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder holder, int position) {
        holder.onBind(getItemAt(position));
    }

    @Override
    public int getItemCount() {
        return mArtists.size();
    }

    public Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        Artist[] entries = new Artist[mArtists.size()];
        mArtists.toArray(entries);
        state.putSerializable(KEY_ITEMS_ARRAY, entries);
        return state;
    }

    public void onRestoreInstanceState(Parcelable savedAdapterState) {
        Bundle state = (Bundle) savedAdapterState;
        if (state == null) {
            throw new IllegalArgumentException("Invalid saved state provided.");
        }
        Artist[] items = (Artist[]) state.getSerializable(KEY_ITEMS_ARRAY);
        if (items == null) {
            throw new IllegalArgumentException("Invalid state argument.");
        }

        this.setItems(items);
    }

    public void setItems(List<Artist> artists) {
        this.mArtists.clear();
        mArtists.addAll(artists);
        this.notifyDataSetChanged();
    }

    public void setItems(Artist[] artists) {
        setItems(Arrays.asList(artists));
    }

    public void clearItems() {
        this.mArtists.clear();
        this.notifyDataSetChanged();
    }

    private Artist getItemAt(int position) {
        return mArtists.get(position);
    }

    public class ArtistViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private TextView mArtistTextView;
        private TextView mGenreTextView;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.artistImage);
            mArtistTextView = (TextView) itemView.findViewById(R.id.artistLabel);
            mGenreTextView = (TextView) itemView.findViewById(R.id.genreLabel);
        }

        protected void onBind(Artist artist) {
            mArtistTextView.setText(artist.name);
            loadImage(artist, mImageView);
            setGenres(artist);
        }

        private void setGenres(Artist artist){
            String genreLabel;
            if(!artist.genres.isEmpty()) {
                genreLabel = artist.genres.get(0);
            }else {
                genreLabel = STRING_EMPTY_GENRE;
            }
            mGenreTextView.setText(genreLabel);
        }

        private void loadImage(Artist artist, ImageView view) {
            Context appContext = view.getContext().getApplicationContext();
            Picasso picasso = Picasso.with(appContext);
            picasso.cancelRequest(view);

            if(!artist.images.isEmpty()) {
                String url = artist.images.get(0).url;
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
