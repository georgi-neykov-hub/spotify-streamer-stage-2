package com.neykov.spotifystreamer.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.neykov.spotifystreamer.R;
import com.neykov.spotifystreamer.SpotifyStreamerApplication;
import com.neykov.spotifystreamer.adapter.ArtistAdapter;
import com.neykov.spotifystreamer.networking.ArtistQueryLoader;
import com.neykov.spotifystreamer.networking.NetworkResult;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistTopTracksFragment extends Fragment {

    public static String TAG = ArtistTopTracksFragment.class.getSimpleName();

    private static final String KEY_LAYOUT_MANAGER_STATE = "ArtistListFragment.LayoutManagerState";
    private static final String KEY_ADAPTER_STATE = "ArtistListFragment.ArtistAdapterState";

    private static final String ARG_QUERY_STRING = "ArtistListFragment.Query";
    private static final int QUERY_LOADER_ID = 0x1234;

    public static ArtistTopTracksFragment newInstance() {
        return new ArtistTopTracksFragment();
    }

    private SearchView mSearchView;
    private RecyclerView mArtistsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArtistAdapter mArtistAdapter;

    private SpotifyService mApiService;
    private Loader<NetworkResult<Tracks>> MqueryLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApiService = SpotifyStreamerApplication.getInstance().getSpotifyAPIService();
        mArtistAdapter = new ArtistAdapter();

        if (savedInstanceState != null) {
            Parcelable arrayData = savedInstanceState.getParcelable(KEY_ADAPTER_STATE);
            mArtistAdapter.onRestoreInstanceState(arrayData);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_LAYOUT_MANAGER_STATE, mLayoutManager.onSaveInstanceState());
        outState.putParcelable(KEY_ADAPTER_STATE, mArtistAdapter.onSaveInstanceState());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_artist_list, container, false);
        intializeViewReferences(rootView);
        configureRecyclerView(savedInstanceState);
        setEventListeners();
        return rootView;
    }

    private void intializeViewReferences(View rootView) {
        mArtistsRecyclerView = (RecyclerView) rootView.findViewById(R.id.artistList);
        mSearchView = (SearchView) rootView.findViewById(R.id.searchView);
    }

    private void configureRecyclerView(Bundle savedState) {
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        if (savedState != null) {
            Parcelable state = savedState.getParcelable(KEY_LAYOUT_MANAGER_STATE);
            mLayoutManager.onRestoreInstanceState(state);
        }

        mArtistsRecyclerView.setLayoutManager(mLayoutManager);
        mArtistsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mArtistsRecyclerView.setAdapter(mArtistAdapter);

        RecyclerView.ItemDecoration decoration = new SpaceItemDecoration(
                getResources(),
                R.dimen.artist_item_spacing,
                SpaceItemDecoration.VERTICAL);
        mArtistsRecyclerView.addItemDecoration(decoration);
    }

    private void setEventListeners() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(TextUtils.isEmpty(newText)){
                    mArtistAdapter.clearItems();
                }else {
                    executeArtistQuery(newText);
                }
                return true;
            }
        });
    }

    private void executeArtistQuery(String query) {
        Bundle args = new Bundle();
        args.putString(ARG_QUERY_STRING, query);
        getLoaderManager().restartLoader(QUERY_LOADER_ID, args, mQueryCallbacks).forceLoad();
    }

    private void showQueryErrorMessage() {
        View container = getActivity().findViewById(android.R.id.content);
        Snackbar.make(container, R.string.message_artist_query_error, Snackbar.LENGTH_SHORT)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().getLoaderManager().getLoader(QUERY_LOADER_ID).reset();
                    }
                })
                .show();
    }

    private void showArtistNotFoundMessage() {
        View container = getActivity().findViewById(android.R.id.content);
        Snackbar.make(container, R.string.message_artist_not_found, Snackbar.LENGTH_SHORT)
                .show();
    }

    private final android.support.v4.app.LoaderManager.LoaderCallbacks<NetworkResult<ArtistsPager>> mQueryCallbacks = new android.support.v4.app.LoaderManager.LoaderCallbacks<NetworkResult<ArtistsPager>>() {

        @Override
        public Loader<NetworkResult<ArtistsPager>> onCreateLoader(int id, Bundle args) {
            String query = args.getString(ARG_QUERY_STRING);
            ArtistQueryLoader loader = new ArtistQueryLoader(getActivity(), mApiService, query);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<NetworkResult<ArtistsPager>> loader, NetworkResult<ArtistsPager> data) {
            if (data.isSuccessful()) {
                List<Artist> artists = data.getResponse().artists.items;
                if (!artists.isEmpty()) {
                    mArtistAdapter.setItems(artists);
                } else {
                    mArtistAdapter.clearItems();
                    showArtistNotFoundMessage();
                }
            } else {
                showQueryErrorMessage();
            }
        }

        @Override
        public void onLoaderReset(Loader<NetworkResult<ArtistsPager>> loader) {

        }
    };

}
