package com.neykov.spotifystreamer.ui;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Direction{}
    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;

    private int space;
    private int startSpace;
    private int endSpace;
    private final @Direction int direction;

    public SpaceItemDecoration(Resources resources,
                               @DimenRes int dimenResId,
                               @DimenRes int startDimenResId,
                               @DimenRes int endDimenResId,
                               @Direction int direction) {
        this(resources.getDimensionPixelSize(dimenResId),
                resources.getDimensionPixelSize(startDimenResId),
                resources.getDimensionPixelSize(endDimenResId),
                direction);
    }

    public SpaceItemDecoration(int spacePixels, int startSpacePixels, int endSpacePixels, @Direction int direction) {
        this.space = spacePixels;
        this.endSpace = endSpacePixels;
        this.startSpace = startSpacePixels;
        this.direction = direction;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if(direction == HORIZONTAL){
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left = startSpace;
                outRect.right = space;
            }else if(parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1){
                outRect.right = endSpace;
            }else {
                outRect.right = space;
            }
        }else {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = startSpace;
                outRect.bottom = space;
            }else if(parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1){
                outRect.bottom = endSpace;
            }else {
                outRect.bottom = space;
            }
        }
    }
}
