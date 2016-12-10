package com.falstad.ripple.client;

public class Lens extends MediumBox {
	void prepare() {
		RippleSim.drawLens(topLeft.x, topLeft.y, width(), height(), speedIndex);
	}

}
