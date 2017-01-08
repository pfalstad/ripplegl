package com.falstad.ripple.client;

public class Parabola extends RectDragObject {
	
	Parabola() {}
	Parabola(StringTokenizer st) { super(st); }
	
	void prepare() {
		RippleSim.drawParabola(topLeft.x, topLeft.y, width(), height());
		double w2 = width()/2.;
		double a = height()/(w2*w2);
		int p = (int) (1/(4*a));
		RippleSim.drawFocus(topLeft.x+width()/2, bottomLeft.y-p);
	}

	@Override void drawSelection() {
		prepare();
	}
	
	// let people poke inside
	boolean hitTestInside(double x, double y) { return false; }

	int getDumpType() { return 'p'; }

}
