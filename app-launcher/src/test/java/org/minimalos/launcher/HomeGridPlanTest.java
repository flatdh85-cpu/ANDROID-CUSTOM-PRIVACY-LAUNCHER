package org.minimalos.launcher;
import org.junit.Test;
import static org.junit.Assert.*;
public class HomeGridPlanTest {
    @Test public void allActionsFitCommonPortraitLandscapeAndSmallPhoneViewports() {
        int[][] sizes={{308,294},{348,490},{588,182},{228,286},{828,306}};
        for(int[] size:sizes) {
            HomeGridPlan plan=HomeGridPlan.fit(size[0],size[1],14);
            assertTrue(plan.rows*plan.columns>=14);
            assertTrue(size[0]/plan.columns>=64);
            assertTrue(size[1]/plan.rows>=48);
            int previousBottom=0;
            for(int row=0;row<plan.rows;row++) {
                int top=row*size[1]/plan.rows, bottom=(row+1)*size[1]/plan.rows;
                assertEquals(previousBottom,top); assertTrue(bottom<=size[1]); previousBottom=bottom;
            }
            assertEquals(size[1],previousBottom);
        }
    }
    @Test public void landscapeUsesMoreColumnsInsteadOfScrolling() {
        HomeGridPlan portrait=HomeGridPlan.fit(348,490,14),landscape=HomeGridPlan.fit(588,182,14);
        assertTrue(landscape.columns>portrait.columns); assertTrue(landscape.rows<portrait.rows);
    }
    @Test public void extremeOrEmptyWindowsDoNotDivideByZero() {
        for(int[] size:new int[][]{{0,0},{1,1},{120,90}}) {
            HomeGridPlan plan=HomeGridPlan.fit(size[0],size[1],14);
            assertTrue(plan.columns>0); assertTrue(plan.rows>0); assertTrue(plan.columns*plan.rows>=14);
        }
        assertEquals(1,HomeGridPlan.fit(0,0,0).columns);
    }
}
