package org.minimalos.launcher;

/** Chooses a grid from the actual remaining viewport, including landscape and small windows. */
final class HomeGridPlan {
    final int columns, rows;
    HomeGridPlan(int columns, int rows) { this.columns = columns; this.rows = rows; }
    static HomeGridPlan fit(int width, int height, int count) {
        if (count <= 0) return new HomeGridPlan(1,1);
        double best = Double.MAX_VALUE; int selected = 1;
        for (int columns = 1; columns <= Math.min(7,count); columns++) {
            int rows = (count + columns - 1) / columns;
            double cellWidth = Math.max(1,width) / (double) columns, cellHeight = Math.max(1,height) / (double) rows;
            double shortage = Math.max(0,64-cellWidth) + Math.max(0,48-cellHeight);
            double score = shortage * 100 + Math.abs(Math.log(cellWidth / cellHeight)) + (columns*rows-count)*0.04;
            if (score < best) { best=score; selected=columns; }
        }
        return new HomeGridPlan(selected,(count+selected-1)/selected);
    }
}
