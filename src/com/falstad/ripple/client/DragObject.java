package com.falstad.ripple.client;

import java.util.Vector;

public class DragObject implements Editable {
	Vector<DragHandle> handles;
	boolean selected;
	RippleSim sim;
	
	DragObject() {
		handles = new Vector<DragHandle>(4);
		sim = RippleSim.theSim;
		sim.changedWalls = true;
	}
	
	void prepare() {}
	void select() { selected = true; }
	void deselect() { selected = false; }

	boolean drag(int dx, int dy) {
		int i;
		for (i = 0; i != handles.size(); i++) {
			DragHandle dh = handles.get(i);
			dh.x += dx;
			dh.y += dy;
		}
		return true;
	}
	
	void draw() {
		if (!selected)
			return;
		int i;
		for (i = 0; i != handles.size(); i++) {
			DragHandle dh = handles.get(i);
			RippleSim.drawHandle(dh.x-sim.windowOffsetX, dh.y-sim.windowOffsetY);
		}
	}
	
	static double hypotf(double x, double y) {
		return Math.sqrt(x*x+y*y);
	}
	
	static double distanceToLineSegment(double x, double y, double lx1, double ly1, double lx2, double ly2)
	{
	    x   -= lx1;
	    y   -= ly1;
	    lx2 -= lx1;
	    ly2 -= ly1;
	    double lr = hypotf(lx2, ly2);
	    
	    // project along line
	    double proj1 = (x*lx2+y*ly2)/(lr*lr);
	    
	    // if we are off edge of line, return distance to nearest endpoint
	    if (proj1 < 0)
	        return hypotf(x, y);
	    if (proj1 > 1) {
	        x -= lx2;
	        y -= ly2;
	        return hypotf(x, y);
	    }
	    
	    // return distance from line
	    double proj2 = x*ly2-y*lx2;
	    double dist = Math.abs(proj2)/lr;
	    return dist;
	}

	double hitTest(int x, int y) {
		if (handles.size() == 1) {
			DragHandle dh = handles.get(0);
			return hypotf(dh.x-x, dh.y-y);
		}
		DragHandle dh1 = handles.get(0);
		DragHandle dh2 = handles.get(1);
		double d = distanceToLineSegment(x, y, dh1.x, dh1.y, dh2.x, dh2.y);
		return d;
	}
	
	boolean dragHandle(DragHandle dh, int x, int y) {
		sim.changedWalls = true;
		return true;
	}
	
	void setInitialPosition() {
		if (handles.size() == 1) {
			Rectangle start = sim.findSpace(this, 0, 0);
			DragHandle dh = handles.get(0);
			dh.x = start.x;
			dh.y = start.y;
		}
		if (handles.size() == 2) {
			Rectangle start = sim.findSpace(this, 40, 0);
			DragHandle dh1 = handles.get(0);
			DragHandle dh2 = handles.get(1);
			dh1.x = start.x;
			dh1.y = start.y;
			dh2.x = start.x + start.width;
			dh2.y = start.y;
		}
	}
	
	Rectangle boundingBox() {
		int minx = 10000, miny = 10000, maxx = -10000, maxy = -10000;
		int i;
		for (i = 0; i != handles.size(); i++) {
			DragHandle dh = handles.get(0);
            if (dh.x < minx)
                minx = dh.x;
        if (dh.y < miny)
                miny = dh.y;
        if (dh.x > maxx)
                maxx = dh.x;
        if (dh.y > maxy)
                maxy = dh.y;
		}
		return new Rectangle(minx, miny, maxx-minx, maxy-miny);
	}

	@Override
	public EditInfo getEditInfo(int n) {
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei) {
	}
}
