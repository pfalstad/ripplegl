package com.falstad.ripple.client;

public class MovingWall extends RectDragObject {

	int moveDuration, pauseDuration;
	int startIter;
	int phase;
	int lastWallX1, lastWallX2, lastWallY1, lastWallY2;
	
	MovingWall() {
		moveDuration = 900;
		pauseDuration = 500;
		startIter = 0;
		phase = 0;
	}
	
	void run() {
		RippleSim.clearWall(lastWallX1, lastWallY1, lastWallX2, lastWallY2);
		int x1 = topLeft.x;
		int x2 = topRight.x;
		int y1 = topLeft.y;
		int y2 = bottomLeft.y;
		double dur = (phase == 0 || phase == 2) ? moveDuration : pauseDuration;
		double step = (sim.iters-startIter)/dur;
		if (step > 1) {
			step = 1;
			startIter = sim.iters;
			phase++;
			if (phase >= 4)
				phase = 0;
			step = 0;
		}
		if (phase == 1)
			step = 1;
		if (phase == 3)
			step = 0;
		if (phase == 2)
			step = 1-step;
		int y = (int) (y1+(y2-y1)*step); 
		lastWallX1 = x1;
		lastWallY1 = y;
		lastWallX2 = x2;
		lastWallY2 = y;
		RippleSim.drawWall(x1, y, x2, y);
	}
	
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Move Duration", moveDuration, 0, 1).
                setDimensionless();
        if (n == 1)
            return new EditInfo("Pause Duration", pauseDuration, 0, 1).
                setDimensionless();
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
        	moveDuration = (int)ei.value;
        if (n == 1)
        	pauseDuration = (int)ei.value;
    }

}
