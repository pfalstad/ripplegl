package com.falstad.ripple.client;

public class MediumBox extends RectDragObject {
	double speedIndex;
	
	MediumBox() {
		speedIndex = .25;
	}
	
	void prepare() {
		RippleSim.drawMedium(topLeft.x, topLeft.y, topRight.x, topRight.y, 
				bottomLeft.x, bottomLeft.y,
				bottomRight.x, bottomRight.y,
				speedIndex);
	}
	
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

}
