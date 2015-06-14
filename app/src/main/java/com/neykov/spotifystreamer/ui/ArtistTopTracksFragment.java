package com.neykov.spotifystreamer.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neykov.spotifystreamer.PreferenceConstants;
import com.neykov.spotifystreamer.R;
import com.neykov.spotifystreamer.SpotifyStreamerApplication;
import com.neykov.spotifystreamer.adapter.BaseArrayAdapter;
import com.neykov.spotifystreamer.adapter.TracksAdapter;
import com.neykov.spotifystreamer.networking.ArtistTracksQueryLoader;
import com.neykov.spotifystreamer.networking.NetworkResult;
import com.neykov.spotifystreamer.ui.base.BaseFragment;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistTopTracksFragment extends BaseFragment {

    public interface OnTrackSelectedListener{
        void onTrackSelected(int trackNumber, Track[] tracks);
    }

    public static String TAG = ArtistTopTracksFragment.class.getSimpleName();

    private static final String ARG_ARTIST = "ArtistTopTracksFragment.Artist";
    private static final String KEY_LAYOUT_MANAGER_STATE = "ArtistTopTracksFragment.LayoutManagerState";
    private static final String KEY_ADAPTER_STATE = "ArtistTopTracksFragment.TracksAdapterState";
    private static final String ARG_QUERY_COUNTRYCODE_STRING = "ArtistTopTracksFragment.Query.CountryCode";

    private static final int QUERY_LOADER_ID = (TAG + ".LoaderID").hashCode();

    public static ArtistTopTracksFragment newInstance(Artist artist) {
        if(artist == null){
            throw new IllegalArgumentException("No artist provided.");
        }

        Bundle args = new Bundle();
        args.putSerializable(ARG_ARTIST, artist);
        ArtistTopTracksFragment instance = new ArtistTopTracksFragment();
        instance.setArguments(args);
        return instance;
    }

    private Artist mArtist;
    private TracksAdapter mTracksAdapter;
    private SpotifyService mApiService;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mTracksRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private Track[] mTracksQueryResult;
    private OnTrackSelectedListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof OnTrackSelectedListener){
            mListener = (OnTrackSelectedListener) activity;
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArtist = (Artist) getArguments().getSerializable(ARG_ARTIST);
        mApiService = SpotifyStreamerApplication.getInstance().getSpotifyAPIService();

        mTracksAdapter = new TracksAdapter();
        mTracksAdapter.setOnItemSelectedListener(mItemSelectedListener);
        if (savedInstanceState != null) {
            Parcelable arrayData = savedInstanceState.getParcelable(KEY_ADAPTER_STATE);
            mTracksAdapter.onRestoreInstanceState(arrayData);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_track_list, container, false);
        intializeViewReferences(rootView);
        configureRecyclerView(savedInstanceState);
        setEventListeners();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState == null){
            executeTracksQuery();
        }
    }

    @Override
    public boolean hasBackNavigation() {
        return true;
    }

    @Override
    public String getScreenTitle() {
        return getString(R.string.title_top_tracks);
    }

    @Override
    public String getScreenSubtitle() {
        if(mArtist.name != null) {
            return mArtist.name;
        }else {
            return super.getScreenSubtitle();
        }
    }

    private void intializeViewReferences(View rootView) {
        mTracksRecyclerView = (RecyclerView) rootView.findViewById(R.id.trackList);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
    }

    private void setEventListeners(){
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                executeTracksQuery();
            }
        });
    }

    private void configureRecyclerView(Bundle savedState) {
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        if (savedState != null) {
            Parcelable state = savedState.getParcelable(KEY_LAYOUT_MANAGER_STATE);
            mLayoutManager.onRestoreInstanceState(state);
        }

        mTracksRecyclerView.setLayoutManager(mLayoutManager);
        mTracksRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mTracksRecyclerView.setAdapter(mTracksAdapter);

        RecyclerView.ItemDecoration decoration = new SpaceItemDecoration(
                getResources(),
                R.dimen.artist_item_spacing,
                R.dimen.activity_vertical_margin,
                R.dimen.activity_vertical_margin,
                SpaceItemDecoration.VERTICAL);
        mTracksRecyclerView.addItemDecoration(decoration);
    }

    private void executeTracksQuery() {
        mSwipeRefreshLayout.setRefreshing(true);
        String countryCode = getActivity()
                .getSharedPreferences(PreferenceConstants.SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE)
                .getString(PreferenceConstants.KEY_PREFFERED_COUNTRY, PreferenceConstants.PREFFERED_COUNTRY_DEFAULT_VALUE);

        Bundle args = new Bundle();
        args.putString(ARG_QUERY_COUNTRYCODE_STRING, countryCode);
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

    private final LoaderManager.LoaderCallbacks<NetworkResult<Tracks>> mQueryCallbacks = new LoaderManager.LoaderCallbacks<NetworkResult<Tracks>>() {

        @Override
        public Loader<NetworkResult<Tracks>> onCreateLoader(int id, Bundle args) {
            String countryCode = args.getString(ARG_QUERY_COUNTRYCODE_STRING);
            ArtistTracksQueryLoader loader = new ArtistTracksQueryLoader(getActivity(),mApiService, mArtist, countryCode);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<NetworkResult<Tracks>> loader, NetworkResult<Tracks> data) {
            mSwipeRefreshLayout.setRefreshing(false);
            if (data.isSuccessful()) {
                List<Track> trackList = data.getResponse().tracks;
                mTracksQueryResult = trackList.toArray(new Track[trackList.size()]);
                mTracksAdapter.setItems(trackList);
            } else {
                showQueryErrorMessage();
            }
        }

        @Override
        public void onLoaderReset(Loader<NetworkResult<Tracks>> loader) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    };

    private BaseArrayAdapter.OnItemSelectedListener<Track> mItemSelectedListener = new BaseArrayAdapter.OnItemSelectedListener<Track>() {
        @Override
        public void onItemSelected(int position, Track item) {
            if(mListener != null){
                mListener.onTrackSelected(position, mTracksQueryResult);
            }
        }
    };
}
