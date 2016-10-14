package com.falstad.ripple.client;

import com.falstad.ripple.shared.FieldVerifier;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Applets implements EntryPoint {

	static RippleSim ripsim;
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		ripsim = new RippleSim();
		ripsim.init();

		    Window.addResizeHandler(new ResizeHandler() {
		    	 
	            public void onResize(ResizeEvent event)
	            {               
	            	ripsim.setCanvasSize();
//	            	ripsim.setiFrameHeight();
	            	ripsim.init();
	            	
	                	
	            }
	        });
//		    ripsim.updateRipple();
		
		
	}
}
