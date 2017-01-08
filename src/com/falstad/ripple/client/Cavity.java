package com.falstad.ripple.client;

public class Cavity extends RectDragObject {

	Cavity() {}
	Cavity(StringTokenizer st) { super(st); }
	
	void prepare() {
		RippleSim.drawWall(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y); 
		RippleSim.drawWall(bottomRight.x, bottomRight.y, topRight.x, topRight.y);
		RippleSim.drawWall(bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
	}

	@Override void drawSelection() {
		prepare();
	}
	
	// let people poke inside
	boolean hitTestInside(double x, double y) { return false; }

	int getDumpType() { return 'c'; }
}
