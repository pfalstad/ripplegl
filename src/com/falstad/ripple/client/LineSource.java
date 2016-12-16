package com.falstad.ripple.client;

public class LineSource extends Source {

	LineSource() {
		handles.add(new DragHandle(this));
		setTransform();
	}
	LineSource(StringTokenizer st) {
		super(st, 2);
		setTransform();
	}
	
	void run() {
		DragHandle dh1 = handles.get(0);
		DragHandle dh2 = handles.get(1);
        double v = getValue();
        if (enabled)
        	RippleSim.drawLineSource(dh1.x, dh1.y, dh2.x, dh2.y, v); 
	}

	@Override void drawSelection() {
		RippleSim.drawWall(handles.get(0).x, handles.get(0).y, handles.get(1).x, handles.get(1).y);
	}

	int getDumpType() { return 'S'; }

}
