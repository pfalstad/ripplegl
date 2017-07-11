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

public class LineSource extends Source {

	boolean gauss;
	
	LineSource() {
		handles.add(new DragHandle(this));
		setTransform();
	}
	LineSource(StringTokenizer st) {
		super(st, 2);
		setTransform();
		gauss = (flags & 1) != 0;
	}
	
	LineSource(int x, int y, int x2, int y2) {
		DragHandle h0 = handles.get(0);
		h0.x = x;
		h0.y = y;
		handles.add(new DragHandle(this, x2, y2));
		frequency = .5;
		length = 10;
		delay = 100;
		setTransform();
	}

	void run() {
		DragHandle dh1 = handles.get(0);
		DragHandle dh2 = handles.get(1);
        double v = getValue();
        if (enabled)
        	RippleSim.drawLineSource(dh1.x, dh1.y, dh2.x, dh2.y, v, gauss); 
	}

	@Override void drawSelection() {
		RippleSim.drawWall(handles.get(0).x, handles.get(0).y, handles.get(1).x, handles.get(1).y);
	}

	String dump() {
		flags = (gauss) ? 1 : 0;
		return super.dump();
	}
	
    public EditInfo getEditInfo(int n) {
    	// we don't put the Gaussian checkbox at the end because Source has a variable number of edit info
    	// items.  So we insert it at n == 1.
    	if (n == 0)
    		return super.getEditInfo(n);
    	if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Gaussian", gauss);
    		return ei;
    	}
    	return super.getEditInfo(n-1);
    }
    
    public void setEditValue(int n, EditInfo ei) {
    	if (n == 0) {
    		super.setEditValue(n, ei);
    	}
    	if (n == 1) {
    		gauss = ei.checkbox.getState();
    		return;
    	}
    	super.setEditValue(n-1, ei);
    }

	int getDumpType() { return 'S'; }

}
