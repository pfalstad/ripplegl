package com.falstad.ripple.client;

public class EditOptions implements Editable {
	RippleSim sim;
    public EditOptions(RippleSim s) { sim = s; }
    EditInfo offsetEditInfo;
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Grid size", sim.windowWidth, 0, 0).setDimensionless();
        if (n == 1)
            return offsetEditInfo = new EditInfo("Absorbing area width", sim.windowOffsetX, 0, 0).setDimensionless();
        return null;
    }
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0 && ei.value > 0) {
        	sim.setResolution((int)ei.value);
        	offsetEditInfo.value = sim.windowOffsetX;
        	EditDialog.theEditDialog.updateValue(offsetEditInfo);
        }
        if (n == 1 && ei.value > 0)
        	sim.setResolution(sim.windowWidth, (int)ei.value);
    }

}
