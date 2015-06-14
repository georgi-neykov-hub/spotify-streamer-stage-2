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

import kaaes.spotify.webapi.android.models.Artist;

public class ArtistAdapter extends BaseArrayAdapter<Artist, ArtistAdapter.ArtistViewHolder> {

    public ArtistAdapter() {
        super();
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

    public class ArtistViewHolder extends RecyclerView.ViewHolder {

        private static final String STRING_EMPTY_GENRE = "(no genre)";

        private ImageView mImageView;
        private TextView mArtistTextView;
        private TextView mGenreTextView;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.artistImage);
            mArtistTextView = (TextView) itemView.findViewById(R.id.artistLabel);
            mGenreTextView = (TextView) itemView.findViewById(R.id.genreLabel);
            itemView.findViewById(R.id.itemView).setOnClickListener(mItemClickListener);
        }

        protected void onBind(Artist artist) {
            mArtistTextView.setText(artist.name);
            loadImage(artist, mImageView);
            setGenres(artist);
        }

        private void setGenres(Artist artist) {
            String genreLabel;
            if (!artist.genres.isEmpty()) {
                genreLabel = artist.genres.get(0);
            } else {
                genreLabel = STRING_EMPTY_GENRE;
            }
            mGenreTextView.setText(genreLabel);
        }

        private void loadImage(Artist artist, ImageView view) {
            Context appContext = view.getContext().getApplicationContext();
            Picasso picasso = Picasso.with(appContext);
            picasso.cancelRequest(view);

            if (!artist.images.isEmpty()) {
                String url = artist.images.get(0).url;
                picasso
                        .load(url)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.ic_av_equalizer)
                        .error(R.drawable.ic_av_equalizer)
                        .into(view);
            } else {
                picasso
                        .load(R.drawable.ic_av_equalizer)
                        .fit()
                        .centerCrop()
                        .noPlaceholder()
                        .into(view);
            }
        }

        private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getOnItemSelectedListener() != null){
                    int position = getAdapterPosition();
                    Artist artist = getItemAt(position);
                    getOnItemSelectedListener().onItemSelected(position, artist);
                }
            }
        };
    }
}
