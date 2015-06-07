package com.neykov.spotifystreamer;

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
    private final @Direction int direction;

    public SpaceItemDecoration(Resources resources, @DimenRes int dimenResId, @Direction int direction) {
        this(resources.getDimensionPixelSize(dimenResId), direction);
    }

    public SpaceItemDecoration(int spacePixels, @Direction int direction) {
        this.space = spacePixels;
        this.direction = direction;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if(direction == HORIZONTAL){
            outRect.right = space;
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left = space;
            }
        }else {
            outRect.bottom = space;
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = space;
            }
        }
    }
}
