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

public class MultipoleSource extends Source {
	double separation, angle;
	int sourceCount;
	
	MultipoleSource() {
		sourceCount = 2;
		separation = 20;
		angle = 0;
		createHandles();
	}
	
	MultipoleSource(StringTokenizer st) {
		super(st, 2);
		DragHandle dh0 = handles.get(0);
		DragHandle dh = handles.get(1);
		sourceCount = new Integer(st.nextToken()).intValue();
		separation = Math.hypot(dh.x-dh0.x, dh.y-dh0.y);
		angle = Math.atan2(dh.y-dh0.y, dh.x-dh0.x);
		createHandles();
	}
	
	void createHandles() {
		handles.setSize(1);
		int i;
		for (i = 0; i != sourceCount; i++)
            handles.add(new DragHandle(this));
		positionHandles();
	}
	
	void positionHandles() {
		int i;
		DragHandle dh0 = handles.get(0);
		for (i = 0; i != sourceCount; i++) {
			DragHandle dh = handles.get(i+1);
    		double a = angle+Math.PI*2*i/sourceCount;
    		dh.x = (int)(dh0.x+separation*Math.cos(a));
    		dh.y = (int)(dh0.y+separation*Math.sin(a));
		}
	}
	
    boolean dragHandle(DragHandle dh, int x, int y) {
		DragHandle dh0 = handles.get(0);
		if (dh == dh0) {
			positionHandles();
			return true;
		}
		separation = Math.hypot(x-dh0.x, y-dh0.y);
		angle = Math.atan2(y-dh0.y, x-dh0.x);
		positionHandles();
		return false;
    }

	void setInitialPosition() {
        Rectangle start = sim.findSpace(this, 0, 0);
        DragHandle dh = handles.get(0);
        dh.x = start.x;
        dh.y = start.y;
		positionHandles();
	}
	
	void run() {
        double v = getValue();
        if (enabled) {
        	int i;
        	for (i = 0; i != sourceCount; i++) {
        		DragHandle dh = handles.get(i+1);
        		RippleSim.drawSource(dh.x, dh.y, i % 2 == 0 ? v : -v);
        	}
        }
	}
    
    void rotate(double ang) {
            angle += ang;
            positionHandles();
    }

    void rotateTo(int x, int y) {
        DragHandle dh = handles.get(0);
        angle = -Math.atan2(-y+dh.y, x-dh.x);
        positionHandles();
    }

    boolean canRotate() { return true; }
    

    public EditInfo getEditInfo(int n) {
        if (n == 0)
        	return new EditInfo("Source Count", sourceCount, 0, 1).setDimensionless();
        if (n == 1)
        	return new EditInfo("Radius (m)", separation*sim.lengthScale, 0, 1);
        return super.getEditInfo(n-2);
    }
    
    public void setEditValue(int n, EditInfo ei) {
    	if (n == 0) {
    		int sc = (int)ei.value;
    		if (sc > 1 && (sc % 2) == 0)
    			sourceCount = sc;
    		createHandles();
    		return;
    	}
    	if (n == 1) {
    		separation = ei.value/sim.lengthScale;
    		positionHandles();
    		return;
    	}
        super.setEditValue(n-2, ei);
    }
    
	int getDumpType() { return 200; }

	String dump() { return super.dump() + " " + sourceCount; }
	
	String dumpHandles() {
		DragHandle dh0 = handles.get(0);
		DragHandle dh1 = handles.get(1);
		return " " + dh0.x + " " + dh0.y + " " + dh1.x + " " + dh1.y;
	}

	@Override void rescale(double scale) {
		handles.get(0).rescale(scale);
		positionHandles();
		setTransform();
	}
}
