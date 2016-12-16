package com.falstad.ripple.client;

public class Source extends DragObject {
    static final int WF_SINE = 0;
    static final int WF_PULSE = 1;
    static final int WF_PACKET = 2;
    int waveform;
    double frequency, phaseShift, freqTimeZero;
    double length, delay;
    boolean enabled;
    
	Source() {
		handles.add(new DragHandle(this));
		frequency = .5;
		length = 10;
		delay = 100;
		setTransform();
	}
	
	Source(StringTokenizer st, int ct) {
		super(st);
		while (ct-- > 0)
			handles.add(new DragHandle(this, st));
		waveform = new Integer(st.nextToken()).intValue();
		frequency = new Double(st.nextToken()).doubleValue();
		phaseShift = new Double(st.nextToken()).doubleValue();
		length = new Double(st.nextToken()).doubleValue();
		delay = new Double(st.nextToken()).doubleValue();
		setTransform();
	}
	
	String dump() {
		return super.dump() + " " + waveform + " " + frequency + " " + phaseShift + " " + length + " " +
					delay;
	}
	
	double getValue() {
		enabled = true;
		if (waveform == WF_SINE) {
			double freq = frequency; // sim.freqBar.getValue() * RippleSim.freqMult;
			double w = freq * (sim.t - freqTimeZero) + phaseShift;
			return Math.cos(w);
		}
		double w = sim.t % (length + delay);
		double v = 1;
		if (w > length) {
			enabled = false;
			v = 0;
		}
		if (waveform == WF_PACKET && v > 0)
			v = Math.cos(frequency * sim.t) * Math.sin(w*Math.PI/length);
		return v;
	}
	
	void run() {
		DragHandle dh = handles.get(0);
        double v = getValue();
        if (enabled)
        	RippleSim.drawSource(dh.x, dh.y, v); 
	}
	
    public EditInfo getEditInfo(int n) {
    	if (n == 0) {
    		EditInfo ei =  new EditInfo("Waveform", waveform, -1, -1);
    		ei.choice = new Choice();
    		ei.choice.add("Sine");
    		ei.choice.add("Pulse");
    		ei.choice.add("Packet");
    		ei.choice.select(waveform);
    		return ei;
    	}
    	if (waveform == WF_SINE) {
    		if (n == 1)
    			return new EditInfo("Frequency (Hz)", frequency, 4, 500);
    		if (n == 2)
    			return new EditInfo("Phase Offset (degrees)", phaseShift*180/Math.PI,
    					-180, 180).setDimensionless();
    	} else {
    		if (n == 1)
    			return new EditInfo("Length", length, 0, 0);
    		if (n == 2)
    			return new EditInfo("Delay", delay, 0, 0);
    		if (waveform == WF_PACKET && n == 3)
    			return new EditInfo("Frequency (Hz)", frequency, 4, 500);
    	}
    	return null;
    }
    public void setEditValue(int n, EditInfo ei) {
    	if (n == 0) {
    		int ow = waveform;
    		waveform = ei.choice.getSelectedIndex();
    		if (waveform != ow)
    			ei.newDialog = true;
    	}
    	if (waveform == WF_SINE) {
    		if (n == 1) {
    			// adjust time zero to maintain continuity in the waveform
    			// even though the frequency has changed.
    			double oldfreq = frequency;
    			frequency = ei.value;
    			freqTimeZero = sim.t-oldfreq*(sim.t-freqTimeZero)/frequency;
    		}
    		if (n == 2)
    			phaseShift = ei.value*Math.PI/180;
    	} else {
    		if (n == 1)
    			length = ei.value;
    		if (n == 2)
    			delay = ei.value;
    		if (n == 3)
    			frequency = ei.value;
    	}
    }
    
	int getDumpType() { return 's'; }

}
