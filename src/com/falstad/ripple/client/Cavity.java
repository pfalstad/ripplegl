package com.falstad.ripple.client;

public class Cavity extends RectDragObject {

	void prepare() {
		RippleSim.drawWall(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y); 
		RippleSim.drawWall(bottomRight.x, bottomRight.y, topRight.x, topRight.y);
		RippleSim.drawWall(bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
	}

	@Override void drawSelection() {
		prepare();
	}
}
