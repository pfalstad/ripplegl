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

public class ModeBox extends RectDragObject {
	int xmode, ymode;
	boolean box, randomize;
	
	ModeBox() {
		xmode = 1;
		ymode = 1;
		box = true;
	}
	
	ModeBox(int x, int y, int x2, int y2, int xm, int ym) {
		box = true;
		xmode = xm;
		ymode = ym;
		topLeft.x = bottomLeft.x = x;
		topLeft.y = topRight.y = y;
		topRight.x = bottomRight.x = x2;
		bottomLeft.y = bottomRight.y = y2;
		setTransform();
	}

	int rand() {
		return Math.abs(sim.random.nextInt());
	}
	
	void drawMode() {
		if (randomize) {
			int m1x = rand() % xmode;
			int m1y = rand() % ymode;
			int m2x = rand() % xmode;
			int m2y = rand() % ymode;
			if (sim.waveChooser.getSelectedIndex() != RippleSim.WAVE_SOUND) {
				if (m1x == 0) m1x = 1;
				if (m1y == 0) m1y = 1;
				if (m2x == 0) m2x = 1;
				if (m2y == 0) m2y = 1;
			}
			RippleSim.drawModes(topLeft.x+1, topLeft.y+1, bottomRight.x-1, bottomRight.y-1,
					m1x*Math.PI, m1y*Math.PI, m2x*Math.PI, m2y*Math.PI);
		} else {
			RippleSim.drawModes(topLeft.x+1, topLeft.y+1, bottomRight.x-1, bottomRight.y-1,
				xmode*Math.PI, ymode*Math.PI, 0, 0);
		}
	}
	
	void prepare() {
		if (box) {
			RippleSim.drawWall(topLeft.x, topLeft.y, topRight.x, topRight.y);
			RippleSim.drawWall(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y); 
			RippleSim.drawWall(bottomRight.x, bottomRight.y, topRight.x, topRight.y);
			RippleSim.drawWall(bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y);
		}
		drawMode();
	}
	
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("x mode", xmode, 0, 1).
                setDimensionless();
        if (n == 1)
            return new EditInfo("y mode", ymode, 0, 1).
                setDimensionless();
        if (n == 2) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Randomize", randomize);
            return ei;
        }
        if (n == 3) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Draw Box", box);
            return ei;
        }
        
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
        	xmode = (int)ei.value;
        if (n == 1)
        	ymode = (int)ei.value;
        if (n == 2)
        	randomize = ei.checkbox.getState();
        if (n == 3)
        	box = ei.checkbox.getState();
    }

    ModeBox(StringTokenizer st) {
    	super(st);
    	xmode = new Integer(st.nextToken()).intValue();
    	ymode = new Integer(st.nextToken()).intValue();
    	randomize = new Boolean(st.nextToken()).booleanValue();
    	box = new Boolean(st.nextToken()).booleanValue();
    }
	String dump() { return super.dump() + " " + xmode + " " + ymode + " " + randomize + " " + box; }
	
	String selectText() { return super.selectText() + ", f = " +
			RippleSim.getUnitText(sim.waveSpeed/2 * Math.hypot(xmode/(sim.lengthScale*(width()-1)),
						ymode/(sim.lengthScale*(height()-1))), "Hz");
	}
	int getDumpType() { return 'M'; }
	
	boolean hitTestInside(double x, double y) {
		return false;
	}
}
