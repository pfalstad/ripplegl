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

public class Source extends DragObject {
    static final int WF_SINE = 0;
    static final int WF_PULSE = 1;
    static final int WF_PACKET = 2;
    static final int WF_TRIANGLE = 3;
    static final int FLAG_STRENGTH = 2; 
    int waveform;
    double frequency, phaseShift, freqTimeZero;
    double strength;
    double length, delay, startDelay;
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
		strength = 1;
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
		strength = 1;
		if ((flags & FLAG_STRENGTH) != 0) {
			strength = new Double(st.nextToken()).doubleValue();
			startDelay = new Double(st.nextToken()).doubleValue();
		}
		setTransform();
	}
	
	Source(int x, int y) {
		handles.add(new DragHandle(this, x, y));
		frequency = .5;
		length = 10;
		delay = 100;
		strength = 1;
		setTransform();
	}
	
	String dump() {
		flags |= FLAG_STRENGTH;
		return super.dump() + " " + waveform + " " + frequency + " " + phaseShift + " " + length + " " +
					delay + " " + strength + " " + startDelay;
	}
	
	void setFrequency(double f) {
		double oldfreq = frequency;
		frequency = f;
		if (sim.useFreqTimeZero())
			freqTimeZero = sim.t-oldfreq*(sim.t-freqTimeZero)/frequency;
		else
			freqTimeZero = 0;
	}
	
    double triangleFunc(double x) {
    	final double pi = Math.PI;
    	
    	// match phase of default cosine
    	x += pi;
    	
    	x %= 2*pi;
        if (x < pi)
            return x*(2/pi)-1;
        return 1-(x-pi)*(2/pi);
    }

    boolean isSimpleWaveform() {
    	return waveform == WF_SINE || waveform == WF_TRIANGLE;
    }
    
	double getValue() {
		enabled = true;
		double t = sim.t-startDelay;
		if (t < 0)
			return 0;
		if (isSimpleWaveform()) {
			double freq = frequency; // sim.freqBar.getValue() * RippleSim.freqMult;
			double w = freq * (t - freqTimeZero) + phaseShift;
			if (waveform == WF_TRIANGLE)
				return triangleFunc(w)*strength;
			return Math.cos(w)*strength;
		}
		double w = t % (length + delay);
		double v = 1;
		if (w > length) {
			enabled = false;
			v = 0;
		}
		if (waveform == WF_PACKET && v > 0)
			v = Math.cos(frequency * sim.t) * Math.sin(w*Math.PI/length);
		return v * strength;
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
    		ei.choice.add("Triangle");
    		ei.choice.select(waveform);
    		return ei;
    	}
    	if (n == 1)
    		return new EditInfo("Strength", strength, 0, 1).setDimensionless();
    	if (n == 2)
    		return new EditInfo("Start Delay (s)", sim.timeToRealTime(startDelay), 0, 1).setNoCenti();
    	if (isSimpleWaveform()) {
    		if (n == 3)
    			return frequencyEditInfo = new EditInfo("Frequency (Hz)", getRealFrequency(), 4, 500);
    		if (n == 4)
    			return new EditInfo("Phase Offset (degrees)", phaseShift*180/Math.PI,
    					-180, 180).setDimensionless();
    	} else {
    		if (n == 3)
    			return new EditInfo("On Duration (s)", sim.timeToRealTime(length), 0, 0).setNoCenti();
    		if (n == 4)
    			return new EditInfo("Off Duration (s)", sim.timeToRealTime(delay), 0, 0).setNoCenti();
    		if (waveform == WF_PACKET && n == 5)
    			return frequencyEditInfo = new EditInfo("Frequency (Hz)", getRealFrequency(), 4, 500);
    	}
    	if ((isSimpleWaveform() && n == 5) || (waveform == WF_PACKET && n == 6))
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
    	if (n == 1)
    		strength = ei.value;
    	if (n == 2)
    		startDelay = sim.realTimeToTime(ei.value);
		if ((isSimpleWaveform() && n == 3) || (waveform == WF_PACKET && n == 5)) {
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
    	if (isSimpleWaveform()) {
    		if (n == 4)
    			phaseShift = ei.value*Math.PI/180;
    	} else {
    		if (n == 3)
    			length = sim.realTimeToTime(ei.value);
    		if (n == 4)
    			delay = sim.realTimeToTime(ei.value);
    	}
    	if ((isSimpleWaveform() && n == 5) || (waveform == WF_PACKET && n == 6)) {
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
		if (!isSimpleWaveform())
			return null;
		return RippleSim.getUnitText(getRealFrequency(), "Hz") + ", \u03bb = " +
			sim.getUnitText(getWavelength(), "m");
	}

}
