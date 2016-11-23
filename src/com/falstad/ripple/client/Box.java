package com.falstad.ripple.client;

public class Box extends RectDragObject {

	void prepare() {
		RippleSim.drawWall(topLeft.x, topLeft.y, topRight.x, topRight.y);
		RippleSim.drawWall(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y); 
		RippleSim.drawWall(bottomRight.x, bottomRight.y, topRight.x, topRight.y);
		RippleSim.drawWall(bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
	}

	boolean hitTestInside(double x, double y) { return false; }
}
