package com.falstad.ripple.client;

public class Lens extends MediumBox {
	
	Lens() {}
	Lens(StringTokenizer st) { super(st); }
	
	void prepare() {
		RippleSim.drawLens(topLeft.x, topLeft.y, width(), height(), speedIndex);
	}

	int getDumpType() { return 'l'; }

}
