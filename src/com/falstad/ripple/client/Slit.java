package com.falstad.ripple.client;

public class Slit extends Wall {
	int slitCount, slitWidth, slitSeparation;
	
	Slit() {
		slitCount = 1;
		slitWidth = 10;
		slitSeparation = 10;
	}
	
	Slit(StringTokenizer st) {
		super(st);
		slitCount = new Integer(st.nextToken()).intValue();
		slitWidth = new Integer(st.nextToken()).intValue();
		slitSeparation = new Integer(st.nextToken()).intValue();
	}
	
	String dump() {
		return super.dump() + " " + slitCount + " " + slitWidth + " " + slitSeparation;
	}
	
	void prepare() {
		DragHandle h1 = handles.get(0);
		DragHandle h2 = handles.get(1);
		double dx = h2.x-h1.x;
		double dy = h2.y-h1.y;
		double len = Math.hypot(dx, dy);
		double pos0 = (len-slitCount*slitWidth-(slitCount-1)*slitSeparation)/2;
		if (pos0 < 0)
			return;
		int i;
		double n1 = 0;
		for (i = 0; i != slitCount; i++) {
			double n2 = (pos0+(slitWidth+slitSeparation)*i)/len;
			drawWall(h1.x+dx*n1, h1.y+dy*n1, h1.x+dx*n2, h1.y+dy*n2);
			n1 = n2+slitWidth/len;
		}
		RippleSim.drawWall(h2.x, h2.y, (int)(h1.x+dx*n1), (int)(h1.y+dy*n1)); 
	}

	void drawWall(double x1, double y1, double x2, double y2) {
		RippleSim.drawWall((int)x1, (int)y1, (int)x2, (int)y2);
	}
	
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Slit Count", slitCount, 0, 1).
                setDimensionless();
        if (n == 1)
            return new EditInfo("Slit Width", slitWidth, 0, 1).
                setDimensionless();
        if (n == 2)
            return new EditInfo("Slit Separation", slitSeparation, 0, 1).
                setDimensionless();
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
        	slitCount = (int)ei.value;
        if (n == 1)
        	slitWidth = (int)ei.value;
        if (n == 2)
        	slitSeparation = (int)ei.value;
    }

	int getDumpType() { return 203; }

}
