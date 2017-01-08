package com.falstad.ripple.client;

public class MediumEllipse extends MediumBox {
	MediumEllipse() {}
	MediumEllipse(StringTokenizer st) { super(st); }
	
	void prepare() {
		RippleSim.drawSolidEllipse(
				(topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2,
				(topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2, speedIndex);
	}

	@Override double hitTest(int x, int y) {
		x -= (topLeft.x+topRight.x)/2;
		y -= (topLeft.y+bottomLeft.y)/2;
		double a = (topRight.x-topLeft.x)/2;
		double b = (bottomLeft.y-topLeft.y)/2;
		double ht = Math.abs(Math.sqrt(x*x/(a*a)+y*y/(b*b))-1)*a;
		return ht;
	}
	
	/*
	@Override boolean hitTestInside(double x, double y) {
		x -= (topLeft.x+topRight.x)/2;
		y -= (topLeft.y+bottomLeft.y)/2;
		double a = (topRight.x-topLeft.x)/2;
		double b = (bottomLeft.y-topLeft.y)/2;
		double ht = Math.sqrt(x*x/(a*a)+y*y/(b*b));
		return ht <= 1;
	}
	*/
	
	@Override void drawSelection() {
		RippleSim.drawEllipse(
				(topLeft.x+topRight.x)/2, (topLeft.y+bottomLeft.y)/2,
				(topRight.x-topLeft.x)/2, (bottomLeft.y-topLeft.y)/2);
	}
	
	int getDumpType() { return 'E'; }

}
