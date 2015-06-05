package com.neykov.spotifystreamer;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistListFragment extends Fragment {

    private RecyclerView mArtistsRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_artist_list, container, false);
        intializeViewReferences(rootView);

        return rootView;
    }

    private void intializeViewReferences(View rootView) {
        mArtistsRecyclerView = (RecyclerView) rootView.findViewById(R.id.artistList);
        mArtistsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mArtistsRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}
