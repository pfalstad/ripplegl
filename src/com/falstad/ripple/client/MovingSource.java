package com.falstad.ripple.client;

public class MovingSource extends Source {
    int moveDuration, pauseDuration;
    int startIter;
    int phase;

    MovingSource() {
        handles.add(new DragHandle(this));
        moveDuration = 900;
        pauseDuration = 500;
        startIter = 0;
        phase = 0;
    }

    MovingSource(StringTokenizer st) {
    	super(st, 2);
    	moveDuration = new Integer(st.nextToken()).intValue();
    	pauseDuration = new Integer(st.nextToken()).intValue();
    }

    String dump() { return super.dump() + " " + moveDuration + " " + pauseDuration; }
    
    void run() {
        DragHandle dh1 = handles.get(0);
        DragHandle dh2 = handles.get(1);
        
        if (sim.iters < startIter)
        	startIter = sim.iters;
        
        double dur = (phase == 0 || phase == 2) ? moveDuration : pauseDuration;
        double step = (sim.iters-startIter)/dur;
        if (step > 1) {
                step = 1;
                startIter = sim.iters;
                phase++;
                if (phase >= 4)
                        phase = 0;
                step = 0;
        }
        if (phase == 1)
                step = 1;
        if (phase == 3)
                step = 0;
        if (phase == 2)
                step = 1-step;

        double v = getValue();
        double nstep = 1-step;
//        sim.console("movingsrc " + v + " " + nstep + " " + dh1.x + " " + dh1.y);
        if (enabled)
        	RippleSim.drawSource((int)(dh1.x*nstep + dh2.x*step),
        						 (int)(dh1.y*nstep + dh2.y*step), v); 
    }

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Move Duration (iters)", moveDuration, 0, 1).
            setDimensionless();
		if (n == 1)
			return new EditInfo("Pause Duration (iters)", pauseDuration, 0, 1).
					setDimensionless();
		return super.getEditInfo(n-2);
	}
	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
            moveDuration = (int)ei.value;
		if (n == 1)
            pauseDuration = (int)ei.value;
		super.setEditValue(n-2, ei);
	}
	
	@Override void drawSelection() {
		RippleSim.drawWall(handles.get(0).x, handles.get(0).y, handles.get(1).x, handles.get(1).y);
	}

	int getDumpType() { return 'd'; }

	String selectText() {
		String s = super.selectText();
		if (s == null)
			return null;
		return s + ", v = " + sim.getSpeedText(length()/moveDuration);
	}

}
