package com.falstad.ripple.client;

public class GradientBox extends RectDragObject {
	double speedIndex, speedIndex2;
	
	GradientBox() {
		speedIndex = .25;
		speedIndex2 = .5;
	}
	
	void prepare() {
		RippleSim.drawMedium(topLeft.x, topLeft.y, topRight.x, topRight.y, 
				bottomLeft.x, bottomLeft.y,
				bottomRight.x, bottomRight.y,
				speedIndex, speedIndex2);
	}
	
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Speed Index 1", speedIndex, 0, 1).
                setDimensionless();
        if (n == 1)
            return new EditInfo("Speed Index 2", speedIndex2, 0, 1).
                setDimensionless();
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
        	speedIndex = ei.value;
        if (n == 1)
        	speedIndex2 = ei.value;
    }

}
