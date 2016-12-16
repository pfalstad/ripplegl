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
}
