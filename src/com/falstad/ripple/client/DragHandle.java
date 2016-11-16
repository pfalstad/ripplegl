package com.falstad.ripple.client;

public class DragHandle {
	int x, y;
	DragObject parent;
	boolean hidden;
	
	DragHandle(DragObject par) {
		parent = par;
	}
	
	public String toString() { return "DragHandle(" + x + "," + y + ")"; }
	
	boolean dragTo(int xd, int yd) {
		if (parent.dragHandle(this, xd, yd)) {
			x = xd;
			y = yd;
			return true;
		}
		return false;
	}
}
