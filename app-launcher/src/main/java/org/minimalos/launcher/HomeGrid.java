package org.minimalos.launcher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/** No scrolling or fixed tile height: children divide the measured available area. */
final class HomeGrid extends ViewGroup {
    private HomeGridPlan plan = new HomeGridPlan(1,1);
    HomeGrid(Context context) { super(context); }
    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec), height = MeasureSpec.getSize(heightSpec);
        float density = getResources().getDisplayMetrics().density;
        plan = HomeGridPlan.fit(Math.round(width/density),Math.round(height/density),getChildCount());
        int gap = Math.min(Ui.dp(getContext(),3), Math.min(width/plan.columns,height/plan.rows)/4);
        for (int i=0;i<getChildCount();i++) {
            int col=i%plan.columns,row=i/plan.columns;
            int w=(col+1)*width/plan.columns-col*width/plan.columns;
            int h=(row+1)*height/plan.rows-row*height/plan.rows;
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(Math.max(0,w-2*gap),MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(Math.max(0,h-2*gap),MeasureSpec.EXACTLY));
        }
        setMeasuredDimension(width,height);
    }
    @Override protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
        int width=right-left,height=bottom-top;
        int gap=Math.min(Ui.dp(getContext(),3),Math.min(width/plan.columns,height/plan.rows)/4);
        for (int i=0;i<getChildCount();i++) {
            int col=i%plan.columns,row=i/plan.columns;
            boolean rtl=getLayoutDirection()==View.LAYOUT_DIRECTION_RTL;
            if (rtl) col=plan.columns-1-col;
            getChildAt(i).layout(col*width/plan.columns+gap,row*height/plan.rows+gap,
                    (col+1)*width/plan.columns-gap,(row+1)*height/plan.rows-gap);
        }
    }
    @Override protected LayoutParams generateDefaultLayoutParams() { return new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT); }
}
