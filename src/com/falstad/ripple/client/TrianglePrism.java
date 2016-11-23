package com.falstad.ripple.client;

public class TrianglePrism extends MediumBox {
	void prepare() {
		RippleSim.drawTriangle(topLeft.x, topLeft.y, topRight.x, topRight.y, 
				bottomLeft.x, bottomLeft.y,
				speedIndex);
	}

}
