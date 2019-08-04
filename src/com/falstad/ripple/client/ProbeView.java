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

import java.util.Date;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.i18n.client.DateTimeFormat;

public class ProbeView extends WindowBox implements ContextMenuHandler {
	
	Canvas cv;
	double values[], maxValue;
	int offset;
	Font font;
	Color color;
	MenuBar popupMenu;
	MenuItem startRecordingItem, stopRecordingItem, saveRecordingItem;
	boolean recording;
	StringBuffer recordingBuffer;
	
	public ProbeView(int n, Color col, boolean showing) {
		super(false, false, true, true);
		Button okButton;
		Anchor a;
		String url;
		
		cv = Canvas.createIfSupported();
		setWidget(cv);
		cv.setWidth("400px");
		cv.setHeight("100px");
		values = new double[100];
		maxValue = 1e-8;
		font = new Font("SansSerif", 0, 16);
		color = col;
		
		setText("Probe View " + (n+1));
		
		createPopupMenu();
		cv.addDomHandler(this, ContextMenuEvent.getType());
		
		/*
		vp=new VerticalPanel();
		setWidget(vp);

		vp.add(new Label("Click on the link below to save your layout"));
		vp.add(okButton = new Button("OK"));
		okButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				closeDialog();
			}
		});
		*/
		
//		this.center();
		int winwidth = (int) RootLayoutPanel.get().getOffsetWidth();
		int winheight = (int) RootLayoutPanel.get().getOffsetHeight();
		int x = winwidth-450-n*20;
		int y = winheight-150-n*20;
		if (150*n < winheight/2) {
			x = winwidth-450;
			y = winheight-150*(n+1);
		}
		setPopupPosition(x, y);
		setVisible(showing);
		show();
	}
	
	void createPopupMenu() {
        popupMenu = new MenuBar(true);
        popupMenu.addItem(startRecordingItem = new MenuItem("Start Recording", new ProbeCommand("start")));
        popupMenu.addItem(stopRecordingItem = new MenuItem("Stop Recording", new ProbeCommand("stop")));
        popupMenu.addItem(saveRecordingItem = new MenuItem("Save Recording...", new ProbeCommand("save")));
	}
	
	void draw() {
		Context2d context = cv.getContext2d();
		Graphics g = new Graphics(context);
	    final int width = cv.getElement().getClientWidth();
	    final int height = cv.getElement().getClientHeight();
        cv.setCoordinateSpaceWidth(width);
        if (width != values.length) {
        	values = new double[width];
        	offset = 0;
        	maxValue = 1e-8;
        }
        cv.setCoordinateSpaceHeight(height);
		g.setColor(Color.white);
        g.fillRect(0, 0, g.context.getCanvas().getWidth(), g.context.getCanvas().getHeight());
		g.setColor(color);
		context.beginPath();
		int i;
		context.moveTo(-10, height/2);
		for (i = 0; i != values.length; i++) {
			context.lineTo(i, height*(.5+.47*values[(i+offset) % values.length]/maxValue));
		}
		context.stroke();
		drawFrequency(g);
	}
	
	void recordValue(double x) {
		values[offset++] = x;
//		maxValue = Math.max(Math.abs(x), maxValue*.999);
		if (offset >= values.length)
			offset = 0;
		if (recording) {
			recordingBuffer.append(String.valueOf(x*1000));
			recordingBuffer.append('\n');
		}
	}
	
	void reset() {
		values = new double[values.length];
    	offset = 0;
    	maxValue = 1e-8;
	}
	
	protected void closeDialog()
	{
		setVisible(false);
//		this.hide();
	}

	protected void onCloseClick(ClickEvent event) {
		setVisible(false);
	}

