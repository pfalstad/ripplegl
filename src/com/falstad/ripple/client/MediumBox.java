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
            return new EditInfo("Refractive Index (1-2)", Math.sqrt(1/speedIndex), 0, 1).
                setDimensionless();
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0) {
        	speedIndex = getRefractiveIndex(ei.value);
        	ei.value = Math.sqrt(1/speedIndex);
        	EditDialog.theEditDialog.updateValue(ei);
        }
    }

    static double getRefractiveIndex(double v) {
    	if (v < 1)
    		v = 1;
    	if (v > 2)
    		v = 2;
    	return 1/(v*v);
    }
    
	int getDumpType() { return 'm'; }
	String dump() { return super.dump() + " " + speedIndex; }
}
