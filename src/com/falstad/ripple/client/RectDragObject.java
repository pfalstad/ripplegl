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

public abstract class RectDragObject extends DragObject {
	DragHandle topLeft, topRight, bottomLeft, bottomRight;
	
	RectDragObject() {
		int i;
		for (i = 0; i != 4; i++) {
			DragHandle dh = new DragHandle(this);
			handles.add(dh);
		}
		topLeft =  handles.get(0);
		topRight = handles.get(1);
		bottomRight = handles.get(2);
		bottomLeft  = handles.get(3);
		setTransform();
	}
	
	RectDragObject(StringTokenizer st) {
		super(st);
		topLeft = new DragHandle(this, st);
		bottomRight = new DragHandle(this, st);
		topRight = new DragHandle(this, bottomRight.x, topLeft.y);
		bottomLeft = new DragHandle(this, topLeft.x, bottomRight.y);
		handles.add(topLeft);
		handles.add(topRight);
		handles.add(bottomRight);
		handles.add(bottomLeft);
		rotation = new Double(st.nextToken()).doubleValue();
		setTransform();
	}
	
	String dump() {
		return super.dump() + " " + rotation;
	}
	
	boolean hitTestInside(double x, double y) {
		Point origin = rotatedOrigin();
		x -= origin.x;
		y -= origin.y;
		return x >= 0 && x <= width() && y >= 0 && y <= height(); 
	}
	
	static boolean minXHandles[] = { true, false, false, true };
	static boolean minYHandles[] = { true, true, false, false };

	@Override boolean dragHandle(DragHandle handle, int x, int y) {
	    int i;
	    int handleIndex = -1;
	    for (i = 0; i != 4; i++) {
	        DragHandle dh = handles.get(i);
	        if (dh == handle) {
	            handleIndex = i;
	            break;
	        }
	    }
	    if (handleIndex < 0)
	        return false;
	    
	    // undo any rotation
	    Point pt = new Point(x, y); // [self rotatePoint:CGPointMake(x_, y_) dir:-1];
	    
	    // check that rectangle is not backwards
	    for (i = 0; i != 4; i++) {
	        DragHandle dh = handles.get(i);
	        Point hp = new Point(dh.x, dh.y);
//	        CGPoint hp = [self rotatePoint:CGPointMake(dh.x, dh.y) dir:-1];
	        if (minXHandles[i] && !minXHandles[handleIndex] && pt.x <= hp.x)
	            return false;
	        if (!minXHandles[i] && minXHandles[handleIndex] && pt.x >= hp.x)
	            return false;
	        if (minYHandles[i] && !minYHandles[handleIndex] && pt.y <= hp.y)
	            return false;
	        if (!minYHandles[i] && minYHandles[handleIndex] && pt.y >= hp.y)
	            return false;
	    }

	    // move affected points
	    for (i = 0; i != 4; i++) {
	        DragHandle dh = handles.get(i);
	        Point hp = new Point(dh.x, dh.y);
//	        CGPoint hp = [self rotatePoint:CGPointMake(dh.x, dh.y) dir:-1];
	        if (minXHandles[i] == minXHandles[handleIndex])
	            hp.x = pt.x;
	        if (minYHandles[i] == minYHandles[handleIndex])
	            hp.y = pt.y;
	        
	        // redo rotation
//	        hp = [self rotatePoint:hp dir:1];
	        dh.x = hp.x;
	        dh.y = hp.y;        
	    }
	    
		return false;
	}
	
	@Override double hitTest(int x, int y) {
		double result = 1e8;
		
	    // find minimum distance to any edge
	    int i;
	    for (i = 0; i != 4; i++) {
	        DragHandle dh1 = handles.get(i); 
	        DragHandle dh2 = handles.get((i+1) % 4);
	        double d = distanceToLineSegment(x, y, dh1.x, dh1.y, dh2.x, dh2.y);
	        if (d < result)
	            result = d;
	    }

		return result;
	}
	
	@Override boolean canRotate() { return true; }

	int width() { return topRight.x-topLeft.x; }
	int height() { return bottomLeft.y-topLeft.y; }
	Point rotatedOrigin() { return new Point(topLeft.x, topLeft.y); }
	
	@Override void setInitialPosition() {
		Rectangle start = sim.findSpace(this, 20, 20);
		topLeft.x = bottomLeft.x = start.x;
		topLeft.y = topRight.y = start.y;
		bottomRight.x = topRight.x = start.x + start.width;
		bottomRight.y = bottomLeft.y = start.y + start.height;
	}
	
	@Override void drawSelection() {
		RippleSim.drawWall(topLeft.x, topLeft.y, topRight.x, topRight.y);
		RippleSim.drawWall(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y); 
		RippleSim.drawWall(bottomRight.x, bottomRight.y, topRight.x, topRight.y);
		RippleSim.drawWall(bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
	}
	
	String dumpHandles() {
		return " " + topLeft.x + " " + topLeft.y + " " + bottomRight.x + " " + bottomRight.y;
	}

	String selectText() {
		return "" + sim.getLengthText(width()) + " x " + sim.getLengthText(height());
	}
}
