package com.falstad.ripple.client;

import com.google.gwt.user.client.ui.ListBox;

public class Choice extends ListBox {
	
	Choice() {
		super();
	}
	
	public void add(String s){
		this.addItem(s);
	}
	
	public void select(int i){
		this.setSelectedIndex(i);
	}
}
