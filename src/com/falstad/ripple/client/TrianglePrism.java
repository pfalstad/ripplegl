package com.falstad.ripple.client;

public class TrianglePrism extends MediumBox {
	
	TrianglePrism() {}
	TrianglePrism(StringTokenizer st) { super(st); }
	
	void prepare() {
		RippleSim.drawTriangle(topLeft.x, topLeft.y, topRight.x, topRight.y, 
				bottomLeft.x, bottomLeft.y,
				speedIndex);
	}

	@Override void drawSelection() {
		RippleSim.drawWall(topLeft.x, topLeft.y, topRight.x, topRight.y);
		RippleSim.drawWall(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y); 
		RippleSim.drawWall(bottomLeft.x, bottomLeft.y, topRight.x, topRight.y);
	}
	
	// let people poke inside
	boolean hitTestInside(double x, double y) { return false; }

	int getDumpType() { return 't'; }

}