    void drawFrequency(Graphics g) {
        // try to get frequency
        // get average
        double avg = 0;
        int i;
        maxValue = 1e-8;
        for (i = 0; i != values.length; i++) {
        	double v = values[(i+offset) % values.length];
            avg += v;
    		maxValue = Math.max(Math.abs(v), maxValue);
        }
        avg /= i*2;
        int state = 0;
        double thresh = avg*.05;
        int oi = 0;
        double avperiod = 0;
        int periodct = -1;
        double avperiod2 = 0;
        // count period lengths
        for (i = 0; i != values.length; i++) {
            double q = values[(i+offset) % values.length]-avg;
            
            int os = state;
            if (q < thresh)
                state = 1;
            else if (q > -thresh)
                state = 2;
            if (state == 2 && os == 1) {
                int pd = i-oi;
                oi = i;
                // short periods can't be counted properly
                if (pd < 12)
                    continue;
                // skip first period, it might be too short
                if (periodct >= 0) {
                    avperiod += pd;
                    avperiod2 += pd*pd;
                }
                periodct++;
            }
        }
        avperiod /= periodct;
        avperiod2 /= periodct;
        double periodstd = Math.sqrt(avperiod2-avperiod*avperiod);
        double freq = 1/(avperiod*RippleSim.theSim.timeToRealTime(RippleSim.timeStep));
        // don't show freq if standard deviation is too great
        if (periodct < 1 || periodstd > 20)
            freq = 0;
//         RippleSim.console(freq + " " + periodstd + " " + periodct);
         g.setFont(font);
         g.drawString(RippleSim.showFormat.format(maxValue*1000), 2, 13);
         if (freq != 0)
            g.drawString(RippleSim.getUnitText(freq, "Hz"), 2, 33);
    }
    
    PopupPanel contextPanel = null;

    public void onContextMenu(ContextMenuEvent e) {
    	startRecordingItem.setEnabled(!recording);
    	stopRecordingItem.setEnabled(recording);
    	saveRecordingItem.setEnabled(recordingBuffer != null);
    	e.preventDefault();
    	int menuX = e.getNativeEvent().getClientX() - getPopupLeft();
    	int menuY = e.getNativeEvent().getClientY() - getPopupTop();
        contextPanel=new PopupPanel(true);
        contextPanel.add(popupMenu);
        int x=Math.max(0, Math.min(menuX, cv.getCoordinateSpaceWidth()-50));
        int y=Math.max(0, Math.min(menuY,cv.getCoordinateSpaceHeight()-50));
        contextPanel.setPopupPosition(x + getPopupLeft(),y + getPopupTop());
        contextPanel.show();
    }
    
    class ProbeCommand implements Command {
    	String name;
    	public ProbeCommand(String n) {
    		name = n;
    	}
    	public void execute() {
    		menuPerformed(name);
    	}
    }
    
    void menuPerformed(String name) {
    	if (contextPanel != null)
    		contextPanel.hide();
    	if (name == "start") {
    		recording = true;
    		recordingBuffer = new StringBuffer();
    	}
    	if (name == "stop")
    		recording = false;
    	if (name == "save") {
    		DialogBox dlg = new SaveRecordingBufferDialog(recordingBuffer.toString());
    		dlg.show();
    	}
    }
}

class SaveRecordingBufferDialog extends DialogBox {
	
    	VerticalPanel vp;
	
    	static public final native boolean downloadIsSupported() 
	/*-{
		return !!(("download" in $doc.createElement("a")));
	 }-*/;
	
	static public final native String getBlobUrl(String data) 
	/*-{
		var datain=[""];
		datain[0]=data;
		var oldblob = $doc.exportBlob;
		if (oldblob)
		    URL.revokeObjectURL(oldblob);
		var blob=new Blob(datain, {type: 'text/plain' } );
		var url = URL.createObjectURL(blob);
		$doc.exportBlob = url;
		return url;
	}-*/;
	
	public SaveRecordingBufferDialog(String data) {
		super();
		Button okButton;
		Anchor a;
		String url;
		vp=new VerticalPanel();
		setWidget(vp);
		setText("Export as Local File");
		vp.add(new Label("Click on the link below to save your data"));
		url=getBlobUrl(data);
		Date date = new Date();
		DateTimeFormat dtf = DateTimeFormat.getFormat("yyyyMMdd-HHmm");
		String fname = "ripple-"+ dtf.format(date) + ".txt";
		a=new Anchor(fname, url);
		a.getElement().setAttribute("Download", fname);
		vp.add(a);
		vp.add(okButton = new Button("OK"));
		okButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				closeDialog();
			}
		});
		this.center();
	}
	
	protected void closeDialog()
	{
		this.hide();
	}

}
