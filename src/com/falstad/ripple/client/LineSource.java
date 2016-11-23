package com.falstad.ripple.client;

public class LineSource extends Source {

	LineSource() {
		handles.add(new DragHandle(this));
	}
	
	void run() {
		DragHandle dh1 = handles.get(0);
		DragHandle dh2 = handles.get(1);
        double v = getValue();
		RippleSim.drawLineSource(dh1.x, dh1.y, dh2.x, dh2.y, v); 
	}

}
