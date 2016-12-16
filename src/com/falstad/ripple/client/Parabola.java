package com.falstad.ripple.client;

public class Parabola extends RectDragObject {
	
	Parabola() {}
	Parabola(StringTokenizer st) { super(st); }
	
	void prepare() {
		RippleSim.drawParabola(topLeft.x, topLeft.y, width(), height());
	}

	@Override void drawSelection() {
		prepare();
	}
	
	int getDumpType() { return 'p'; }

}
