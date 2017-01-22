package com.falstad.ripple.client;

public class Source extends DragObject {
    static final int WF_SINE = 0;
    static final int WF_PULSE = 1;
    static final int WF_PACKET = 2;
    int waveform;
    double frequency, phaseShift, freqTimeZero;
    double length, delay;
    boolean enabled;
    EditInfo frequencyEditInfo, wavelengthEditInfo;
    
	Source() {
		handles.add(new DragHandle(this));
		int i;
		frequency = .5;
		
		// get freq from last source if any
		for (i = sim.dragObjects.size()-1; i >= 0; i--) {
			DragObject obj = sim.dragObjects.get(i);
			if (obj instanceof Source) {
				frequency = ((Source) obj).frequency;
				break;
			}
		}
		
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
	
	Source(int x, int y) {
		handles.add(new DragHandle(this, x, y));
		frequency = .5;
		length = 10;
		delay = 100;
		setTransform();
	}
	
	String dump() {
		return super.dump() + " " + waveform + " " + frequency + " " + phaseShift + " " + length + " " +
					delay;
	}
	
	void setFrequency(double f) {
		double oldfreq = frequency;
		frequency = f;
		if (sim.useFreqTimeZero())
			freqTimeZero = sim.t-oldfreq*(sim.t-freqTimeZero)/frequency;
		else
			freqTimeZero = 0;
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
	
	void draw() {
		int i;
		for (i = 0; i != handles.size(); i++) {
			DragHandle dh = handles.get(i);
			RippleSim.drawHandle(dh.x,  dh.y);
		}
		super.draw();
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
    			return frequencyEditInfo = new EditInfo("Frequency (Hz)", getRealFrequency(), 4, 500);
    		if (n == 2)
    			return new EditInfo("Phase Offset (degrees)", phaseShift*180/Math.PI,
    					-180, 180).setDimensionless();
    	} else {
    		if (n == 1)
    			return new EditInfo("Length", length, 0, 0);
    		if (n == 2)
    			return new EditInfo("Delay", delay, 0, 0);
    		if (waveform == WF_PACKET && n == 3)
    			return frequencyEditInfo = new EditInfo("Frequency (Hz)", getRealFrequency(), 4, 500);
    	}
    	if ((waveform == WF_SINE && n == 3) || (waveform == WF_PACKET && n == 4))
    		return wavelengthEditInfo = new EditInfo("Wavelength (m)", getWavelength(), 4, 500);
    	return null;
    }
    
    static final double freqScale = 92/3 * .5;
    
    public void setEditValue(int n, EditInfo ei) {
    	if (n == 0) {
    		int ow = waveform;
    		waveform = ei.choice.getSelectedIndex();
    		if (waveform != ow)
    			ei.newDialog = true;
    	}
		if ((waveform == WF_SINE && n == 1) || (waveform == WF_PACKET && n == 3)) {
			// adjust time zero to maintain continuity in the waveform
			// even though the frequency has changed.
			double oldfreq = frequency;
			double wavelength = sim.waveSpeed/ei.value;
    		frequency = freqScale * sim.lengthScale /wavelength;
    		enforceMaxFrequency();
    		if (sim.useFreqTimeZero())
    			freqTimeZero = sim.t-oldfreq*(sim.t-freqTimeZero)/frequency;
    		else
    			freqTimeZero = 0;
			wavelengthEditInfo.value = getWavelength();
			EditDialog.theEditDialog.updateValue(wavelengthEditInfo);
		}
    	if (waveform == WF_SINE) {
    		if (n == 2)
    			phaseShift = ei.value*Math.PI/180;
    	} else {
    		if (n == 1)
    			length = ei.value;
    		if (n == 2)
    			delay = ei.value;
    	}
    	if ((waveform == WF_SINE && n == 3) || (waveform == WF_PACKET && n == 4)) {
    		// set wavelength
    		frequency = freqScale * sim.lengthScale /ei.value;
    		enforceMaxFrequency();
    		frequencyEditInfo.value = getRealFrequency();
			EditDialog.theEditDialog.updateValue(frequencyEditInfo);
    	}
    }

    void enforceMaxFrequency() {
    	// enforce minimum wavelength of 6 pixels
    	double maxfreq = freqScale/6;
    	if (frequency <= maxfreq)
    		return;
    	frequency = maxfreq;
		frequencyEditInfo.value = getRealFrequency();
		EditDialog.theEditDialog.updateValue(frequencyEditInfo);
		wavelengthEditInfo.value = getWavelength();
		EditDialog.theEditDialog.updateValue(wavelengthEditInfo);
}
    
    double getWavelength() {
    	return (freqScale/frequency) * sim.lengthScale;
    }
    
    double getRealFrequency() {
    	return sim.waveSpeed/getWavelength();
    }
    
	int getDumpType() { return 's'; }
	
	String selectText() {
		if (waveform != WF_SINE)
			return null;
		return RippleSim.getUnitText(getRealFrequency(), "Hz") + " \u03bb = " +
			sim.getUnitText(getWavelength(), "m");
	}

}
