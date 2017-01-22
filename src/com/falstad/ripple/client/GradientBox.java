package com.falstad.ripple.client;

public class GradientBox extends RectDragObject {
	double speedIndex, speedIndex2;
	
	GradientBox() {
		speedIndex = .25;
		speedIndex2 = .5;
	}

	GradientBox(StringTokenizer st) {
		super(st);
		speedIndex  = new Double(st.nextToken()).doubleValue();
		speedIndex2 = new Double(st.nextToken()).doubleValue();
	}
	
	void prepare() {
		RippleSim.drawMedium(topLeft.x, topLeft.y, topRight.x, topRight.y, 
				bottomLeft.x, bottomLeft.y,
				bottomRight.x, bottomRight.y,
				speedIndex, speedIndex2);
	}
	
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Refractive Index 1", Math.sqrt(1/speedIndex), 0, 1).
                setDimensionless();
        if (n == 1)
            return new EditInfo("Refractive Index 2", Math.sqrt(1/speedIndex2), 0, 1).
                setDimensionless();
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
        	speedIndex = MediumBox.getRefractiveIndex(ei.value);
        if (n == 1)
        	speedIndex2 = MediumBox.getRefractiveIndex(ei.value);
    }

	int getDumpType() { return 'g'; }
	String dump() { return super.dump() + " " + speedIndex + " " + speedIndex2; }
}
