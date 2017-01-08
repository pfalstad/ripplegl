package com.falstad.ripple.client;

public class MediumBox extends RectDragObject {
	double speedIndex;
	
	MediumBox() {
		speedIndex = .25;
	}
	
	MediumBox(StringTokenizer st) {
		super(st);
		speedIndex = new Double(st.nextToken()).doubleValue();
	}
	
	MediumBox(int x, int y, int x2, int y2) {
		speedIndex = .25;
		topLeft.x = bottomLeft.x = x;
		topLeft.y = topRight.y = y;
		topRight.x = bottomRight.x = x2;
		bottomLeft.y = bottomRight.y = y2;
		setTransform();
	}

	void prepare() {
		RippleSim.drawMedium(topLeft.x, topLeft.y, topRight.x, topRight.y, 
				bottomLeft.x, bottomLeft.y,
				bottomRight.x, bottomRight.y,
				speedIndex, speedIndex);
	}
	
	// let people poke inside
	boolean hitTestInside(double x, double y) { return false; }

    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Speed Index", speedIndex, 0, 1).
                setDimensionless();
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
        	speedIndex = ei.value;
    }

	int getDumpType() { return 'm'; }
	String dump() { return super.dump() + " " + speedIndex; }
}
