package com.neykov.spotifystreamer.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.neykov.spotifystreamer.R;
import com.neykov.spotifystreamer.ui.base.ActionBarConfigurable;
import com.neykov.spotifystreamer.ui.base.ActionbarConfigurator;

public class BaseFragment extends Fragment implements ActionbarConfigurator {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        if(activity instanceof ActionBarConfigurable){
            ((ActionBarConfigurable)activity).onApplyConfiguratorOptions(this);
        }
    }

    @Override
    public boolean hasBackNavigation() {
        return false;
    }

    @Override
    public boolean hasScreenTitle() {
        return true;
    }

    @Override
    public String getScreenTitle() {
        return getString(R.string.app_name);
    }

    @Override
    public String getScreenSubtitle() {
        return "";
    }
}
