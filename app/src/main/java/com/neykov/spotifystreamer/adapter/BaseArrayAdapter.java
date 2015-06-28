package com.neykov.spotifystreamer.adapter;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class BaseArrayAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{

    public interface OnItemSelectedListener<T>{
        void onItemSelected(int position, T item);
    }

    private List<T> mItems;
    private OnItemSelectedListener<T> mListener;

    protected BaseArrayAdapter() {
        mItems = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @SuppressWarnings("unchecked")
    public Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        T[] entries = (T[]) mItems.toArray();
        state.putSerializable(this.getClass().getSimpleName() + ".Items", entries);
        return state;
    }

    @SuppressWarnings("unchecked")
    public void onRestoreInstanceState(Parcelable savedAdapterState) {
        Bundle state = (Bundle) savedAdapterState;
        if (state == null) {
            throw new IllegalArgumentException("Invalid saved state provided.");
        }
        T[] items = (T[]) state.getSerializable(this.getClass().getSimpleName() + ".Items");
        if (items == null) {
            throw new IllegalArgumentException("Invalid state argument.");
        }

        this.setItems(items);
    }

    public void setItems(List<T> items) {
        this.mItems.clear();
        mItems.addAll(items);
        this.notifyDataSetChanged();
    }

    public void setItems(T[] items) {
        setItems(Arrays.asList(items));
    }

    public void clearItems() {
        this.mItems.clear();
        this.notifyDataSetChanged();
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(mItems);
    }

    protected T getItemAt(int position) {
        return mItems.get(position);
    }

    protected OnItemSelectedListener<T> getOnItemSelectedListener(){
        return mListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener<T> listener){
        mListener = listener;
    }
}
