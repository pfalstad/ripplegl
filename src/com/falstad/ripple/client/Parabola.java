package com.falstad.ripple.client;

public class Parabola extends RectDragObject {
	void prepare() {
		RippleSim.drawParabola(topLeft.x, topLeft.y, width(), height());
	}

}
