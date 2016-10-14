package com.falstad.ripple.client;

import com.google.gwt.user.client.ui.CheckBox;

class Checkbox extends CheckBox {
	public Checkbox(String s){
		super(s);
	}
	
	public Checkbox(String s, boolean b){
		super(s);
		this.setValue(b);
	}
	
	public boolean getState(){
		return this.getValue();
	}
	
	public void setState(boolean s){
		this.setValue(s);
	}
	
}
