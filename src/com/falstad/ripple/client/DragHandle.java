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

public class DragHandle {
	int x, y;
	DragObject parent;
	boolean hidden;
	
	DragHandle(DragObject par) {
		parent = par;
	}
	
	DragHandle(DragObject par, int xa, int ya) {
		parent = par;
		x = xa;
		y = ya;
	}

	DragHandle(DragObject par, StringTokenizer st) {
		parent = par;
		x = new Integer(st.nextToken()).intValue();
		y = new Integer(st.nextToken()).intValue();
	}
	
	public String toString() { return "DragHandle(" + x + "," + y + ")"; }
	
	boolean dragTo(int xd, int yd) {
		if (parent.dragHandle(this, xd, yd)) {
			x = xd;
			y = yd;
			parent.setTransform();
			return true;
		}
		return false;
	}
	
	void rescale(double scale) {
		x = (int) (x*scale);
		y = (int) (y*scale);
	}
}
