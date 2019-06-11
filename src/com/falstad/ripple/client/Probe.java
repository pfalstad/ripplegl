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

public class Probe extends DragObject {
	ProbeView probeView;
	int num;
	int colorIndex;
	DragHandle handle;
	static final int FLAG_OPEN = 1;
	
	static final double colors[] = {
			1,0,0,
			0,.7,0,
			0,0,1,
			0,0,0,
			.7,.7,0,
			0,.7,.7,
			1,0,1,
			1,.5,0
	};
	
	Probe() {
		handles.add(handle = new DragHandle(this));
		flags |= FLAG_OPEN;
		makeView();
		setTransform();
	}
	
	Probe(StringTokenizer st) {
		super(st);
		handles.add(handle = new DragHandle(this, st));
		makeView();
		setTransform();
	}
	
	String dump() {
		flags &= ~FLAG_OPEN;
		if (probeView.isVisible())
			flags |= FLAG_OPEN;
		return super.dump();
	}
	
	void makeView() {
		int i;
		num = 0;
		for (i = 0; i != sim.dragObjects.size(); i++) {
			DragObject obj = sim.dragObjects.get(i);
			if (obj != this && obj instanceof Probe)
				num++;
		}
		colorIndex = (num % 8) * 3;
		probeView = new ProbeView(num,
				new Color(colors[colorIndex], colors[colorIndex+1], colors[colorIndex+2]),
				(flags & FLAG_OPEN) != 0);
	}
	
	void run() {
		double v = RippleSim.getProbeValue(handle.x, handle.y);
		probeView.recordValue(v);
	}
	
	void reset() {
		probeView.reset();
	}
	
	void rescale(double x) {
		super.rescale(x);;
		probeView.reset();
	}
	
	void draw() {
		RippleSim.drawHandle(handle.x, handle.y);
        RippleSim.setDrawingSelection(1);
		RippleSim.setDrawingColor(colors[colorIndex], colors[colorIndex+1], colors[colorIndex+2], 1);
		RippleSim.drawEllipse(handle.x, handle.y, 8, 8);
		probeView.draw();
		super.draw();
	}
	
	void mouseDown() {
		probeView.setVisible(true);
	}
	
	int getDumpType() { return 'P'; }
	
	void delete() {
		probeView.hide();
	}
	
	String selectText() {
		return "Probe " + (num+1);
	}
}
