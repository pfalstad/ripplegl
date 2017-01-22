/*
    Copyright (C) 2017 by Paul Falstad

    This file is part of RippleGL.

    RippleGL is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    RippleGL is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RippleGL.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.falstad.ripple.client;

public class Parabola extends RectDragObject {
	
	Parabola() {}
	Parabola(StringTokenizer st) { super(st); }
	
	void prepare() {
		RippleSim.drawParabola(topLeft.x, topLeft.y, width(), height());
		double w2 = width()/2.;
		double a = height()/(w2*w2);
		int p = (int) (1/(4*a));
		RippleSim.drawFocus(topLeft.x+width()/2, bottomLeft.y-p);
	}

	@Override void drawSelection() {
		prepare();
	}
	
	// let people poke inside
	boolean hitTestInside(double x, double y) { return false; }

	int getDumpType() { return 'p'; }

}
