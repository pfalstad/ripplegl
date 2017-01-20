package com.falstad.ripple.client;

public class Ellipse extends RectDragObject {
	Ellipse(){}
	Ellipse(StringTokenizer st) { super(st); }
	
	void prepare() {
		RippleSim.drawEllipse(
				(topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2,
				(topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2);
	}

	boolean hitTestInside(double x, double y) { return false; }

	@Override double hitTest(int x, int y) {
		x -= (topLeft.x+topRight.x)/2;
		y -= (topLeft.y+bottomLeft.y)/2;
		double a = (topRight.x-topLeft.x)/2;
		double b = (bottomLeft.y-topLeft.y)/2;
		double ht = Math.abs(Math.sqrt(x*x/(a*a)+y*y/(b*b))-1)*a;
		return ht;
	}
	
	@Override void drawSelection() {
		prepare();
		double a = (topRight.x-topLeft.x)/2;
		double b = (bottomRight.y-topRight.y)/2;
		int fc = (int)Math.sqrt(Math.abs(a*a-b*b));
		int fd = fc;
		if (a > b)
			fd = 0;
		else
			fc = 0;
		RippleSim.drawFocus((topLeft.x+topRight.x)/2-fc, (topLeft.y+bottomLeft.y)/2-fd);
		RippleSim.drawFocus((topLeft.x+topRight.x)/2+fc, (topLeft.y+bottomLeft.y)/2+fd);
	}
	
	int getDumpType() { return 'e'; }

}
