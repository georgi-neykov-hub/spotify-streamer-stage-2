package com.neykov.spotifystreamer.ui.base;

public interface ActionbarConfigurator {

    boolean hasBackNavigation();
    boolean hasScreenTitle();
    String getScreenTitle();
    String getScreenSubtitle();
}
