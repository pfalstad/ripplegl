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
}
