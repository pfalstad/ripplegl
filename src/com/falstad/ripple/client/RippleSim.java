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

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.javascript.host.Console;
import com.gargoylesoftware.htmlunit.javascript.host.Navigator;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RippleSim implements MouseDownHandler, MouseMoveHandler,
		MouseUpHandler, ClickHandler, DoubleClickHandler, ContextMenuHandler,
		NativePreviewHandler, MouseOutHandler, MouseWheelHandler, ChangeHandler {

	Logger logger = Logger.getLogger(RippleSim.class.getName());
	
	Image dbimage;
	ImageData data;
	Dimension winSize;
	Random random;
	int gridSizeX;
	int gridSizeY;
	int gridSizeXY;
	int windowWidth = 50;
	int windowHeight = 50;
	int windowOffsetX = 0;
	int windowOffsetY = 0;
	int windowBottom = 0;
	int windowRight = 0;
	public static final int sourceRadius = 17;
	public static final double freqMult = .0233333 * 5;

	public String getAppletInfo() {
		return "Ripple by Paul Falstad";
	}

	// Container main;
	Button blankButton;
	Button blankWallsButton;
	Button borderButton;
	Button boxButton;
	Button exportButton;
	Checkbox stoppedCheck;
	Checkbox view3dCheck;
	Choice setupChooser;
	Choice colorChooser;
	Choice waveChooser;
	Vector<Setup> setupList;
	Vector<DragObject> dragObjects;
	DragObject selectedObject, mouseObject, menuObject;
	DragHandle draggingHandle;
	Setup setup;
	Scrollbar dampingBar;
	Scrollbar speedBar;
	Scrollbar freqBar;
	Scrollbar resBar;
	Scrollbar brightnessBar;
	double dampcoef;
	double freqTimeZero;
	double movingSourcePos = 0;
	double brightMult = 1;
	double zoom3d = 1.2;
	Point mouseLocation;
	static final double pi = 3.14159265358979323846;
	static final int MODE_SETFUNC = 0;
	static final int MODE_WALLS = 1;
	static final int MODE_MEDIUM = 2;
	static final int MODE_FUNCHOLD = 3;
	static final int WAVE_SOUND = 0;
	int dragX, dragY, dragStartX = -1, dragStartY;
	int selectedSource = -1;
	int sourceIndex;
	int freqBarValue;
	boolean dragging;
	boolean dragStop;
	public boolean useFrame;
	boolean showControls;
	boolean changedWalls;
	boolean ignoreFreqBarSetting;
	double t;
	double lengthScale, waveSpeed;
	int iters;
	// MemoryImageSource imageSource;
	CanvasPixelArray pixels;
	int sourceCount = -1;
	boolean sourcePlane = false;
	boolean sourceMoving = false;
	boolean increaseResolution = false;
	boolean adjustResolution = true;
	boolean rotationMode = false;
	boolean preserveSelection = false;
	int sourceFreqCount = -1;
	int sourceWaveform = SWF_SIN;
	int auxFunction;
	int mouseWheelAccum;
	long startTime;
	MenuBar menuBar;
	MenuBar mainMenuBar;
	MenuBar fileMenuBar;
	MenuBar elmMenuBar;
    MenuItem elmEditMenuItem;
    MenuItem elmCutMenuItem;
    MenuItem elmCopyMenuItem;
    MenuItem elmDeleteMenuItem;
    MenuItem elmRotateMenuItem;
    
    MenuItem aboutItem;
    MenuItem importFromLocalFileItem, importFromTextItem,
    	exportAsUrlItem, exportAsLocalFileItem, exportAsTextItem;
    MenuItem undoItem, redoItem,
	cutItem, copyItem, pasteItem, selectAllItem, optionsItem;

	Color wallColor, posColor, negColor, zeroColor, medColor, posMedColor,
			negMedColor, sourceColor;
	Color schemeColors[][];
	Point dragPoint, pokePoint;
	double pokeValue;
	// Method timerMethod;
	int timerDiv;
	static final int mediumMax = 191;
	static final double mediumMaxIndex = .5;
	static final int SWF_SIN = 0;
	static final int SWF_SQUARE = 1;
	static final int SWF_PULSE = 2;
	
	static final double timeStep = .25;

//	Frame iFrame;
    LoadFile loadFileInput;
	DockLayoutPanel layoutPanel;
	VerticalPanel verticalPanel;
	AbsolutePanel absolutePanel;
	Rectangle ripArea;
    String clipboard;
    Vector<String> undoStack, redoStack;
	Canvas cv;
	Context2d cvcontext;
	Canvas backcv;
	Label coordsLabel;
	Context2d backcontext;
	HandlerRegistration handler;
	DialogBox dialogBox;
	int verticalPanelWidth;
	String startLayoutText = null;
	String versionString = "1.2";
    public static NumberFormat showFormat, shortFormat, noCommaFormat;
	static RippleSim theSim;
    static EditDialog editDialog;
    static ExportAsUrlDialog exportAsUrlDialog;
    static ExportAsTextDialog exportAsTextDialog;
    static ExportAsLocalFileDialog exportAsLocalFileDialog;
    static ImportFromTextDialog importFromTextDialog;
    static AboutBox aboutBox;

	static final int MENUBARHEIGHT = 30;
	static final int MAXVERTICALPANELWIDTH = 166;
	static final int POSTGRABSQ = 16;

	final Timer timer = new Timer() {
		public void run() {
			
			updateRipple();
		}
	};
	final int FASTTIMER = 33; // 16;

	int getrand(int x) {
		int q = random.nextInt();
		if (q < 0)
			q = -q;
		return q % x;
	}

	public void setCanvasSize() {
		int width, height;
		int fullwidth = width = (int) RootLayoutPanel.get().getOffsetWidth();
		height = (int) RootLayoutPanel.get().getOffsetHeight();
		height = height - MENUBARHEIGHT;   // put this back in if we add a menu bar
		width = width - MAXVERTICALPANELWIDTH;
		width = height = (width < height) ? width : height;
		winSize = new Dimension(width, height);
		verticalPanelWidth = fullwidth-width;
		if (layoutPanel != null)
			layoutPanel.setWidgetSize(verticalPanel, verticalPanelWidth);
		if (resBar != null) {
			resBar.setWidth(verticalPanelWidth);
			dampingBar.setWidth(verticalPanelWidth);
			speedBar.setWidth(verticalPanelWidth);
			freqBar.setWidth(verticalPanelWidth);
			brightnessBar.setWidth(verticalPanelWidth);
		}
		if (cv != null) {
			cv.setWidth(width + "PX");
			cv.setHeight(height + "PX");
			cv.setCoordinateSpaceWidth(width);
			cv.setCoordinateSpaceHeight(height);
		}
		if (coordsLabel != null)
			absolutePanel.setWidgetPosition(coordsLabel, 0, height-coordsLabel.getOffsetHeight());
		/*
		if (backcv != null) {
			backcv.setWidth(width + "PX");
			backcv.setHeight(height + "PX");
			backcv.setCoordinateSpaceWidth(width);
			backcv.setCoordinateSpaceHeight(height);
		}
		*/
		int h = height / 5;
		/*
		 * if (h < 128 && winSize.height > 300) h = 128;
		 */
		ripArea = new Rectangle(0, 0, width, height - h);

	}

    public static native void console(String text)
    /*-{
	    console.log(text);
	}-*/;

    // pass the canvas element to ripple.js and install all the callbacks we need into "this" 
	static native void passCanvas(CanvasElement cv) /*-{
		$doc.passCanvas(cv, this);
	}-*/;

	// call into ripple.js
	static native void updateRippleGL(double bright, boolean threed) /*-{
		if (threed)
			this.updateRipple3D(bright);
		else
			this.updateRipple(bright);
	}-*/;

	static native void simulate() /*-{
		this.simulate();
	}-*/;

	static native void setAcoustic(boolean ac) /*-{
		this.acoustic = ac;
	}-*/;

	static native void set3dViewAngle(double angle1, double angle2) /*-{
		this.set3dViewAngle(angle1, angle2);
	}-*/;

	static native void set3dViewZoom(double zoom) /*-{
		this.set3dViewZoom(zoom);
	}-*/;

	static native void setResolutionGL(int x, int y, int wx, int wy) /*-{
		this.setResolution(x, y, wx, wy);
	}-*/;
	
	static native void drawSource(int x, int y, double value) /*-{
		this.drawSource(x, y, value);
	}-*/;

	static native void drawHandle(int x, int y) /*-{
		this.drawHandle(x, y);
	}-*/;

	static native void drawFocus(int x, int y) /*-{
		this.drawFocus(x, y);
	}-*/;

	static native void drawPoke(int x, int y, double value) /*-{
		this.drawPoke(x, y, value);
	}-*/;

	static native void drawLineSource(int x1, int y1, int x2, int y2, double value, boolean gauss) /*-{
		this.drawLineSource(x1, y1, x2, y2, value, gauss);
	}-*/;

	static native void drawPhasedArray(int x1, int y1, int x2, int y2, double w1, double w2) /*-{
		this.drawPhasedArray(x1, y1, x2, y2, w1, w2);
	}-*/;

	static native void drawWall(int x1, int y1, int x2, int y2) /*-{
		this.drawWall(x1, y1, x2, y2);
	}-*/;

	static native double getProbeValue(int x, int y) /*-{
		return this.getProbeValue(x, y);
	}-*/;

	
	static native void clearWall(int x1, int y1, int x2, int y2) /*-{
		this.clearWall(x1, y1, x2, y2);
	}-*/;

	static native void drawEllipse(int x1, int y1, int rx, int ry) /*-{
		this.drawEllipse(x1, y1, rx, ry);
	}-*/;

	static native void drawParabola(int x1, int y1, int w, int h) /*-{
		this.drawParabola(x1, y1, w, h);
	}-*/;

	static native void drawLens(int x1, int y1, int w, int h, double med) /*-{
		this.drawLens(x1, y1, w, h, med);
	}-*/;

	static native void setDrawingSelection(double ds) /*-{
		this.drawingSelection = ds;
	}-*/;

	static native void setDrawingColor(double r, double g, double b, double a) /*-{
		this.drawingColor = [ r, g, b, a ];
	}-*/;

	
	static native void setTransform(double a, double b, double c, double d, double e, double f) /*-{
		this.setTransform(a, b, c, d, e, f);
	}-*/;

	static native void drawSolidEllipse(int x1, int y1, int rx, int ry, double med) /*-{
		this.drawSolidEllipse(x1, y1, rx, ry, med);
	}-*/;

	static native void drawMedium(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, double med, double med2) /*-{
		this.drawMedium(x1, y1, x2, y2, x3, y3, x4, y4, med, med2);
	}-*/;
	
	static native void drawModes(int x1, int y1, int x2, int y2, double a, double b, double c, double d) /*-{
		this.drawModes(x1, y1, x2, y2, a, b, c, d);
	}-*/;

	static native void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, double med) /*-{
		this.drawTriangle(x1, y1, x2, y2, x3, y3, med);
	}-*/;

	static native void doBlank() /*-{
		this.doBlank();
	}-*/;

	static native void doBlankWalls() /*-{
		this.doBlankWalls();
	}-*/;

	static native boolean readPixelsWorks() /*-{
		return this.readPixelsWorks;
	}-*/;
	
	static native void setColors(int wallColor, int posColor, int negColor,
			int zeroColor, int posMedColor, int negMedColor,
			int medColor, int sourceColor, int zeroColor3d) /*-{
		this.setColors(wallColor, posColor, negColor, zeroColor, posMedColor, negMedColor,
			medColor, sourceColor, zeroColor3d);
	}-*/;

	static native void setDamping(double d) /*-{
		this.setDamping(d);
	}-*/;
	
	Frame iFrame;
	
	public void init() {
		theSim = this;
//		logger.log(Level.SEVERE, "RAwr");
		
        QueryParameters qp = new QueryParameters();
        
        try {
                // look for layout embedded in URL
                String cct=qp.getValue("rol");
                if (cct!=null)
                	startLayoutText = cct.replace("%24", "$");
        } catch (Exception e) { }

		cv = Canvas.createIfSupported();
		passCanvas(cv.getCanvasElement());
		if (cv == null) {
			RootPanel
					.get()
					.add(new Label(
							"Not working. You need a browser that supports the CANVAS element."));
			return;
		}

		dragObjects = new Vector<DragObject>();
		cvcontext = cv.getContext2d();
//		backcv = Canvas.createIfSupported();
//		backcontext = backcv.getContext2d();
		setCanvasSize();
		layoutPanel = new DockLayoutPanel(Unit.PX);
		verticalPanel = new VerticalPanel();
		
		setupList = new Vector<Setup>();
        undoStack = new Vector<String>();
        redoStack = new Vector<String>();

		setupChooser = new Choice();
		setupChooser.addChangeHandler(this);
//		setupChooser.addItemListener(this);
		getSetupList();
		
		colorChooser = new Choice();
		colorChooser.addChangeHandler(this);
        colorChooser.addStyleName("topSpace");

		waveChooser = new Choice();
		waveChooser.add("Waves = Sound");
		waveChooser.add("Waves = Visible Light/IR/UV");
		waveChooser.add("Waves = AM Radio");
		waveChooser.add("Waves = FM Radio");
		waveChooser.add("Waves = Microwave");
		waveChooser.addChangeHandler(this);
        waveChooser.addStyleName("topSpace");

		
		verticalPanel.add(setupChooser);
//		verticalPanel.add(sourceChooser);
//		verticalPanel.add(modeChooser);
		verticalPanel.add(waveChooser);
		verticalPanel.add(colorChooser);
		verticalPanel.add(blankButton = new Button("Clear Waves"));
		blankButton.addClickHandler(this);

		verticalPanel.add(stoppedCheck = new Checkbox("Stopped"));
		verticalPanel.add(view3dCheck = new Checkbox("3-D View"));

        if (LoadFile.isSupported())
            verticalPanel.add(loadFileInput = new LoadFile(this));

		int res = 512;
		Label l;
		verticalPanel.add(l = new Label("Simulation Speed"));
        l.addStyleName("topSpace");
		verticalPanel.add(speedBar = new Scrollbar(Scrollbar.HORIZONTAL, 4, 1, 1, 40));
		verticalPanel.add(l = new Label("Resolution"));
        l.addStyleName("topSpace");
		verticalPanel.add(resBar = new Scrollbar(Scrollbar.HORIZONTAL, res, 5, 384, 1024));
		resBar.addClickHandler(this);
		setResolution();
		verticalPanel.add(l = new Label("Damping"));
        l.addStyleName("topSpace");
		verticalPanel.add(
				dampingBar = new Scrollbar(Scrollbar.HORIZONTAL, 0, 0, 0, 100, new Command() {
					public void execute() { setDamping(); }
				}
		));
		verticalPanel.add(l = new Label("Source Frequency"));
        l.addStyleName("topSpace");
		verticalPanel.add(freqBar = new Scrollbar(Scrollbar.HORIZONTAL, freqBarValue = 15, 1, 1, 30,
				new Command() {
			public void execute() { if (!ignoreFreqBarSetting) setFreq(); }
		}));
//		freqBar.addClickHandler(this);
		verticalPanel.add(l = new Label("Brightness"));
        l.addStyleName("topSpace");
		verticalPanel.add(brightnessBar = new Scrollbar(Scrollbar.HORIZONTAL, 27, 1, 1, 1200));
		
        verticalPanel.add(iFrame = new Frame("iframe.html"));
        iFrame.setWidth(verticalPanelWidth+"px");
        iFrame.setHeight("100 px");
        iFrame.getElement().setAttribute("scrolling", "no");

		
        l.addStyleName("topSpace");
		resBar.setWidth(verticalPanelWidth);
		dampingBar.setWidth(verticalPanelWidth);
		speedBar.setWidth(verticalPanelWidth);
		freqBar.setWidth(verticalPanelWidth);
		brightnessBar.setWidth(verticalPanelWidth);

		absolutePanel = new AbsolutePanel();
		coordsLabel = new Label("(0,0)");
		coordsLabel.setStyleName("coordsLabel");
		absolutePanel.add(cv);
		absolutePanel.add(coordsLabel, 0, 0);
		
		createMenus();
        layoutPanel.addNorth(menuBar, MENUBARHEIGHT);
		layoutPanel.addEast(verticalPanel, verticalPanelWidth);
		layoutPanel.add(absolutePanel);
		RootLayoutPanel.get().add(layoutPanel);

		mainMenuBar = new MenuBar(true);
		mainMenuBar.setAutoOpen(true);
		composeMainMenu(mainMenuBar);
		
        elmMenuBar = new MenuBar(true);
        elmMenuBar.addItem(elmEditMenuItem = new MenuItem("Edit",new MyCommand("elm","edit")));
        elmMenuBar.addItem(elmCutMenuItem = new MenuItem("Cut",new MyCommand("elm","cut")));
        elmMenuBar.addItem(elmCopyMenuItem = new MenuItem("Copy",new MyCommand("elm","copy")));
        elmMenuBar.addItem(elmDeleteMenuItem = new MenuItem("Delete",new MyCommand("elm","delete")));
        elmMenuBar.addItem(elmRotateMenuItem = new MenuItem("Rotate",new MyCommand("elm","rotate")));
        elmMenuBar.addItem(                    new MenuItem("Duplicate",new MyCommand("elm","duplicate")));

//		winSize = new Dimension(256, 256);
//		if (pixels == null) {
//		    pixels = new int[winSize.width*winSize.height];
//		    int j;
//		    for (j = 0; j != winSize.width*winSize.height; j++)
//		    	pixels[j] = 0xFF000000;
//		    
//		    
//		}
		
        showFormat=NumberFormat.getFormat("####.##");
        shortFormat=NumberFormat.getFormat("####.#");
        
		schemeColors = new Color[20][8];
		if (colorChooser.getItemCount() == 0)
		    addDefaultColorScheme();
		doColor();
		setWaveType();
		setDamping();
//		setup = (Setup) setupList.elementAt(setupChooser.getSelectedIndex());
		
		cv.addMouseMoveHandler(this);
		cv.addMouseDownHandler(this);
		cv.addMouseOutHandler(this);
		cv.addMouseUpHandler(this);
        cv.addMouseWheelHandler(this);
        cv.addClickHandler(this);
        doTouchHandlers(cv.getCanvasElement());
		cv.addDomHandler(this,  ContextMenuEvent.getType());
		
		reinit();
		set3dViewZoom(zoom3d);
		setCanvasSize();
		
		// String os = Navigator.getPlatform();
		// isMac = (os.toLowerCase().contains("mac"));
		// ctrlMetaKey = (isMac) ? "Cmd" : "Ctrl";
		timer.scheduleRepeating(FASTTIMER);

	}
	
	void createMenus() {
		  fileMenuBar = new MenuBar(true);
		  importFromLocalFileItem = new MenuItem("Open File...", new MyCommand("file","importfromlocalfile"));
		  importFromLocalFileItem.setEnabled(LoadFile.isSupported());
		  fileMenuBar.addItem(importFromLocalFileItem);
		  importFromTextItem = new MenuItem("Import From Text...", new MyCommand("file","importfromtext"));
		  fileMenuBar.addItem(importFromTextItem);
//		  importFromDropboxItem = new MenuItem("Import From Dropbox", new MyCommand("file", "importfromdropbox"));
//		  fileMenuBar.addItem(importFromDropboxItem); 
		  exportAsUrlItem = new MenuItem("Export As Link...", new MyCommand("file","exportasurl"));
		  fileMenuBar.addItem(exportAsUrlItem);
		  exportAsLocalFileItem = new MenuItem("Save As...", new MyCommand("file","exportaslocalfile"));
		  exportAsLocalFileItem.setEnabled(ExportAsLocalFileDialog.downloadIsSupported());
		  fileMenuBar.addItem(exportAsLocalFileItem);
		  exportAsTextItem = new MenuItem("Export As Text...", new MyCommand("file","exportastext"));
		  fileMenuBar.addItem(exportAsTextItem);
		  fileMenuBar.addItem(getClassCheckItem("Options...", "Options"));
		  fileMenuBar.addSeparator();
		  aboutItem=new MenuItem("About...",(Command)null);
		  fileMenuBar.addItem(aboutItem);
		  aboutItem.setScheduledCommand(new MyCommand("file","about"));
		  
          menuBar = new MenuBar();
          menuBar.addItem("File", fileMenuBar);

		MenuBar m = new MenuBar(true);
		final String edithtml="<div style=\"display:inline-block;width:80px;\">";
		String sn=edithtml+"Undo</div>Ctrl-Z";
		m.addItem(undoItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","undo")));
		// undoItem.setShortcut(new MenuShortcut(KeyEvent.VK_Z));
		sn=edithtml+"Redo</div>Ctrl-Y";
		m.addItem(redoItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","redo")));
		//redoItem.setShortcut(new MenuShortcut(KeyEvent.VK_Z, true));
		m.addSeparator();
//		m.addItem(cutItem = new MenuItem("Cut", new MyCommand("edit","cut")));
		sn=edithtml+"Cut</div>Ctrl-X";
		m.addItem(cutItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","cut")));
		//cutItem.setShortcut(new MenuShortcut(KeyEvent.VK_X));
		sn=edithtml+"Copy</div>Ctrl-C";
		m.addItem(copyItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","copy")));
		sn=edithtml+"Paste</div>Ctrl-V";
		m.addItem(pasteItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","paste")));
		//pasteItem.setShortcut(new MenuShortcut(KeyEvent.VK_V));
		pasteItem.setEnabled(false);
		
		sn=edithtml+"Duplicate</div>Ctrl-D";
		m.addItem(new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","duplicate")));
		
		m.addSeparator();
		sn=edithtml+"Select All</div>Ctrl-A";
		m.addItem(selectAllItem = new MenuItem(SafeHtmlUtils.fromTrustedString(sn), new MyCommand("edit","selectAll")));
		//selectAllItem.setShortcut(new MenuShortcut(KeyEvent.VK_A));
		menuBar.addItem("Edit",m);

		MenuBar drawMenuBar = new MenuBar(true);
		drawMenuBar.setAutoOpen(true);

		menuBar.addItem("Add", drawMenuBar);
		
		mainMenuBar = new MenuBar(true);
		mainMenuBar.setAutoOpen(true);
		composeMainMenu(mainMenuBar);
		composeMainMenu(drawMenuBar);
	}
	
    // install touch handlers to handle touch events properly on mobile devices.
    // don't feel like rewriting this in java.  Anyway, java doesn't let us create mouse
    // events and dispatch them.
    native void doTouchHandlers(CanvasElement cv) /*-{
	// Set up touch events for mobile, etc
	var lastTap;
	var tmout;
	var sim = this;
	cv.addEventListener("touchstart", function (e) {
        	mousePos = getTouchPos(cv, e);
  		var touch = e.touches[0];
  		var etype = "mousedown";
  		clearTimeout(tmout);
  		if (e.timeStamp-lastTap < 300) {
     		    etype = "dblclick";
  		} else {
  		    tmout = setTimeout(function() {
  		        sim.@com.falstad.ripple.client.RippleSim::longPress()();
  		    }, 1000);
  		}
  		lastTap = e.timeStamp;
  		
  		var mouseEvent = new MouseEvent(etype, {
    			clientX: touch.clientX,
    			clientY: touch.clientY
  		});
  		e.preventDefault();
  		cv.dispatchEvent(mouseEvent);
	}, false);
	cv.addEventListener("touchend", function (e) {
  		var mouseEvent = new MouseEvent("mouseup", {});
  		e.preventDefault();
  		clearTimeout(tmout);
  		cv.dispatchEvent(mouseEvent);
	}, false);
	cv.addEventListener("touchmove", function (e) {
  		var touch = e.touches[0];
  		var mouseEvent = new MouseEvent("mousemove", {
    			clientX: touch.clientX,
    			clientY: touch.clientY
  		});
  		e.preventDefault();
  		clearTimeout(tmout);
  		cv.dispatchEvent(mouseEvent);
	}, false);

	// Get the position of a touch relative to the canvas
	function getTouchPos(canvasDom, touchEvent) {
  		var rect = canvasDom.getBoundingClientRect();
  		return {
    			x: touchEvent.touches[0].clientX - rect.left,
    			y: touchEvent.touches[0].clientY - rect.top
  		};
	}
	
    }-*/;
    

    public void composeMainMenu(MenuBar mainMenuBar) {
    	mainMenuBar.addItem(getClassCheckItem("Add Wall", "Wall"));
    	mainMenuBar.addItem(getClassCheckItem("Add Slit", "Slit"));
    	mainMenuBar.addItem(getClassCheckItem("Add Box", "Box"));
    	mainMenuBar.addItem(getClassCheckItem("Add Point Source", "Source"));
    	mainMenuBar.addItem(getClassCheckItem("Add Line Source", "LineSource"));
    	mainMenuBar.addItem(getClassCheckItem("Add Multipole Source", "MultipoleSource"));
    	mainMenuBar.addItem(getClassCheckItem("Add Phased Array Source", "PhasedArraySource"));
    	mainMenuBar.addItem(getClassCheckItem("Add Solid Box", "SolidBox"));
    	mainMenuBar.addItem(getClassCheckItem("Add Moving Wall", "MovingWall"));
    	mainMenuBar.addItem(getClassCheckItem("Add Moving Source", "MovingSource"));
    	mainMenuBar.addItem(getClassCheckItem("Add Cavity", "Cavity"));
    	mainMenuBar.addItem(getClassCheckItem("Add Medium", "MediumBox"));
    	mainMenuBar.addItem(getClassCheckItem("Add Mode Box", "ModeBox"));
    	mainMenuBar.addItem(getClassCheckItem("Add Gradient", "GradientBox"));
    	mainMenuBar.addItem(getClassCheckItem("Add Ellipse", "Ellipse"));
    	mainMenuBar.addItem(getClassCheckItem("Add Prism", "TrianglePrism"));
    	mainMenuBar.addItem(getClassCheckItem("Add Ellipse Medium", "MediumEllipse"));
    	mainMenuBar.addItem(getClassCheckItem("Add Parabola", "Parabola"));
    	mainMenuBar.addItem(getClassCheckItem("Add Lens", "Lens"));
    	mainMenuBar.addItem(getClassCheckItem("Add Probe", "Probe"));
    }

    MenuItem getClassCheckItem(String s, String t) {
        return new MenuItem(s, new MyCommand("main", t));
    }

    public void wallsChanged() {
    	changedWalls = true;
    }
    
    public void menuPerformed(String menu, String item) {
    	if (item=="about")
    		aboutBox = new AboutBox(versionString);
    	if (item=="importfromlocalfile") {
    		pushUndo();
    		loadFileInput.click();
    	}
    	if (item=="importfromtext") {
    		importFromTextDialog = new ImportFromTextDialog(this);
    	}
    	if (item=="exportasurl") {
    		doExportAsUrl();
    	}
    	if (item=="exportaslocalfile")
    		doExportAsLocalFile();
    	if (item=="exportastext")
    		doExportAsText();

    	if (item=="undo")
    		doUndo();
    	if (item=="redo")
    		doRedo();
    	if (item == "cut") {
    		if (menu!="elm")
    			menuObject = null;
    		doCut();
    	}
    	if (item == "copy") {
    		if (menu!="elm")
    			menuObject = null;
    		doCopy();
    	}
    	if (item == "delete") {
    		if (menu!="elm")
    			menuObject = null;
    		doDelete();
    	}
    	if (item=="paste")
    		doPaste(null);
    	if (item=="duplicate") {
    		if (menu!="elm")
    			menuObject = null;
    		doDuplicate();
    	}
    	if (item=="selectAll")
    		doSelectAll();

    	if (menu=="elm" && contextPanel!=null)
    		contextPanel.hide();
    	if (contextPanel != null)
    		contextPanel.hide();
    	if (item == "edit")
    		doEdit(selectedObject);
    	if (item == "rotate" && selectedObject != null && selectedObject.canRotate())
    		rotationMode = true;
    	DragObject newObject = null;
    	if (item == "Wall")
    		newObject = new Wall();
    	if (item == "Box")
    		newObject = new Box();
    	if (item == "MediumBox")
    		newObject = new MediumBox();
    	if (item == "GradientBox")
    		newObject = new GradientBox();
    	if (item == "Cavity")
    		newObject = new Cavity();
    	if (item == "MediumEllipse")
    		newObject = new MediumEllipse();
    	if (item == "Ellipse")
    		newObject = new Ellipse();
    	if (item == "SolidBox")
    		newObject = new SolidBox();
    	if (item == "MovingWall")
    		newObject = new MovingWall();
    	if (item == "MovingSource")
    		newObject = new MovingSource();
    	if (item == "ModeBox")
    		newObject = new ModeBox();
    	if (item == "TrianglePrism")
    		newObject = new TrianglePrism();
    	if (item == "Parabola")
    		newObject = new Parabola();
    	if (item == "Lens")
    		newObject = new Lens();
    	if (item == "Source")
    		newObject = new Source();
    	if (item == "Probe") {
    		if (!readPixelsWorks()) {
    			Window.alert("Not supported in this browser.  Try Chrome.");
    			return;
    		}
    		newObject = new Probe();
    	}
    	if (item == "LineSource")
    		newObject = new LineSource();
    	if (item == "PhasedArraySource")
    		newObject = new PhasedArraySource();
    	if (item == "MultipoleSource")
    		newObject = new MultipoleSource();
    	if (item == "Slit")
    		newObject = new Slit();
    	if (newObject != null) {
    		pushUndo();
    		newObject.setInitialPosition();
    		dragObjects.add(newObject);
    		setSelectedObject(newObject);
    		preserveSelection = true;
    	}
    	if (item == "Options") {
    		doEdit(new EditOptions(this));
    	}
    }

    DragObject createObj(int tint, StringTokenizer st) {
    	if (tint == 'b') return new Box(st);
    	if (tint == 'c') return new Cavity(st);
    	if (tint == 'e') return new Ellipse(st);
    	if (tint == 'g') return new GradientBox(st);
    	if (tint == 'l') return new Lens(st);
    	if (tint == 'S') return new LineSource(st);
    	if (tint == 'm') return new MediumBox(st);
    	if (tint == 'E') return new MediumEllipse(st);
    	if (tint == 'M') return new ModeBox(st);
    	if (tint == 'd') return new MovingSource(st);
    	if (tint == 'W') return new MovingWall(st);
    	if (tint == 200) return new MultipoleSource(st);
    	if (tint == 'p') return new Parabola(st);
    	if (tint == 'P') return new Probe(st);
    	if (tint == 201) return new PhasedArraySource(st);
    	if (tint == 203) return new Slit(st);
    	if (tint == 202) return new SolidBox(st);
    	if (tint == 's') return new Source(st, 1);
    	if (tint == 't') return new TrianglePrism(st);
    	if (tint == 'w') return new Wall(st);
    	return null;
    }
    
    void doEdit(Editable eable) {
        clearSelection();
        pushUndo();
        if (editDialog != null) {
    //          requestFocus();
                editDialog.setVisible(false);
                editDialog = null;
        }
        editDialog = new EditDialog(eable, this);
        editDialog.show();
    }
    
    void doExportAsUrl()
    {
    	String dump = dumpLayout();
    	exportAsUrlDialog = new ExportAsUrlDialog(dump);
    	exportAsUrlDialog.show();
    }

    void doExportAsText()
    {
    	String dump = dumpLayout();
    	exportAsTextDialog = new ExportAsTextDialog(this, dump);
    	exportAsTextDialog.show();
    }

    void doExportAsLocalFile() {
    	String dump = dumpLayout();
    	exportAsLocalFileDialog = new ExportAsLocalFileDialog(dump);
    	exportAsLocalFileDialog.show();
    }

	boolean moveRight = true;
	boolean moveDown = true;

	long lastTime = 0, lastFrameTime, secTime = 0;
	int frames = 0;
	int steps = 0;
	int framerate = 0, steprate = 0;

	void reinit() {
		reinit(true);
	}

	void reinit(boolean setup) {
		sourceCount = -1;
		System.out.print("reinit " + gridSizeX + " " + gridSizeY + "\n");
		gridSizeXY = gridSizeX * gridSizeY;
		if (setup)
			doSetup();
	}

	// draw objects into blue channel of render texture
	void prepareObjects() {
		doBlankWalls();
		int i;
		setAcoustic(waveChooser.getSelectedIndex() == WAVE_SOUND); // need this for mode boxes
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			double xform[] = obj.transform;
			setTransform(xform[0], xform[1], xform[2], xform[3], xform[4], xform[5]);
			obj.prepare();
		}
		setTransform(1, 0, 0, 0, 1, 0);
	}

	// call reset() on objects after clearing waves
	// used to draw mode boxes, reset probe data, etc
	void reset() {
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			double xform[] = obj.transform;
			setTransform(xform[0], xform[1], xform[2], xform[3], xform[4], xform[5]);
			obj.reset();
		}
		setTransform(1, 0, 0, 0, 1, 0);
	}

	public void updateRipple() {
			if (changedWalls) {
				prepareObjects();
				changedWalls = false;
			}
			int iterCount = speedBar.getValue();
			if (stoppedCheck.getState() || dragStop)
				iterCount = 0;
			int i;
			setAcoustic(waveChooser.getSelectedIndex() == WAVE_SOUND);
			for (i = 0; i != iterCount; i++) {
				simulate();
				t += timeStep;
				int j;
				for (j = 0; j != dragObjects.size(); j++)
					dragObjects.get(j).run();
				iters++;
				if (pokePoint != null)
					drawPoke(pokePoint.x, pokePoint.y, pokeValue);
			}
			brightMult = Math.exp(brightnessBar.getValue() / 100. - 5.);
			updateRippleGL(brightMult, view3dCheck.getState());
			if (!view3dCheck.getState())
				for (i = 0; i != dragObjects.size(); i++) {
					DragObject obj = dragObjects.get(i);
					setDrawingColor(1, 1, 0, .5);
					if (obj.selected)
						setDrawingSelection(.6+.4*Math.sin(t*.2));
					else
						setDrawingSelection(-1);
					double xform[] = obj.transform;
					setTransform(xform[0], xform[1], xform[2], xform[3], xform[4], xform[5]);
					obj.draw();
				}
			setTransform(1, 0, 0, 0, 1, 0);
			setDrawingSelection(-1);
			doCoordsLabel();
	}

	void doCoordsLabel() {
		if (mouseLocation == null) {
			coordsLabel.setVisible(false);
			return;
		}
		String txt = (selectedObject != null) ? selectedObject.selectText() : null;
		if (txt == null) {
			txt = "t = " + getUnitText(getRealTime(), "s");
		}
		Point pt = mouseLocation;
		coordsLabel.setText("(" + getLengthText(pt.x) + ", " + getLengthText(windowHeight-1-pt.y) + ") " + txt);
		absolutePanel.setWidgetPosition(coordsLabel,
				0,
				(pt.x < windowWidth/4 && pt.y > windowHeight*3/4) ? 0 :
					cv.getOffsetHeight()-coordsLabel.getOffsetHeight());
		coordsLabel.setVisible(true);
	}
	
	int abs(int x) {
		return x < 0 ? -x : x;
	}

	int sign(int x) {
		return (x < 0) ? -1 : (x == 0) ? 0 : 1;
	}

	void setDamping() {
		double damper = dampingBar.getValue() * .0002;
		dampcoef = Math.exp(-damper);
		setDamping(dampcoef);
		console("damp " + dampcoef + " " + dampingBar.getValue());
	}

	void setFreqBar(int x) {
		freqBar.setValue(x);
		freqBarValue = x;
		freqTimeZero = 0;
	}

	void setFreq() {
		// adjust time zero to maintain continuity in the freq func
		// even though the frequency has changed.
//		double oldfreq = freqBarValue * freqMult;
		freqBarValue = freqBar.getValue();
		double newfreq = freqBarValue * freqMult;
//		double adj = newfreq - oldfreq;
//		freqTimeZero = t - oldfreq * (t - freqTimeZero) / newfreq;
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			if (obj instanceof Source) {
				((Source) obj).setFrequency(newfreq);
			}
		}
	}

	void setResolution() {
		int newWidth = resBar.getValue();
		setResolution(newWidth, 0);
	}

	void setResolution(int newWidth, int border) {
		int oldWidth = windowWidth;
		if (newWidth == oldWidth && border == 0)
			return;
		if (border == 0) {
			border = newWidth / 8;
			if (border < 20)
				border = 20;
		}
		if (resBar.getValue() != newWidth)
			resBar.setValue(newWidth);
		windowWidth = windowHeight = newWidth;
		windowOffsetX = windowOffsetY = border;
		System.out.println(windowWidth + "," + windowHeight);
		gridSizeX = windowWidth + windowOffsetX * 2;
		gridSizeY = windowHeight + windowOffsetY * 2;
		windowBottom = windowOffsetY + windowHeight - 1;
		windowRight = windowOffsetX + windowWidth - 1;
		setResolutionGL(gridSizeX, gridSizeY, windowOffsetX, windowOffsetY);
		console("res " + gridSizeX + " " + speedBar.getValue());
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			obj.rescale(windowWidth/(double)oldWidth);
		}
		changedWalls = true;
	}

	void setResolution(int x) {
		setResolution(x, 0);
	}

	void view3dDrag(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		set3dViewAngle(x-dragX, y-dragY);
		dragX = x;
		dragY = y;
	}

	void deleteAllObjects() {
        for (int i = dragObjects.size()-1; i >= 0; i--) {
        	DragObject ce = dragObjects.get(i);
            ce.delete();
        }
		dragObjects.removeAllElements();
		selectedObject = null;
		doBlankWalls();
	}

	void resetTime() {
		t = 0;
		iters = 0;
	}
	
	void doSetup() {
		if (setupList.size() == 0)
			return;
		resetTime();
		if (resBar.getValue() < 32)
			setResolution(32);
		doBlank();
		deleteAllObjects();
		dampingBar.setValue(0);
		setFreqBar(5);
		setBrightness(10);
		waveChooser.setSelectedIndex(1);
		setWaveType();
		setup = (Setup) setupList.elementAt(setupChooser.getSelectedIndex());
		setup.select();
		setDamping();
		enableDisableUI();
	}

	void setBrightness(int x) {
		double m = x / 5.;
		m = (Math.log(m) + 5.) * 100;
		brightnessBar.setValue((int) m);
	}

	void doColor() {
		int cn = colorChooser.getSelectedIndex();
		wallColor = schemeColors[cn][0];
		posColor = schemeColors[cn][1];
		negColor = schemeColors[cn][2];
		zeroColor = schemeColors[cn][3];
		posMedColor = schemeColors[cn][4];
		negMedColor = schemeColors[cn][5];
		medColor = schemeColors[cn][6];
		sourceColor = schemeColors[cn][7];
		int zerocol3d = zeroColor.toInteger();
		if (zerocol3d == 0)
			zerocol3d = 0x808080;
		setColors(wallColor.toInteger(), posColor.toInteger(), negColor.toInteger(),
				zeroColor.toInteger(), posMedColor.toInteger(), negMedColor.toInteger(),
				  medColor.toInteger(), sourceColor.toInteger(), zerocol3d);
	}

	void addDefaultColorScheme() {
		String schemes[] = {
				"#808080 #00ffff #000000 #008080 #0000ff #000000 #000080 #ffffff",
				"#808080 #00ff00 #ff0000 #000000 #00ffff #ff00ff #0000ff #0000ff",
				"#800000 #00ffff #0000ff #000000 #80c8c8 #8080c8 #808080 #ffffff",
				"#800000 #ffffff #000000 #808080 #0000ff #000000 #000080 #00ff00",
				"#800000 #ffff00 #0000ff #000000 #ffff80 #8080ff #808080 #ffffff",
				"#808080 #00ff00 #ff0000 #FFFFFF #00ffff #ff00ff #0000ff #0000ff",
				"#FF0000 #00FF00 #0000FF #FFFF00 #00FFFF #FF00FF #FFFFFF #000000" };
		int i;

		for (i = 0; i != 7; i++)
			decodeColorScheme(i, schemes[i]);
		// colorChooser.hide();
	}

	void decodeColorScheme(int cn, String s) {
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens()) {
			int i;
			for (i = 0; i != 8; i++)
				schemeColors[cn][i] = Color.hex2Rgb(st.nextToken());
		}
		colorChooser.add("Color Scheme " + (cn + 1));
	}

	void addMedium() {
		MediumBox mb = new MediumBox(-windowOffsetX, windowHeight/2, windowWidth+windowOffsetX-1, windowHeight+windowOffsetY-1);
		dragObjects.add(mb);
	}

	// set length scale and speed for a particular wave type, which determines what units we report
	// in coordinates box and editing values.  Also, choice of sound/non-sound affects boundary conditions
	// at walls.
	void setWaveType() {
		double windowScale = 25;
		switch (waveChooser.getSelectedIndex()) {
		case 0: // sound
			waveSpeed = 343.2;
			windowScale = 25;
			break;
		case 1: // light
			waveSpeed = 299792458;
			windowScale = 8000e-9;
			break;
		case 2: // AM
			waveSpeed = 299792458;
			windowScale = 50000;
			break;
		case 3: // FM
			waveSpeed = 299792458;
			windowScale = 60;
			break;
		case 4: // uwave
			waveSpeed = 299792458;
			windowScale = 2;
			break;
		}
		lengthScale = windowScale/512;
	}
	
    void getSetupList() {

    	String url;
    	url = GWT.getModuleBaseURL()+"setuplist.txt"+"?v="+random.nextInt(); 
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					GWT.log("File Error Response", exception);
				}

				public void onResponseReceived(Request request, Response response) {
					// processing goes here
					if (response.getStatusCode()==Response.SC_OK) {
					String text = response.getText();
					processSetupList(text.getBytes(), text.length());
					if (startLayoutText == null)
						doSetup();
					else
						readImport(startLayoutText);
					// end of processing
					}
					else 
						GWT.log("Bad file server response:"+response.getStatusText() );
				}
			});
		} catch (RequestException e) {
			GWT.log("failed file reading", e);
		}
    }
		
    void processSetupList(byte b[], int len) {
    	int p;
    	for (p = 0; p < len; ) {
    		int l;
    		for (l = 0; l != len-p; l++)
    			if (b[l+p] == '\n') {
    				l++;
    				break;
    			}
    		String line = new String(b, p, l-1);
    		if (line.charAt(0) == '#')
    			;
/*    		else if (line.charAt(0) == '+') {
    		//	MenuBar n = new Menu(line.substring(1));
    			MenuBar n = new MenuBar(true);
    			n.setAutoOpen(true);
    			currentMenuBar.addItem(line.substring(1),n);
    			currentMenuBar = stack[stackptr++] = n;
    		} else if (line.charAt(0) == '-') {
    			currentMenuBar = stack[--stackptr-1];
    		} */
    		else {
    			int i = line.indexOf(' ');
    			if (i > 0) {
    				String title = line.substring(i+1);
    				boolean first = false;
    				if (line.charAt(0) == '>')
    					first = true;
    				String file = line.substring(first ? 1 : 0, i);
    				Setup s = new FileSetup(title, file);
    				setupList.add(s);
    				setupChooser.add("Example: " + title);
    			}
    		}
    		p += l;
    	}
}


	void readSetupFile(String str, String title) {
		resetTime();
		console("reading example " + str);
		String url=GWT.getModuleBaseURL()+"examples/"+str+"?v="+random.nextInt(); 
		loadFileFromURL(url);
		enableDisableUI();
	}

	void loadFileFromURL(String url) {
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					GWT.log("File Error Response", exception);
				}

				public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode()==Response.SC_OK) {
					String text = response.getText();
					readImport(text);
					}
					else 
						GWT.log("Bad file server response:"+response.getStatusText() );
				}
			});
		} catch (RequestException e) {
			GWT.log("failed file reading", e);
		}
		
	}

	String dumpLayout() {
		String dump = "";

		int i;
		dump = "$ 3 " + windowWidth + " " + windowOffsetX + " " + dampingBar.getValue() + " " +
				waveChooser.getSelectedIndex() + " " + brightnessBar.getValue() + " " + lengthScale + "\n";
/*		for (i = 0; i != sourceCount; i++) {
			OscSource src = sources[i];
			dump += "s " + src.x + " " + src.y + "\n";
		}*/
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			dump += obj.dump() + "\n";
		}
		return dump;
	}
	
	void readImport(String s) { readImport (s, false); }
	
	void readImport(String s, boolean retain) {
		if (!retain) {
			doBlank();
			resetTime();
			deleteAllObjects();
		}
		char b[] = new char[s.length()];
		s.getChars(0, s.length(), b, 0);
		int len = s.length();
		int p;
		int x = 0;
		int srci = 0;
//		setupChooser.select(0);
//		setup = (Setup) setupList.elementAt(0);
		for (p = 0; p < len;) {
			int l;
            int linelen = len-p; // IES - changed to allow the last line to not end with a delim.
			for (l = 0; l != len - p; l++)
				if (b[l + p] == '\n' || b[l + p] == '\r') {
					linelen = l++;
					if (l + p < b.length && b[l + p] == '\n')
						l++;
					break;
				}
			String line = new String(b, p, linelen);
            StringTokenizer st = new StringTokenizer(line, " +\t\n\r\f");
			while (st.hasMoreTokens()) {
				String type = st.nextToken();
				int tint = type.charAt(0);
				try {
					if (tint == '$') {
						int flags = new Integer(st.nextToken()).intValue();
						if ((flags & 1) == 0)
							return;

						int ww = Integer.parseInt(st.nextToken());
						int wo = Integer.parseInt(st.nextToken());
						setResolution(ww, wo);
						reinit(false);

						int damp = Integer.parseInt(st.nextToken());
						
						// ignore damping values on old dumps
						if ((flags & 2) != 0)
							dampingBar.setValue(damp);
						
						waveChooser.setSelectedIndex(Integer.parseInt(st.nextToken()));
						setWaveType();
						brightnessBar.setValue(new Integer(st.nextToken())
								.intValue());
						lengthScale = Double.parseDouble(st.nextToken());
						break;
					}
                    if (tint >= '0' && tint <= '9')
                        tint = new Integer(type).intValue();
                    
                    // ignore probes if we can't display them
                    if (tint == 'P' && !readPixelsWorks())
                    	break;
                    
                    DragObject newobj = createObj(tint, st);
                    if (newobj==null) {
                    	console("unrecognized dump type: " + type);
                    	break;
                    }
                    if (newobj.getDumpType() != tint)
                    	console("dump type mismatch for " + tint);
                    dragObjects.add(newobj);
				} catch (Exception ee) {
					console("got exception when reading setup");
					ee.printStackTrace();
					break;
				}
				break;
			}
			p += l;

		}
		setDamping();
		wallsChanged();
		enableDisableUI();
		console("done with reading setup, " + dragObjects.size() + " " + changedWalls);
	}

	abstract class Setup {
		abstract String getName();

		void select() {}

		void deselect() {
		}

		double sourceStrength() {
			return 1;
		}

		Setup createNext() { return null; }

		void eachFrame() {
		}

		float calcSourcePhase(double ph, float v, double w) {
			return v;
		}
	};

	class FileSetup extends Setup {
		String title, file;
		
		FileSetup(String t, String f) {
			title = t;
			file = f;
		}
		
		void select() {
			readSetupFile(file, title);
		}
		
		String getName() { return title; }
	}
	
	@Override
	public void onMouseUp(MouseUpEvent event) {
		event.preventDefault();
//		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
//		    return;
		dragging = false;
		dragStop = false;
		pokePoint = null;
		if (mouseObject == null)
			preserveSelection = false;
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		event.preventDefault();
		doMouseMove(event);
	}
	
	Point getPointFromEvent(MouseEvent<?> event) {
		int xp = event.getX()*windowWidth/winSize.width;
		int yp = event.getY()*windowHeight/winSize.height;
		return new Point(xp, yp);
	}
	
	double getRealTime() {
		return t/(2*Math.PI*waveSpeed/Source.freqScale / lengthScale);
	}
	
	double timeToRealTime(double x) {
		return x/(2*Math.PI*waveSpeed/Source.freqScale / lengthScale);
	}
	
	double realTimeToTime(double x) {
		return x * (2*Math.PI*waveSpeed/Source.freqScale / lengthScale);
	}
	
	void doMouseMove(MouseEvent<?> event) {
		Point pt = getPointFromEvent(event);
		mouseLocation = pt;
		if (rotationMode) {
			selectedObject.rotateTo(pt.x, pt.y);
			return;
		}
		if (dragging) {
			dragMouse(event);
			return;
		}
		int x = event.getX();
		int y = event.getY();
		dragPoint = getPointFromEvent(event);
		dragStartX = dragX = x;
		dragStartY = dragY = y;
		
		double minf = 5 * windowWidth/winSize.height + 1;
		double bestf = minf;
		Point mp = getPointFromEvent(event);
		draggingHandle = null;
		if (selectedObject != null) {
			int i;
			// select handle?
			Point p = selectedObject.inverseTransformPoint(mp);
			for (i = 0; i != selectedObject.handles.size(); i++) {
				DragHandle dh = selectedObject.handles.get(i);
				double r = DragObject.hypotf(p.x-dh.x, p.y-dh.y);
				if (r < bestf) {
					draggingHandle = dh;
					bestf = r;
				}
			}
			if (draggingHandle != null)
				return;
		}

		// select object?
		DragObject sel = null;
		bestf = 1e8;
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			Point p = obj.inverseTransformPoint(mp);
			double ht = obj.hitTest(p.x, p.y);
			
	        // if there are no better options, select a RectDragObject if we're tapping
	        // inside it.
			if (ht > minf && !obj.hitTestInside(p.x, p.y))
				continue;
			
			// find best match
			if (ht < bestf) {
				sel = obj;
				bestf = ht;
			}
		}
		if (!preserveSelection)
			setSelectedObject(sel);
		mouseObject = sel;
	}

    static String getUnitText(double v, String u) {
        double va = Math.abs(v);
        if (va < 1e-17)
            return "0 " + u;
        if (va < 1e-12)
        	return showFormat.format(v*1e15) + " f" + u;
        if (va < 1e-9)
            return showFormat.format(v*1e12) + " p" + u;
        if (va < 1e-6)
            return showFormat.format(v*1e9) + " n" + u;
        if (va < 1e-3)
            return showFormat.format(v*1e6) + " \u03bc" + u;
        if (va < 1e-2 || (va < 1 && u.equals("s")))
            return showFormat.format(v*1e3) + " m" + u;
        if (va < 1)
            return showFormat.format(v*1e2) + " c" + u;
        if (va < 1e3)
            return showFormat.format(v) + " " + u;
        if (va < 1e6)
            return showFormat.format(v*1e-3) + " k" + u;
        if (va < 1e9)
            return showFormat.format(v*1e-6) + " M" + u;
        if (va < 1e12)
            return showFormat.format(v*1e-9) + " G" + u;
        if (va < 1e15)
            return showFormat.format(v*1e-12) + " T" + u;
        if (va < 1e18)
            return showFormat.format(v*1e-15) + " P" + u;
        return showFormat.format(v*1e-18) + " E" + u;
    }
    
    static String getShortUnitText(double v, String u) {
        double va = Math.abs(v);
        if (va < 1e-13)
            return null;
        if (va < 1e-9)
            return shortFormat.format(v*1e12) + "p" + u;
        if (va < 1e-6)
            return shortFormat.format(v*1e9) + "n" + u;
        if (va < 1e-3)
            return shortFormat.format(v*1e6) + "\u03bc" + u;
        if (va < 1e-2)
            return shortFormat.format(v*1e3) + "m" + u;
        if (va < 1)
            return shortFormat.format(v*1e2) + "c" + u;
        if (va < 1e3)
            return shortFormat.format(v) + u;
        if (va < 1e6)
            return shortFormat.format(v*1e-3) + "k" + u;
        if (va < 1e9)
            return shortFormat.format(v*1e-6) + "M" + u;
        if (va < 1e12)
            return shortFormat.format(v*1e-9) + "G" + u;
        if (va < 1e15)
            return shortFormat.format(v*1e-12) + "T" + u;
        if (va < 1e18)
            return shortFormat.format(v*1e-15) + "P" + u;
        return shortFormat.format(v*1e-18) + "E" + u;
    }
    
    // convert pixels to meters and return as string
    String getLengthText(double px) {
    	return getUnitText(px * lengthScale, "m");
    }

    // convert pixels/iter to m/s and return as string
    String getSpeedText(double px) {
    	return getUnitText(px*4 *2*Math.PI*waveSpeed/Source.freqScale, "m/s");
    }
    
	void dragMouse(MouseEvent<?> event) {
		if (view3dCheck.getState()) {
			view3dDrag(event);
			return;
		}
		dragging = true;
		adjustResolution = false;

		Point pt = getPointFromEvent(event);
		if (draggingHandle != null) {
			Point mp = selectedObject.inverseTransformPoint(pt);
			draggingHandle.dragTo(mp.x, mp.y);
			changedWalls = true;
		} else if (isSelection()) {
			if (dragPoint.x != pt.x || dragPoint.y != pt.y) {
				int i;
				for (i = 0; i != dragObjects.size(); i++) {
					DragObject obj = dragObjects.get(i);
					if (obj.isSelected())
						obj.drag(pt.x-dragPoint.x, pt.y-dragPoint.y);
				}
				dragPoint = pt;
				changedWalls = true;
			}
		} else {
			if (pokePoint != null)
				pokePoint = pt;
			drawPoke(pt.x, pt.y, pokeValue);
		}
	}

	boolean isSelection() {
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			if (obj.isSelected())
				return true;
		}
		return false;
	}
	
	void enableDisableUI() {
		int i;
		
		// check if all sources are same frequency
		Source src1 = null;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			if (!(obj instanceof Source))
				continue;
			Source src = (Source)obj;
			
			// don't let freq be adjusted if we have pulse sources
			if (src.waveform == Source.WF_PULSE) {
				src1 = null;
				break;
			}
			if (src1 == null)
				src1 = src;
			else if (Math.abs(src.frequency-src1.frequency) > 1e-3) {
				src1 = null;
				break;
			}
		}
		if (src1 == null)
			freqBar.disable();
		else {
			freqBar.enable();
			ignoreFreqBarSetting = true;
			freqBar.setValue((int)(src1.frequency/freqMult));
			ignoreFreqBarSetting = false;
		}
	}
	
	@Override
	public void onMouseDown(MouseDownEvent event) {
		event.preventDefault();
		adjustResolution = false;
		pushUndo();
		doMouseMove(event);
		if (rotationMode) {
			rotationMode = false;
			return;
		}
		dragging = true;
		
		if (view3dCheck.getState())
			return;

		if (mouseObject != null)
			mouseObject.mouseDown();
		Point mp = getPointFromEvent(event);

		if (draggingHandle == null) {
			if (mouseObject == null)
				setSelectedObject(null);
			else {
				if (!mouseObject.isSelected())
					setSelectedObject(mouseObject);
				preserveSelection = true;
			}
		}
//		console("onmd " + mouseObject + " " + preserveSelection + " " + draggingHandle);
		
		if (mouseObject == null && event.getNativeButton() != NativeEvent.BUTTON_RIGHT) {
			pokeValue = event.isShiftKeyDown() ? -1 : 1;
			if (event.isMetaKeyDown() || event.isControlKeyDown())
				dragStop = true;
			else
				pokePoint = mp;
			drawPoke(mp.x, mp.y, pokeValue);
		}
	}
	
	void setSelectedObject(DragObject obj) {
		if (obj != null && obj.isSelected())
			return;
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject dd = dragObjects.get(i);
			dd.setSelected(false);
		}
		selectedObject = obj;
		if (obj != null)
			selectedObject.setSelected(true);
		preserveSelection = false;
	}
	
	@Override
	public void onMouseWheel(MouseWheelEvent event) {
        event.preventDefault();
        if (selectedObject != null && selectedObject.canRotate()) {
        	// rotate in 15 degree steps, but save mouse wheel motions that aren't large enough for a rotation
        	int dy = event.getDeltaY() + mouseWheelAccum;
        	int dy10 = dy/10;
        	mouseWheelAccum = dy-dy10*10;
        	console("wheel " + mouseWheelAccum + " " + dy + " " + dy10 + " " + event.getDeltaY());
        	selectedObject.rotate(dy10*Math.PI/12);
        	preserveSelection = true;
        }
        if (view3dCheck.getState()) {
        	zoom3d *= Math.exp(-event.getDeltaY() * .01);
        	set3dViewZoom(zoom3d);
        }
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		dragging = false;
		dragStop = false;
		pokePoint = null;
		mouseLocation = null;
	}

	@Override
	public void onPreviewNativeEvent(NativePreviewEvent event) {
		// TODO Auto-generated method stub

	}

	int menuX, menuY;
    PopupPanel contextPanel = null;

	@Override
	public void onContextMenu(ContextMenuEvent e) {
        e.preventDefault();
        menuX = e.getNativeEvent().getClientX();
        menuY = e.getNativeEvent().getClientY();
        doPopupMenu();
	}

    void longPress() {
    	menuX = dragStartX;
    	menuY = dragStartY;
    	doPopupMenu();
    }

    void doPopupMenu() {
    	if (selectedObject != null) {
                elmEditMenuItem .setEnabled(selectedObject.getEditInfo(0) != null);
                elmRotateMenuItem.setEnabled(selectedObject.canRotate());
                contextPanel=new PopupPanel(true);
                contextPanel.add(elmMenuBar);
                contextPanel.setPopupPosition(menuX, menuY);
                contextPanel.show();
        } else {
    	int x, y;
    	
                contextPanel=new PopupPanel(true);
                contextPanel.add(mainMenuBar);
                x=Math.max(0, Math.min(menuX, cv.getCoordinateSpaceWidth()-400));
                y=Math.max(0, Math.min(menuY,cv.getCoordinateSpaceHeight()-450));
                contextPanel.setPopupPosition(x,y);
                contextPanel.show();
        }
    }

	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		event.preventDefault();
	}

	void doCreateWall() {
		Wall w = new Wall();
		w.setInitialPosition();
		dragObjects.add(w);
	}
	
	Rectangle findSpace(DragObject obj, int sx, int sy) {
		int spsize = 20;
		boolean spacegrid[][] = new boolean[spsize][spsize];
		int i;
		int jx, jy;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject d = dragObjects.get(i);
			Rectangle r = d.boundingBox();
			for (jx = r.x*spsize/windowWidth; jx <= (r.x+r.width)*spsize/windowWidth; jx++)
				for (jy = r.y*spsize/windowHeight; jy <= (r.y+r.height)*spsize/windowHeight; jy++) {
					if (jx >= 0 && jy >= 0 && jx < spsize && jy < spsize) {
						spacegrid[jx][jy] = true;
					}
				}
		}
        int spiralIndex = 1, spiralCounter = 1;
        int tx = spsize/2;
        int ty = spsize/2;
        int dx = 1;
        int dy = 0;
        while (true) {
        	if (!spacegrid[tx][ty]) {
        		return new Rectangle(tx*windowWidth/spsize+2, ty*windowHeight/spsize+2,
        				windowWidth/spsize-4,
        				windowHeight/spsize-4);
        	}
        	tx += dx;
        	ty += dy;
            if (--spiralIndex == 0) {
                int d0 = dx;
                dx = dy;
                dy = -d0;
                if (dy == 0) spiralCounter++;
                spiralIndex = spiralCounter;
            }
            if (tx < 0 || ty < 0 || tx >= spsize || ty >= spsize)
            	break;
        }
		return new Rectangle(gridSizeX/2, gridSizeY/2, spsize, spsize);
	}
	
	@Override
	public void onClick(ClickEvent event) {
		event.preventDefault();
		if (event.getSource() == blankButton) {
			doBlank();
			reset();
			resetTime();
		}
		
		if (event.getSource() == resBar) {
		    setResolution();
//		    reinit();
		}
		if (event.getSource() == dampingBar)
		    setDamping();
		if (event.getSource() == freqBar) {
		    setFreq();
		}
	}

	@Override
	public void onChange(ChangeEvent event) {

			if (event.getSource() == stoppedCheck) {
//			    cv.repaint();
			    return;
			}
			if (event.getSource() == setupChooser)
			    doSetup();
			if (event.getSource() == colorChooser){
			    doColor();
			}
			if (event.getSource() == waveChooser)
				setWaveType();
	}

    void pushUndo() {
        String s = dumpLayout();
        if (undoStack.size() > 0 &&
                        s.compareTo(undoStack.lastElement()) == 0)
                return;
        redoStack.removeAllElements();
        undoStack.add(s);
        enableUndoRedo();
    }

    void doUndo() {
        if (undoStack.size() == 0)
                return;
        redoStack.add(dumpLayout());
        String s = undoStack.remove(undoStack.size()-1);
        readImport(s);
        enableUndoRedo();
    }

    void doRedo() {
        if (redoStack.size() == 0)
                return;
        undoStack.add(dumpLayout());
        String s = redoStack.remove(redoStack.size()-1);
        readImport(s);
        enableUndoRedo();
    }

    void enableUndoRedo() {
        redoItem.setEnabled(redoStack.size() > 0);
        undoItem.setEnabled(undoStack.size() > 0);
    }

    void setMenuSelection() {
        if (menuObject != null) {
                if (menuObject.selected)
                        return;
                clearSelection();
                menuObject.setSelected(true);
        }
    }

    void doCut() {
        int i;
        pushUndo();
        setMenuSelection();
        clipboard = "";
        for (i = dragObjects.size()-1; i >= 0; i--) {
        	DragObject ce = dragObjects.get(i);
                if (ce.isSelected()) {
                        clipboard = ce.dump() + "\n" + clipboard;
                        ce.delete();
                        dragObjects.removeElementAt(i);
                }
        }
        writeClipboardToStorage();
        enablePaste();
        wallsChanged();
    }

    void writeClipboardToStorage() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
                return;
        stor.setItem("rippleClipboard", clipboard);
    }
    
    void readClipboardFromStorage() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
                return;
        clipboard = stor.getItem("rippleClipboard");
    }

    void doDelete() {
        int i;
        pushUndo();
        setMenuSelection();
        boolean hasDeleted = false;

        for (i = dragObjects.size()-1; i >= 0; i--) {
        	DragObject ce = dragObjects.get(i);
                if (ce.isSelected()) {
                        ce.delete();
                        dragObjects.removeElementAt(i);
                        hasDeleted = true;
                }
        }

        if ( !hasDeleted )
        {
        	/*
            for (i = dragObjects.size()-1; i >= 0; i--) {
            	DragObject ce = dragObjects.get(i);
                        if (ce == mouseObject) {
                                ce.delete();
                                dragObjects.removeElementAt(i);
                                hasDeleted = true;
                                setMouseElm(null);
                                break;
                        }
                }
                */
        }

        if ( hasDeleted )
        	wallsChanged();
    }

    void doCopy() {
        int i;
        clipboard = "";
        setMenuSelection();
        for (i = dragObjects.size()-1; i >= 0; i--) {
        	DragObject ce = dragObjects.get(i);
        	if (ce.isSelected())
        		clipboard += ce.dump() + "\n";
        }
        writeClipboardToStorage();
        enablePaste();
    }

    void enablePaste() {
        if (clipboard == null || clipboard.length() == 0)
                readClipboardFromStorage();
        pasteItem.setEnabled(clipboard != null && clipboard.length() > 0);
    }

    void doDuplicate() {
        int i;
        String s = "";
        setMenuSelection();
        for (i = 0; i != dragObjects.size(); i++) {
        	DragObject ce = dragObjects.get(i);
        	if (ce.isSelected())
        		s += ce.dump() + "\n";
        }
        doPaste(s);
    }

    void doPaste(String dump) {
        pushUndo();
        clearSelection();
        int i;
        int oldsz = dragObjects.size();
        if (dump != null)
            readImport(dump, true);
        else {
            readClipboardFromStorage();
            readImport(clipboard, true);
        }

        // select new items
        for (i = oldsz; i != dragObjects.size(); i++) {
        	DragObject ce = dragObjects.get(i);
        	int j;
        	// make sure new items are not on top of old items
        	for (j = 0; j != oldsz; j++) {
        		if (ce.boundingBox().equals(dragObjects.get(j).boundingBox())) {
        			// move new one slightly
        			ce.drag(windowWidth/32, 0);
        			j = -1;
        		}
        	}
        	ce.setSelected(true);
        }
        if (dragObjects.size() == oldsz+1)
        	selectedObject = dragObjects.get(oldsz);
        preserveSelection = true;
        wallsChanged();
    }

    void clearSelection() {
        int i;
        for (i = 0; i != dragObjects.size(); i++) {
        	DragObject ce = dragObjects.get(i);
            ce.setSelected(false);
        }
        selectedObject = null;
        preserveSelection = false;
    }
    
    void doSelectAll() {
        int i;
        for (i = 0; i != dragObjects.size(); i++) {
        	DragObject ce = dragObjects.get(i);
        	ce.setSelected(true);
        }
        selectedObject = null;
        preserveSelection = true;
    }

    void createNewLoadFile() {
        // This is a hack to fix what IMHO is a bug in the <INPUT FILE element
        // reloading the same file doesn't create a change event so importing the same file twice
        // doesn't work unless you destroy the original input element and replace it with a new one
        int idx=verticalPanel.getWidgetIndex(loadFileInput);
        LoadFile newlf=new LoadFile(this);
        verticalPanel.insert(newlf, idx);
        verticalPanel.remove(idx+1);
        loadFileInput=newlf;
    }

    boolean useFreqTimeZero() {
    	// automatically adjust phase to avoid discontinuities in sine wave when we change frequencies.
    	// But don't do that if the user manually adjusts phase in one of the sources.
        int i;
        for (i = 0; i != dragObjects.size(); i++) {
        	DragObject ce = dragObjects.get(i);
        	if (ce instanceof Source) {
        		Source s = (Source) ce;
        		if (s.phaseShift != 0)
        			return false;
        	}
        }
        return true;
    }
}
