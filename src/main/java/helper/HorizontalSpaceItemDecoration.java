package helper;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int mHorizontalpaceHeight;

    public HorizontalSpaceItemDecoration(int mHorizontalpaceHeight) {
        this.mHorizontalpaceHeight = mHorizontalpaceHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.right = mHorizontalpaceHeight;
    }
}