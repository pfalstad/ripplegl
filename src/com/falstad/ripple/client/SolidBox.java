package com.falstad.ripple.client;

public class SolidBox extends RectDragObject {
	
	SolidBox() {}
	SolidBox(StringTokenizer st) { super(st); }
	
	void prepare() {
		RippleSim.drawMedium(topLeft.x, topLeft.y, topRight.x, topRight.y, 
				bottomLeft.x, bottomLeft.y,
				bottomRight.x, bottomRight.y,
				0, 0);
	}

	int getDumpType() { return 202; }

}
