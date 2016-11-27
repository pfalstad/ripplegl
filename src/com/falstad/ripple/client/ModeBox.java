package com.falstad.ripple.client;

public class ModeBox extends RectDragObject {
	int xmode, ymode;
	boolean box, randomize;
	
	ModeBox() {
		xmode = 1;
		ymode = 1;
		box = true;
	}
	
	int rand() {
		return Math.abs(sim.random.nextInt());
	}
	
	void prepare() {
		if (box) {
			RippleSim.drawWall(topLeft.x, topLeft.y, topRight.x, topRight.y);
			RippleSim.drawWall(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y); 
			RippleSim.drawWall(bottomRight.x, bottomRight.y, topRight.x, topRight.y);
			RippleSim.drawWall(bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
		}
		if (randomize) {
			int m1x = rand() % xmode;
			int m1y = rand() % ymode;
			int m2x = rand() % xmode;
			int m2y = rand() % ymode;
			if (sim.fixedEndsCheck.getState()) {
				if (m1x == 0) m1x = 1;
				if (m1y == 0) m1y = 1;
				if (m2x == 0) m2x = 1;
				if (m2y == 0) m2y = 1;
			}
			RippleSim.drawModes(topLeft.x+1, topLeft.y+1, bottomRight.x-1, bottomRight.y-1,
					m1x*Math.PI, m1y*Math.PI, m2x*Math.PI, m2y*Math.PI);
		} else {
			RippleSim.drawModes(topLeft.x+1, topLeft.y+1, bottomRight.x-1, bottomRight.y-1,
				xmode*Math.PI, ymode*Math.PI, 0, 0);
		}
	}
	
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("x mode", xmode, 0, 1).
                setDimensionless();
        if (n == 1)
            return new EditInfo("y mode", ymode, 0, 1).
                setDimensionless();
        if (n == 2) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Randomize", randomize);
            return ei;
        }
        if (n == 3) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Draw Box", box);
            return ei;
        }
        
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
        	xmode = (int)ei.value;
        if (n == 1)
        	ymode = (int)ei.value;
        if (n == 2)
        	randomize = ei.checkbox.getState();
        if (n == 3)
        	box = ei.checkbox.getState();
    }


}
