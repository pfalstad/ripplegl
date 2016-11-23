package com.falstad.ripple.client;

public class Source extends DragObject {

	Source() {
		handles.add(new DragHandle(this));
	}
	
	double getValue() {
        double w = sim.freqBar.getValue() * (sim.t - sim.freqTimeZero)
                * RippleSim.freqMult;
        return Math.cos(w);
	}
	
	void run() {
		DragHandle dh = handles.get(0);
        double v = getValue();
		RippleSim.drawSource(dh.x, dh.y, v); 
	}
}
