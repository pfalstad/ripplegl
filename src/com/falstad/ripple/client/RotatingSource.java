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

public class RotatingSource extends MultipoleSource {
    double anglePerIter;
    
	RotatingSource() {
	    super();
	    anglePerIter = .002;
	}
	
	RotatingSource(StringTokenizer st) {
	    super(st);
	    anglePerIter = Double.parseDouble(st.nextToken());
	}
	
	void run() {
	    super.run();
	    rotate(anglePerIter);
	}
    
	String dump() {
	    return super.dump() + " " + anglePerIter;
	}
	
    boolean canRotate() { return false; }
    
	int getDumpType() { return 204; }

	    public EditInfo getEditInfo(int n) {
	        if (n == 0)
	                return new EditInfo("Iters Per Rotation", 2*Math.PI/anglePerIter, 0, 1).setDimensionless();
	        return super.getEditInfo(n-1);
	    }
	    
	    public void setEditValue(int n, EditInfo ei) {
	        if (n == 0)
	            anglePerIter = 2*Math.PI/ei.value;
	        super.setEditValue(n-1, ei);
	    }

}
