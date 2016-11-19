package com.falstad.ripple.client;

public class Ellipse extends RectDragObject {
	void prepare() {
		RippleSim.drawEllipse(
				(topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2,
				(topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2);
	}

}
