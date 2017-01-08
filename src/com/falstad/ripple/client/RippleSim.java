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
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
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
	int gw;
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
	Checkbox fixedEndsCheck;
	Checkbox view3dCheck;
	Choice modeChooser;
	Choice sourceChooser;
	Choice setupChooser;
	Choice colorChooser;
	Vector<Setup> setupList;
	Vector<Setup> oldSetupList;
	Vector<DragObject> dragObjects;
	DragObject selectedObject;
	DragHandle draggingHandle;
	Setup setup;
	Scrollbar dampingBar;
	Scrollbar speedBar;
	Scrollbar freqBar;
	Scrollbar resBar;
	Scrollbar brightnessBar;
	Scrollbar auxBar;
	Label auxLabel;
	double dampcoef;
	double freqTimeZero;
	double movingSourcePos = 0;
	double brightMult = 1;
	double zoom3d = 1.2;
	static final double pi = 3.14159265358979323846;
	float func[];
	float funci[];
	float damp[];
	boolean walls[];
	boolean exceptional[];
	int medium[];
	OscSource sources[];
	static final int MODE_SETFUNC = 0;
	static final int MODE_WALLS = 1;
	static final int MODE_MEDIUM = 2;
	static final int MODE_FUNCHOLD = 3;
	int dragX, dragY, dragStartX = -1, dragStartY;
	int selectedSource = -1;
	int sourceIndex;
	int freqBarValue;
	boolean dragging;
	boolean dragClear;
	boolean dragSet;
	public boolean useFrame;
	boolean showControls;
	boolean changedWalls;
	double t;
	int iters;
	// MemoryImageSource imageSource;
	CanvasPixelArray pixels;
	int sourceCount = -1;
	boolean sourcePlane = false;
	boolean sourceMoving = false;
	boolean increaseResolution = false;
	boolean adjustResolution = true;
	boolean rotationMode = false;
	int sourceFreqCount = -1;
	int sourceWaveform = SWF_SIN;
	int auxFunction;
	long startTime;
	MenuBar mainMenuBar;
	MenuBar elmMenuBar;
    MenuItem elmEditMenuItem;
    MenuItem elmDeleteMenuItem;
    MenuItem elmRotateMenuItem;
	Color wallColor, posColor, negColor, zeroColor, medColor, posMedColor,
			negMedColor, sourceColor;
	Color schemeColors[][];
	Point dragPoint;
	// Method timerMethod;
	int timerDiv;
	ImportDialog impDialog;
	static final int mediumMax = 191;
	static final double mediumMaxIndex = .5;
	static final int SWF_SIN = 0;
	static final int SWF_SQUARE = 1;
	static final int SWF_PULSE = 2;
	static final int AUX_NONE = 0;
	static final int AUX_PHASE = 1;
	static final int AUX_FREQ = 2;
	static final int AUX_SPEED = 3;
	static final int SRC_NONE = 0;
	static final int SRC_1S1F = 1;
	static final int SRC_2S1F = 3;
	static final int SRC_2S2F = 4;
	static final int SRC_4S1F = 6;
	static final int SRC_1S1F_PULSE = 8;
	static final int SRC_1S1F_MOVING = 9;
	static final int SRC_1S1F_PLANE = 10;
	static final int SRC_2S1F_PLANE = 12;
	static final int SRC_1S1F_PLANE_PULSE = 14;
	static final int SRC_1S1F_PLANE_PHASE = 15;
	static final int SRC_6S1F = 16;
	static final int SRC_8S1F = 17;
	static final int SRC_10S1F = 18;
	static final int SRC_12S1F = 19;
	static final int SRC_16S1F = 20;
	static final int SRC_20S1F = 21;

//	Frame iFrame;
	DockLayoutPanel layoutPanel;
	VerticalPanel verticalPanel;
	AbsolutePanel absolutePanel;
	Rectangle ripArea;
	Canvas cv;
	Context2d cvcontext;
	Canvas backcv;
	Label coordsLabel;
	Context2d backcontext;
	HandlerRegistration handler;
	DialogBox dialogBox;
	int verticalPanelWidth;
	static RippleSim theSim;
    static EditDialog editDialog;

	static final int MENUBARHEIGHT = 30;
	static final int MAXVERTICALPANELWIDTH = 166;
	static final int POSTGRABSQ = 16;

	final Timer timer = new Timer() {
		public void run() {
			
			updateRipple();
		}
	};
	final int FASTTIMER = 16;

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
//		height = height - MENUBARHEIGHT;   // put this back in if we add a menu bar
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
			auxBar.setWidth(verticalPanelWidth);
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

	static native void passCanvas(CanvasElement cv) /*-{
		$doc.passCanvas(cv, this);
	}-*/;

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

	static native void drawPoke(int x, int y) /*-{
		this.drawPoke(x, y);
	}-*/;

	static native void drawLineSource(int x1, int y1, int x2, int y2, double value) /*-{
		this.drawLineSource(x1, y1, x2, y2, value);
	}-*/;

	static native void drawPhasedArray(int x1, int y1, int x2, int y2, double w1, double w2) /*-{
		this.drawPhasedArray(x1, y1, x2, y2, w1, w2);
	}-*/;

	static native void drawWall(int x1, int y1, int x2, int y2) /*-{
		this.drawWall(x1, y1, x2, y2);
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

	static native void setColors(int wallColor, int posColor, int negColor,
			int zeroColor, int posMedColor, int negMedColor,
			int medColor, int sourceColor, int zeroColor3d) /*-{
		this.setColors(wallColor, posColor, negColor, zeroColor, posMedColor, negMedColor,
			medColor, sourceColor, zeroColor3d);
	}-*/;

	public void init() {
		theSim = this;
//		logger.log(Level.SEVERE, "RAwr");
		
		cv = Canvas.createIfSupported();
		passCanvas(cv.getCanvasElement());
		if (cv == null) {
			RootPanel
					.get()
					.add(new Label(
							"Not working. You need a browser that supports the CANVAS element."));
			return;
		}

		sources = new OscSource[20];
		dragObjects = new Vector<DragObject>();
		cvcontext = cv.getContext2d();
//		backcv = Canvas.createIfSupported();
//		backcontext = backcv.getContext2d();
		setCanvasSize();
		layoutPanel = new DockLayoutPanel(Unit.PX);
		verticalPanel = new VerticalPanel();
		
		setupList = new Vector<Setup>();
		oldSetupList = new Vector<Setup>();
		Setup s = new SingleSourceSetup();
		while (s != null) {
		    oldSetupList.addElement(s);
		    s = s.createNext();
		}

		setupChooser = new Choice();
		setupChooser.addChangeHandler(this);
//		setupChooser.addItemListener(this);
		getSetupList(false);
		
		sourceChooser = new Choice();
		sourceChooser.add("No Sources");
		sourceChooser.add("1 Src, 1 Freq");
		sourceChooser.add("1 Src, 2 Freq");
		sourceChooser.add("2 Src, 1 Freq");
		sourceChooser.add("2 Src, 2 Freq");
		sourceChooser.add("3 Src, 1 Freq");
		sourceChooser.add("4 Src, 1 Freq");
		sourceChooser.add("1 Src, 1 Freq (Square)");
		sourceChooser.add("1 Src, 1 Freq (Pulse)");
		sourceChooser.add("1 Moving Src");
		sourceChooser.add("1 Plane Src, 1 Freq");
		sourceChooser.add("1 Plane Src, 2 Freq");
		sourceChooser.add("2 Plane Src, 1 Freq");
		sourceChooser.add("2 Plane Src, 2 Freq");
		sourceChooser.add("1 Plane 1 Freq (Pulse)");
		sourceChooser.add("1 Plane 1 Freq w/Phase");
		sourceChooser.add("6 Src, 1 Freq");
		sourceChooser.add("8 Src, 1 Freq");
		sourceChooser.add("10 Src, 1 Freq");
		sourceChooser.add("12 Src, 1 Freq");
		sourceChooser.add("16 Src, 1 Freq");
		sourceChooser.add("20 Src, 1 Freq");
		sourceChooser.select(SRC_1S1F);
		sourceChooser.addChangeHandler(this);

		modeChooser = new Choice();
		modeChooser.add("Mouse = Edit Wave");
		modeChooser.add("Mouse = Edit Walls");
		modeChooser.add("Mouse = Edit Medium");
		modeChooser.add("Mouse = Hold Wave");
		modeChooser.addChangeHandler(this);
		
		colorChooser = new Choice();
		colorChooser.addChangeHandler(this);
		
		
		verticalPanel.add(setupChooser);
//		verticalPanel.add(sourceChooser);
//		verticalPanel.add(modeChooser);
		verticalPanel.add(colorChooser);
		verticalPanel.add(blankButton = new Button("Clear Waves"));
		blankButton.addClickHandler(this);
		verticalPanel.add(blankWallsButton = new Button("Clear Walls"));
		blankWallsButton.addClickHandler(this);
		verticalPanel.add(exportButton = new Button("Import/Export"));
		exportButton.addClickHandler(this);

		verticalPanel.add(stoppedCheck = new Checkbox("Stopped"));
		verticalPanel.add(fixedEndsCheck = new Checkbox("Fixed Edges"));
		verticalPanel.add(view3dCheck = new Checkbox("3-D View"));

		int res = 512;
		verticalPanel.add(new Label("Simulation Speed"));
		verticalPanel.add(speedBar = new Scrollbar(Scrollbar.HORIZONTAL, 8, 1, 1, 30));
		verticalPanel.add(new Label("Resolution"));
		verticalPanel.add(resBar = new Scrollbar(Scrollbar.HORIZONTAL, res, 5, 256, 1024));
		resBar.addClickHandler(this);
		setResolution();
		verticalPanel.add(new Label("Damping"));
		verticalPanel.add(dampingBar = new Scrollbar(Scrollbar.HORIZONTAL, 10, 1, 2, 100));
		dampingBar.addClickHandler(this);
		verticalPanel.add(new Label("Source Frequency"));
		verticalPanel.add(freqBar = new Scrollbar(Scrollbar.HORIZONTAL, freqBarValue = 15, 1, 1, 30,
				new Command() {
			public void execute() { setFreq(); }
		}));
//		freqBar.addClickHandler(this);
		verticalPanel.add(new Label("Brightness"));
		verticalPanel.add(brightnessBar = new Scrollbar(Scrollbar.HORIZONTAL, 27, 1, 1, 1200));
//		auxBar = new Scrollbar(Scrollbar.HORIZONTAL, 1, 1, 1, 30);
		verticalPanel.add(auxLabel = new Label("Aux Bar"));
		verticalPanel.add(auxBar = new Scrollbar(Scrollbar.HORIZONTAL, 1, 1, 1, 30));
		auxLabel.setVisible(false);
		auxBar.setVisible(false);
		verticalPanel.add(new Label("http://www.falstad.com"));
		resBar.setWidth(verticalPanelWidth);
		dampingBar.setWidth(verticalPanelWidth);
		speedBar.setWidth(verticalPanelWidth);
		freqBar.setWidth(verticalPanelWidth);
		brightnessBar.setWidth(verticalPanelWidth);
		auxBar.setWidth(verticalPanelWidth);

		absolutePanel = new AbsolutePanel();
		coordsLabel = new Label("(0,0)");
		coordsLabel.setStyleName("coordsLabel");
		absolutePanel.add(cv);
		absolutePanel.add(coordsLabel, 0, 0);
		
		layoutPanel.addEast(verticalPanel, verticalPanelWidth);
		layoutPanel.add(absolutePanel);
		RootLayoutPanel.get().add(layoutPanel);

		mainMenuBar = new MenuBar(true);
		mainMenuBar.setAutoOpen(true);
		composeMainMenu(mainMenuBar);
		
        elmMenuBar = new MenuBar(true);
        elmMenuBar.addItem(elmEditMenuItem = new MenuItem("Edit",new MyCommand("elm","edit")));
//        elmMenuBar.addItem(elmCutMenuItem = new MenuItem("Cut",new MyCommand("elm","cut")));
//        elmMenuBar.addItem(elmCopyMenuItem = new MenuItem("Copy",new MyCommand("elm","copy")));
        elmMenuBar.addItem(elmDeleteMenuItem = new MenuItem("Delete",new MyCommand("elm","delete")));
        elmMenuBar.addItem(elmRotateMenuItem = new MenuItem("Rotate",new MyCommand("elm","rotate")));
//        elmMenuBar.addItem(                    new MenuItem("Duplicate",new MyCommand("elm","duplicate")));

//		winSize = new Dimension(256, 256);
//		if (pixels == null) {
//		    pixels = new int[winSize.width*winSize.height];
//		    int j;
//		    for (j = 0; j != winSize.width*winSize.height; j++)
//		    	pixels[j] = 0xFF000000;
//		    
//		    
//		}
		
		
		schemeColors = new Color[20][8];
		if (colorChooser.getItemCount() == 0)
		    addDefaultColorScheme();
		doColor();
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
    	mainMenuBar.addItem(getClassCheckItem("Add Source", "Source"));
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
    	mainMenuBar.addItem(getClassCheckItem("Options...", "Options"));
    }

    MenuItem getClassCheckItem(String s, String t) {
        return new MenuItem(s, new MyCommand("main", t));
    }

    public void wallsChanged() {
    	changedWalls = true;
    }
    
    public void menuPerformed(String menu, String item) {
    	if (contextPanel != null)
    		contextPanel.hide();
    	if (item == "delete") {
    		if (selectedObject != null) {
    			dragObjects.remove(selectedObject);
    			selectedObject = null;
    			wallsChanged();
    		}
    	}
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
    	if (item == "LineSource")
    		newObject = new LineSource();
    	if (item == "PhasedArraySource")
    		newObject = new PhasedArraySource();
    	if (item == "MultipoleSource")
    		newObject = new MultipoleSource();
    	if (item == "Slit")
    		newObject = new Slit();
    	if (newObject != null) {
    		newObject.setInitialPosition();
    		dragObjects.add(newObject);
    		setSelectedObject(newObject);
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
    	if (tint == 201) return new PhasedArraySource(st);
    	if (tint == 203) return new Slit(st);
    	if (tint == 202) return new SolidBox(st);
    	if (tint == 's') return new Source(st, 1);
    	if (tint == 't') return new TrianglePrism(st);
    	if (tint == 'w') return new Wall(st);
    	return null;
    }
    
	void createWall(int x1, int y1, int x2, int y2) {
		console("createwall " + x1 + " " + y1 + " " +x2 + " "  + y2);
		Wall w = new Wall(x1-windowOffsetX, y1-windowOffsetY,
				x2-windowOffsetX, y2-windowOffsetY);
		dragObjects.add(w);
		changedWalls = true;
	}
	
	void setWall(int x, int y) {
		walls[x + gw * y] = true;
	}

	void setWall(int x, int y, boolean b) {
		walls[x + gw * y] = b;
	}

	void setMedium(int x, int y, int q) {
		medium[x + gw * y] = q;
	}

    void doEdit(Editable eable) {
//        clearSelection();
//        pushUndo();
        if (editDialog != null) {
    //          requestFocus();
                editDialog.setVisible(false);
                editDialog = null;
        }
        editDialog = new EditDialog(eable, this);
        editDialog.show();
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
		gw = gridSizeY;
		func = new float[gridSizeXY];
		funci = new float[gridSizeXY];
		damp = new float[gridSizeXY];
		exceptional = new boolean[gridSizeXY];
		medium = new int[gridSizeXY];
		walls = new boolean[gridSizeXY];
		int i, j;
		for (i = 0; i != gridSizeXY; i++)
			damp[i] = 1f; // (float) dampcoef;
		for (i = 0; i != windowOffsetX; i++)
			for (j = 0; j != gridSizeX; j++)
				damp[i + j * gw] = damp[gridSizeX - 1 - i + gw * j] = damp[j
						+ gw * i] = damp[j + (gridSizeY - 1 - i) * gw] = (float) (.999 - (windowOffsetX - i) * .002);
		if (setup)
			doSetup();

	}

	void drawWalls() {
		doBlankWalls();
		int i;
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			double xform[] = obj.transform;
			setTransform(xform[0], xform[1], xform[2], xform[3], xform[4], xform[5]);
			obj.prepare();
		}
		setTransform(1, 0, 0, 0, 1, 0);
	}
	
	public void updateRipple() {
			if (changedWalls) {
				drawWalls();
				changedWalls = false;
			}
			if (stoppedCheck.getState())
				return;
			long time = System.currentTimeMillis();
			int iterCount = speedBar.getValue();
			int i;
			setAcoustic(!fixedEndsCheck.getState());
			for (i = 0; i != iterCount; i++) {
				simulate();
				t += .25;
//				doSources(.25);
				int j;
				for (j = 0; j != dragObjects.size(); j++)
					dragObjects.get(j).run();
				iters++;
				// limit frame time
				if (System.currentTimeMillis()-time > 100)
					break;
			}
//			console("total time = " + (System.currentTimeMillis()-time));
			brightMult = Math.exp(brightnessBar.getValue() / 100. - 5.);
			updateRippleGL(brightMult, view3dCheck.getState());
			if (!view3dCheck.getState())
				for (i = 0; i != dragObjects.size(); i++) {
					DragObject obj = dragObjects.get(i);
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
	}

	int abs(int x) {
		return x < 0 ? -x : x;
	}

	int sign(int x) {
		return (x < 0) ? -1 : (x == 0) ? 0 : 1;
	}

	void setDamping() {
		/*
		 * int i; double damper = dampingBar.getValue() * .00002;// was 5
		 * dampcoef = Math.exp(-damper);
		 */
		dampcoef = 1;
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
		console("setfreq " + newfreq);
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			if (obj instanceof Source) {
				console("src " + obj);
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
		dragObjects.removeAllElements();
		selectedObject = null;
		doBlankWalls();
	}
	
	void doSetup() {
		if (setupList.size() == 0)
			return;
		t = 0;
		if (resBar.getValue() < 32)
			setResolution(32);
		doBlank();
		deleteAllObjects();
		// don't use previous source positions, use defaults
		sourceCount = -1;
		sourceChooser.select(SRC_1S1F);
		dampingBar.setValue(10);
		setFreqBar(5);
		setBrightness(10);
		auxBar.setValue(1);
		fixedEndsCheck.setState(true);
		setup = (Setup) setupList.elementAt(setupChooser.getSelectedIndex());
		setup.select();
		setup.doSetupSources();
		int i;
		if (sourcePlane) {
			for (i = 0; i != sourceCount; i += 2)
				dragObjects.add(new LineSource(sources[i].x-windowOffsetX, sources[i].y-windowOffsetY,
						sources[i+1].x-windowOffsetX, sources[i+1].y-windowOffsetY));
		} else
			for (i = 0; i != sourceCount; i++)
				dragObjects.add(new Source(sources[i].x-windowOffsetX, sources[i].y-windowOffsetY));
		setDamping();
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

	void setSources() {
		sourceIndex = sourceChooser.getSelectedIndex();
		int oldSCount = sourceCount;
		boolean oldPlane = sourcePlane;
		sourceFreqCount = 1;
		sourcePlane = (sourceChooser.getSelectedIndex() >= SRC_1S1F_PLANE && sourceChooser
				.getSelectedIndex() < SRC_6S1F);
		sourceMoving = false;
		sourceWaveform = SWF_SIN;
		sourceCount = 1;
		boolean phase = false;
		switch (sourceChooser.getSelectedIndex()) {
		case 0:
			sourceCount = 0;
			break;
		case 2:
			sourceFreqCount = 2;
			break;
		case 3:
			sourceCount = 2;
			break;
		case 4:
			sourceCount = 2;
			sourceFreqCount = 2;
			break;
		case 5:
			sourceCount = 3;
			break;
		case 6:
			sourceCount = 4;
			break;
		case 7:
			sourceWaveform = SWF_SQUARE;
			break;
		case 8:
			sourceWaveform = SWF_PULSE;
			break;
		case 9:
			sourceMoving = true;
			break;
		case 11:
			sourceFreqCount = 2;
			break;
		case 12:
			sourceCount = 2;
			break;
		case 13:
			sourceCount = sourceFreqCount = 2;
			break;
		case 14:
			sourceWaveform = SWF_PULSE;
			break;
		case 15:
			phase = true;
			break;
		case 16:
			sourceCount = 6;
			break;
		case 17:
			sourceCount = 8;
			break;
		case 18:
			sourceCount = 10;
			break;
		case 19:
			sourceCount = 12;
			break;
		case 20:
			sourceCount = 16;
			break;
		case 21:
			sourceCount = 20;
			break;
		}
		if (sourceFreqCount >= 2) {
			auxFunction = AUX_FREQ;
			auxBar.setValue(freqBar.getValue());
			if (sourceCount == 2)
				auxLabel.setText("Source 2 Frequency");
			else
				auxLabel.setText("2nd Frequency");
		} else if (sourceCount == 2 || sourceCount >= 4 || phase) {
			auxFunction = AUX_PHASE;
			auxBar.setValue(1);
			auxLabel.setText("Phase Difference");
		} else if (sourceMoving) {
			auxFunction = AUX_SPEED;
			auxBar.setValue(7);
			auxLabel.setText("Source Speed");
		} else {
			auxFunction = AUX_NONE;
			 auxBar.setVisible(false);
			 auxLabel.setVisible(false);
		}
		if (auxFunction != AUX_NONE) {
			 auxBar.setVisible(true);
			 auxLabel.setVisible(true);
		}
		// validate();

		if (sourcePlane) {
			sourceCount *= 2;
			if (!(oldPlane && oldSCount == sourceCount)) {
				int x2 = windowOffsetX + windowWidth - 1;
				int y2 = windowOffsetY + windowHeight - 1;
				sources[0] = new OscSource(windowOffsetX, windowOffsetY + 1);
				sources[1] = new OscSource(x2, windowOffsetY + 1);
				sources[2] = new OscSource(windowOffsetX, y2);
				sources[3] = new OscSource(x2, y2);
			}
		} else if (!(oldSCount == sourceCount && !oldPlane)) {
			sources[0] = new OscSource(gridSizeX / 2, windowOffsetY + 1);
			sources[1] = new OscSource(gridSizeX / 2, gridSizeY - windowOffsetY
					- 2);
			sources[2] = new OscSource(windowOffsetX + 1, gridSizeY / 2);
			sources[3] = new OscSource(gridSizeX - windowOffsetX - 2,
					gridSizeY / 2);
			int i;
			for (i = 4; i < sourceCount; i++)
				sources[i] = new OscSource(windowOffsetX + 1 + i * 2,
						gridSizeY / 2);
		}
	}

	class OscSource {
		int x;
		int y;
		float v;

		OscSource(int xx, int yy) {
			x = xx;
			y = yy;
		}

		int getScreenX() {
			return ((x - windowOffsetX) * winSize.width + winSize.width / 2)
					/ windowWidth;
		}

		int getScreenY() {
			return ((y - windowOffsetY) * winSize.height + winSize.height / 2)
					/ windowHeight;
		}
	};


    void getSetupList(final boolean openDefault) {

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
					processSetupList(text.getBytes(), text.length(), openDefault);
					// end or processing
					}
					else 
						GWT.log("Bad file server response:"+response.getStatusText() );
				}
			});
		} catch (RequestException e) {
			GWT.log("failed file reading", e);
		}
    }
		
    void processSetupList(byte b[], int len, final boolean openDefault) {
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
    				Setup s = null;
    				try {
    					int num = Integer.parseInt(file);
    					s = oldSetupList.get(num);
    					title += " (old)";
    				} catch (NumberFormatException e) {
        				s = new FileSetup(title, file);
    				}
    				setupList.add(s);
    				setupChooser.add("Example: " + title);
    			}
    		}
    		p += l;
    	}
}


	void readSetupFile(String str, String title) {
		t = 0;
		console("reading example " + str);
			String url=GWT.getModuleBaseURL()+"examples/"+str+"?v="+random.nextInt(); 
			loadFileFromURL(url);
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

	void doImport() {
		if (impDialog != null) {
//			requestFocus();
			impDialog.setVisible(false);
			impDialog = null;
		}
		
		String dump = "";

		int i;
		dump = "$ 1 " + windowWidth + " " + windowOffsetX + " " + dampingBar.getValue() + " " +
				fixedEndsCheck.getState() + " " + brightnessBar.getValue() + "\n";
/*		for (i = 0; i != sourceCount; i++) {
			OscSource src = sources[i];
			dump += "s " + src.x + " " + src.y + "\n";
		}*/
		for (i = 0; i != dragObjects.size(); i++) {
			DragObject obj = dragObjects.get(i);
			dump += obj.dump() + "\n";
		}
		DialogBox dial = new DialogBox();
		
		impDialog = new ImportDialog(dial, dump, this);
		dial.setWidget(impDialog);
		dial.center();
		dial.show();
	}

	void readImport(String s) {
		doBlank();
		deleteAllObjects();
		char b[] = new char[s.length()];
		s.getChars(0, s.length(), b, 0);
		int len = s.length();
		int p;
		int x = 0;
		int srci = 0;
		setupChooser.select(0);
		setup = (Setup) setupList.elementAt(0);
		for (p = 0; p < len;) {
			int l;
			int linelen = 0;
			for (l = 0; l != len - p; l++)
				if (b[l + p] == '\n' || b[l + p] == '\r') {
					linelen = l++;
					if (l + p < b.length && b[l + p] == '\n')
						l++;
					break;
				}
			String line = new String(b, p, linelen);
			StringTokenizer st = new StringTokenizer(line);
			while (st.hasMoreTokens()) {
				String type = st.nextToken();
				int tint = type.charAt(0);
				try {
					if (tint == '$') {
						int flags = new Integer(st.nextToken()).intValue();
						if ((flags & 1) == 0)
							return;

//						dump = "$ 1 " + windowWidth + " " + windowOffsetX + " " +
//								fixedEndsCheck.getState() + " " + brightnessBar.getValue() + "\n";

						int ww = Integer.parseInt(st.nextToken());
						int wo = Integer.parseInt(st.nextToken());
						setResolution(ww, wo);
						reinit(false);

						dampingBar.setValue(Integer.parseInt(st.nextToken()));
						fixedEndsCheck.setState(st.nextToken()
								.compareTo("true") == 0);
						brightnessBar.setValue(new Integer(st.nextToken())
								.intValue());
						break;
					}
                    if (tint >= '0' && tint <= '9')
                        tint = new Integer(type).intValue();
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
	}

	 class ImportDialog extends Composite implements ClickHandler {
	
		 Button importButton, clearButton, closeButton;
		 TextArea text;
		 DialogBox dialogBox;
		 RippleSim rframe;
		 VerticalPanel panel;
	
		 ImportDialog(DialogBox f, String str, RippleSim rFrame ) {
			 initWidget(panel = new VerticalPanel());
			 dialogBox = f;
//			 super(f, (str.length() > 0) ? "Export" : "Import", false);
			 rframe = rFrame;
//			 setLayout(new ImportDialogLayout());
			 panel.add(text = new TextArea());
			 text.setValue(str);
			 text.setHeight("120px");
			 text.setWidth("180px");
			 HorizontalPanel hPanel = new HorizontalPanel();
			 hPanel.add(importButton = new Button("Import"));
			 importButton.addClickHandler(this);
			 hPanel.add(clearButton = new Button("Clear"));
			 clearButton.addClickHandler(this);
			 hPanel.add(closeButton = new Button("Close"));
			 closeButton.addClickHandler(this);
			 
			 panel.add(hPanel);
//			 Point x = rframe.get
//			 resize(400, 300);
//			 Dimension d = getSize();
//			 setLocation(x.x + (winSize.width - d.width) / 2, x.y
//			 + (winSize.height - d.height) / 2);
			
			 
			 if (str.length() > 0)
				 text.selectAll();
		 }
	
		
		
		 

		@Override
		public void onClick(ClickEvent event) {
			int i;
			 Object src = event.getSource();
			 if (src == importButton) {
				 rframe.readImport(text.getText());
				 dialogBox.hide();
			 }
			 if (src == closeButton)
				 dialogBox.hide();
			 if (src == clearButton)
				 text.setText("");
		}
	 }

	abstract class Setup {
		abstract String getName();

		void select() {}

		void doSetupSources() {
			setSources();
		}

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
	
	class SingleSourceSetup extends Setup {
		
		String getName() {
			return "Single Source";
		}

		void select() {
			setFreqBar(15);
			setBrightness(27);
		}

		Setup createNext() {
			return new DoubleSourceSetup();
		}
	}

	class DoubleSourceSetup extends Setup {
		String getName() {
			return "Two Sources";
		}

		void select() {
			setFreqBar(15);
			setBrightness(19);
		}

		void doSetupSources() {
			sourceChooser.select(SRC_2S1F);
			setSources();
			sources[0].y = gridSizeY / 2 - 8;
			sources[1].y = gridSizeY / 2 + 8;
			sources[0].x = sources[1].x = gridSizeX / 2;
		}

		Setup createNext() {
			return new QuadrupleSourceSetup();
		}
	}

	class QuadrupleSourceSetup extends Setup {
		String getName() {
			return "Four Sources";
		}

		void select() {
			sourceChooser.select(SRC_4S1F);
			setFreqBar(15);
		}

		Setup createNext() {
			return new SingleSlitSetup();
		}
	}

	class SingleSlitSetup extends Setup {
		String getName() {
			return "Single Slit";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			int i;
			int x = gridSizeX / 2;
			int y = windowOffsetY + 8; // +4
			createWall(0, y, x-9, y);
			createWall(x+9, y, gridSizeX-1, y);
//			for (i = 0; i != gridSizeX; i++)
//				setWall(i, y);
//			for (i = -8; i <= 8; i++)
//				 was 4
//				setWall(x + i, y, false);
			setBrightness(7);
			setFreqBar(25);
		}

		Setup createNext() {
			return new DoubleSlitSetup();
		}
	}

	class DoubleSlitSetup extends Setup {
		String getName() {
			return "Double Slit";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			int i;
			int x = gridSizeX / 2;
			int y = windowOffsetY + 4;
			for (i = 0; i != gridSizeX; i++)
				setWall(i, y);
			for (i = 0; i != 3; i++) {
				setWall(x - 5 - i, y, false);
				setWall(x + 5 + i, y, false);
			}
			brightnessBar.setValue(488);
			setFreqBar(22);
		}

		Setup createNext() {
			return new TripleSlitSetup();
		}
	}

	class TripleSlitSetup extends Setup {
		String getName() {
			return "Triple Slit";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			int i;
			int x = gridSizeX / 2;
			int y = windowOffsetY + 4;
			for (i = 0; i != gridSizeX; i++)
				setWall(i, y);
			for (i = -1; i <= 1; i++) {
				setWall(x - 12 + i, y, false);
				setWall(x + i, y, false);
				setWall(x + 12 + i, y, false);
			}
			setBrightness(12);
			setFreqBar(22);
		}

		Setup createNext() {
			return new ObstacleSetup();
		}
	}

	class ObstacleSetup extends Setup {
		String getName() {
			return "Obstacle";
		}

		void select() {
			int i;
			int x = gridSizeX / 2;
			int y = windowOffsetY + 12; // was +6
			createWall(x-15, y, x+15, y);
//			for (i = -15; i <= 15; i++)
//				 was 5
//				setWall(x + i, y);
			setBrightness(280);
			setFreqBar(20);
		}

		Setup createNext() {
			return new HalfPlaneSetup();
		}
	}

	class HalfPlaneSetup extends Setup {
		String getName() {
			return "Half Plane";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			int x = windowOffsetX + windowWidth / 2;
			int i;
			createWall(windowOffsetX+windowWidth/2, windowOffsetY+3,
					windowOffsetX+windowWidth-1, windowOffsetY+3);
//			for (i = windowWidth / 2; i < windowWidth; i++)
//				setWall(windowOffsetX + i, windowOffsetY + 3);
			setBrightness(4);
			setFreqBar(25);
		}

		Setup createNext() {
			return new DipoleSourceSetup();
		}
	}

	class DipoleSourceSetup extends Setup {
		String getName() {
			return "Dipole Source";
		}

		void doSetupSources() {
			sourceChooser.select(SRC_2S1F);
			setSources();
			sources[0].y = sources[1].y = gridSizeY / 2;
			sources[0].x = gridSizeX / 2 - 1;
			sources[1].x = gridSizeX / 2 + 1;
			
			
			auxBar.setValue(29);
			setFreqBar(13);
		}

		void select() {
			setBrightness(33);
		}

		Setup createNext() {
			return new LateralQuadrupoleSetup();
		}
	}

	class LateralQuadrupoleSetup extends Setup {
		String getName() {
			return "Lateral Quadrupole";
		}

		void doSetupSources() {
			sourceChooser.select(SRC_4S1F);
			setSources();
			sources[0].y = sources[2].y = gridSizeY / 2;
			sources[0].x = gridSizeX / 2 - 2;
			sources[2].x = gridSizeX / 2 + 2;
			sources[1].x = sources[3].x = gridSizeX / 2;
			sources[1].y = gridSizeY / 2 - 2;
			sources[3].y = gridSizeY / 2 + 2;
			
			
			setFreqBar(13);
			auxBar.setValue(29);
		}

		void select() {
			setBrightness(33);
		}

		Setup createNext() {
			return new LinearQuadrupoleSetup();
		}
	}

	class LinearQuadrupoleSetup extends Setup {
		String getName() {
			return "Linear Quadrupole";
		}

		void doSetupSources() {
			sourceChooser.select(SRC_4S1F);
			setSources();
			sources[0].y = sources[1].y = sources[2].y = sources[3].y = gridSizeY / 2;
			sources[0].x = gridSizeX / 2 - 3;
			sources[2].x = gridSizeX / 2 + 3;
			sources[1].x = gridSizeX / 2 + 1;
			sources[3].x = gridSizeX / 2 - 1;
			
			
			auxBar.setValue(29);
			setFreqBar(13);
		}

		void select() {
			setBrightness(33);
		}

		Setup createNext() {
			return new HexapoleSetup();
		}
	}

	class HexapoleSetup extends Setup {
		String getName() {
			return "Hexapole";
		}

		void doSetupSources() {
			sourceChooser.select(SRC_6S1F);
			setSources();
			doMultipole(6, 4);
			
			
			setFreqBar(22);
			auxBar.setValue(29);
		}

		void doMultipole(int n, double dist) {
			int i;
//			sources = new OscSource[20];
			for (i = 0; i != n; i++) {

				double xx = Math.round(dist * Math.cos(2 * pi * i / n));
	
				double yy = Math.round(dist * Math.sin(2 * pi * i / n));
				if(sources[i]==null)
					sources[i] = new OscSource(0, 0);
				sources[i].x = gridSizeX / 2 + (int) xx;
		
				sources[i].y = gridSizeY / 2 + (int) yy;
			}
		}

		void select() {
			brightnessBar.setValue(648);
		}

		Setup createNext() {
			return new OctupoleSetup();
		}
	}

	class OctupoleSetup extends HexapoleSetup {
		String getName() {
			return "Octupole";
		}

		void doSetupSources() {
			sourceChooser.select(SRC_8S1F);
			setSources();
			doMultipole(8, 4);
			
			
			setFreqBar(22);
			auxBar.setValue(29);
		}

		Setup createNext() {
			return new Multi12Setup();
		}
	}

	/*
	 * class Multi10Setup extends HexapoleSetup { String getName() { return
	 * "10-Pole"; } void doSetupSources() { sourceChooser.select(SRC_10S1F);
	 * setSources(); doMultipole(10, 6); setFreqBar(22); auxBar.setValue(29); }
	 * Setup createNext() { return new Multi12Setup(); } }
	 */
	class Multi12Setup extends HexapoleSetup {
		String getName() {
			return "12-Pole";
		}

		void doSetupSources() {
			sourceChooser.select(SRC_12S1F);
			setSources();
			doMultipole(12, 6);
			
			
			setFreqBar(22);
			auxBar.setValue(29);
		}

		Setup createNext() {
			return new PlaneWaveSetup();
		}
	}

	/*
	 * class Multi16Setup extends HexapoleSetup { String getName() { return
	 * "16-Pole"; } void doSetupSources() { sourceChooser.select(SRC_16S1F);
	 * setSources(); doMultipole(16, 8); setFreqBar(22); auxBar.setValue(29); }
	 * Setup createNext() { return new Multi20Setup(); } } class Multi20Setup
	 * extends HexapoleSetup { String getName() { return "20-Pole"; } void
	 * doSetupSources() { sourceChooser.select(SRC_20S1F); setSources();
	 * doMultipole(20, 10); setFreqBar(22); auxBar.setValue(29); } Setup
	 * createNext() { return new PlaneWaveSetup(); } }
	 */
	class PlaneWaveSetup extends Setup {
		String getName() {
			return "Plane Wave";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			// setBrightness(7);
			setFreqBar(15);
		}

		Setup createNext() {
			return new IntersectingPlaneWavesSetup();
		}
	}

	class IntersectingPlaneWavesSetup extends Setup {
		String getName() {
			return "Intersecting Planes";
		}

		void select() {
			setBrightness(4);
			setFreqBar(17);
		}

		void doSetupSources() {
			sourceChooser.select(SRC_2S1F_PLANE);
			setSources();
			sources[0].y = sources[1].y = windowOffsetY;
			sources[0].x = windowOffsetX + 1;
			sources[2].x = sources[3].x = windowOffsetX;
			sources[2].y = windowOffsetY + 1;
			sources[3].y = windowOffsetY + windowHeight - 1;
			
		}

		Setup createNext() {
			return new PhasedArray1Setup();
		}
	}

	class PhasedArray1Setup extends Setup {
		String getName() {
			return "Phased Array 1";
		}

		void select() {
			setBrightness(5);
			setFreqBar(17);
		}

		void doSetupSources() {
			sourceChooser.select(SRC_1S1F_PLANE_PHASE);
			setSources();
			sources[0].x = sources[1].x = gridSizeX / 2;

			sources[0].y = gridSizeY/2 - 12;
			sources[1].y = gridSizeY/2 + 12;
			
			auxBar.setValue(5);
			
			
		}

		float calcSourcePhase(double ph, float v, double w) {
			ph *= (auxBar.getValue() - 15) * 3.8 * freqBar.getValue()
					* freqMult;
			return (float) Math.sin(w + ph);
		}

		Setup createNext() {
			return new PhasedArray2Setup();
		}
	}

	class PhasedArray2Setup extends PhasedArray1Setup {
		String getName() {
			return "Phased Array 2";
		}

		void doSetupSources() {
			sourceChooser.select(SRC_1S1F_PLANE_PHASE);
			setSources();
			sources[0].x = sources[1].x = windowOffsetX + 1;
			sources[0].y = windowOffsetY + 1;
			sources[1].y = windowOffsetY + windowHeight - 2;
			
			auxBar.setValue(5);
		}

		float calcSourcePhase(double ph, float v, double w) {
			double d = auxBar.getValue() * 2.5 / 30.;
			ph -= .5;
			ph = Math.sqrt(ph * ph + d * d);
			ph *= freqBar.getValue() * freqMult * 108;
			return (float) Math.sin(w + ph);
		}

		Setup createNext() {
			return new PhasedArray3Setup();
		}
	}

	class PhasedArray3Setup extends PhasedArray2Setup {
		String getName() {
			return "Phased Array 3";
		}

		float calcSourcePhase(double ph, float v, double w) {
			double d = auxBar.getValue() * 2.5 / 30.;
			ph -= .5;
			ph = Math.sqrt(ph * ph + d * d);
			ph *= freqBar.getValue() * freqMult * 108;
			return (float) Math.sin(w - ph);
		}

		Setup createNext() {
			return new DopplerSetup();
		}
	}

	class DopplerSetup extends Setup {
		String getName() {
			return "Doppler Effect 1";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_MOVING);
			setFreqBar(13);
			setBrightness(20);
			fixedEndsCheck.setState(false);
		}

		Setup createNext() {
			return new Doppler2Setup();
		}
	}

	class Doppler2Setup extends Setup {
		String getName() {
			return "Doppler Effect 2";
		}

		double wall;
		int dir;
		int waiting;

		void select() {
			wall = gridSizeY / 2.;
			dir = 1;
			waiting = 0;
			setFreqBar(13);
			setBrightness(220);
		}

		void doSetupSources() {
			sourceChooser.select(SRC_1S1F);
			
			sources[0].x = windowOffsetX + 1;
			sources[0].y = windowOffsetY + 1;
			setSources();
		}

		void eachFrame() {
			if (waiting > 0) {
				waiting--;
				return;
			}
			int w1 = (int) wall;
			wall += dir * .04;
			int w2 = (int) wall;
			if (w1 != w2) {
				int i;
				for (i = windowOffsetX + windowWidth / 3; i <= gridSizeX - 1; i++) {
					setWall(i, w1, false);
					setWall(i, w2);
					int gi = i + w1 * gw;
					if (w2 < w1)
						func[gi] = funci[gi] = 0;
					else if (w1 > 1) {
						func[gi] = func[gi - gw] / 2;
						funci[gi] = funci[gi - gw] / 2;
					}
				}
				int w3 = (w2 - windowOffsetY) / 2 + windowOffsetY;
				for (i = windowOffsetY; i < w3; i++)
					setWall(gridSizeX / 2, i);
				setWall(gridSizeX / 2, i, false);
			}
			if (w2 == windowOffsetY + windowHeight / 4
					|| w2 == windowOffsetY + windowHeight * 3 / 4) {
				dir = -dir;
				waiting = 1000;
			}
		}

		Setup createNext() {
			return new SonicBoomSetup();
		}
	}

	class SonicBoomSetup extends Setup {
		String getName() {
			return "Sonic Boom";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_MOVING);
			setFreqBar(13);
			setBrightness(20);
			fixedEndsCheck.setState(false);
		}

		void doSetupSources() {
			setSources();
			auxBar.setValue(30);
		}

		Setup createNext() {
			return new BigModeSetup();
		}
	}

	void setupMode(int x, int y, int sx, int sy, int nx, int ny) {
		ModeBox m = new ModeBox(x-1-windowOffsetX, y-1-windowOffsetY,
				x+sx-windowOffsetX, y+sy-windowOffsetY, nx, ny);
		dragObjects.add(m);
		/*
		int i, j;
		for (i = 0; i != sx; i++)
			for (j = 0; j != sy; j++) {
				int gi = i + x + gw * (j + y);
				func[gi] = (float) (Math.sin(pi * nx * (i + 1) / (sx + 1)) * Math
						.sin(pi * ny * (j + 1) / (sy + 1)));
				funci[gi] = 0;
			}
			*/
	}

	void setupAcousticMode(int x, int y, int sx, int sy, int nx, int ny) {
		setupMode(x, y, sx, sy, nx, ny);
		/*
		int i, j;
		if (nx == 0 && ny == 0)
			return;
		for (i = 0; i != sx; i++)
			for (j = 0; j != sy; j++) {
				int gi = i + x + gw * (j + y);
				func[gi] = (float) (Math.cos(pi * nx * i / (sx - 1)) * Math
						.cos(pi * ny * j / (sy - 1)));
				funci[gi] = 0;
			}
			*/
	}

	class BigModeSetup extends Setup {
		String getName() {
			return "Big 1x1 Mode";
		}

		void select() {
			sourceChooser.select(SRC_NONE);
			int i;
			int n = windowWidth * 3 / 4;
			int x = windowOffsetX + windowWidth / 2 - n / 2;
			int y = windowOffsetY + windowHeight / 2 - n / 2;
			for (i = 0; i != n + 2; i++) {
				setWall(x + i - 1, y - 1);
				setWall(x + i - 1, y + n);
				setWall(x - 1, y + i - 1);
				setWall(x + n, y + i - 1);
			}
			setupMode(x, y, n, n, 1, 1);
			dampingBar.setValue(1);
		}

		Setup createNext() {
			return new OneByOneModesSetup();
		}
	}

	class OneByOneModesSetup extends Setup {
		String getName() {
			return "1x1 Modes";
		}

		void select() {
			sourceChooser.select(SRC_NONE);
			int i, j;
			int y = 1;
			int ny = 25;
			while (y + ny < windowHeight) {
				int nx = ((y + ny) * (windowWidth - 8) / windowHeight) + 6;
				int y1 = y + windowOffsetY;
				int x1 = windowOffsetX + 1;
				for (i = 0; i != nx + 2; i++) {
					setWall(x1 + i - 1, y1 - 1);
					setWall(x1 + i - 1, y1 + ny);
				}
				for (j = 0; j != ny + 2; j++) {
					setWall(x1 - 1, y1 + j - 1);
					setWall(x1 + nx, y1 + j - 1);
				}
				setupMode(x1, y1, nx, ny, 1, 1);
				y += ny + 1;
			}
			dampingBar.setValue(1);
		}

		Setup createNext() {
			return new OneByNModesSetup();
		}
	}

	class OneByNModesSetup extends Setup {
		String getName() {
			return "1xN Modes";
		}

		void select() {
			sourceChooser.select(SRC_NONE);
			int i, j;
			int y = 1;
			int ny = 25;
			int nx = windowWidth - 2;
			int mode = 1;
			while (y + ny < windowHeight) {
				int y1 = y + windowOffsetY;
				int x1 = windowOffsetX + 1;
				for (i = 0; i != nx + 2; i++) {
					setWall(x1 + i - 1, y1 - 1);
					setWall(x1 + i - 1, y1 + ny);
				}
				for (j = 0; j != ny + 2; j++) {
					setWall(x1 - 1, y1 + j - 1);
					setWall(x1 + nx, y1 + j - 1);
				}
				setupMode(x1, y1, nx, ny, mode, 1);
				y += ny + 1;
				mode++;
			}
			dampingBar.setValue(1);
		}

		Setup createNext() {
			return new NByNModesSetup();
		}
	}

	class NByNModesSetup extends Setup {
		String getName() {
			return "NxN Modes";
		}

		void select() {
			sourceChooser.select(SRC_NONE);
			int i, j;
			int y = 1;
			int modex, modey;
			int maxmode = 3;
			if (resBar.getValue() >= 70)
				maxmode++;
			if (resBar.getValue() >= 100)
				maxmode++;
			int ny = windowHeight / maxmode - 2;
			int nx = windowWidth / maxmode - 2;
			for (modex = 1; modex <= maxmode; modex++)
				for (modey = 1; modey <= maxmode; modey++) {
					int x1 = windowOffsetX + 1 + (ny + 1) * (modey - 1);
					int y1 = windowOffsetY + 1 + (nx + 1) * (modex - 1);
					for (i = 0; i != nx + 2; i++) {
						setWall(x1 + i - 1, y1 - 1);
						setWall(x1 + i - 1, y1 + ny);
					}
					for (j = 0; j != ny + 2; j++) {
						setWall(x1 - 1, y1 + j - 1);
						setWall(x1 + nx, y1 + j - 1);
					}
					setupMode(x1, y1, nx, ny, modex, modey);
				}
			dampingBar.setValue(1);
		}

		Setup createNext() {
			return new OneByNModeCombosSetup();
		}
	}

	class OneByNModeCombosSetup extends Setup {
		String getName() {
			return "1xN Mode Combos";
		}

		void select() {
			sourceChooser.select(SRC_NONE);
			int i, j;
			int y = 1;
			int ny = 5;
			int nx = windowWidth - 2;
			while (y + ny < windowHeight) {
				int mode1 = getrand(12) + 1;
				int mode2;
				do
					mode2 = getrand(12) + 1;
				while (mode1 == mode2);
				int y1 = y + windowOffsetY;
				int x1 = windowOffsetX + 1;
				for (i = 0; i != nx + 2; i++) {
					setWall(x1 + i - 1, y1 - 1);
					setWall(x1 + i - 1, y1 + ny);
				}
				for (j = 0; j != ny + 2; j++) {
					setWall(x1 - 1, y1 + j - 1);
					setWall(x1 + nx, y1 + j - 1);
				}
				for (i = 0; i != nx; i++)
					for (j = 0; j != ny; j++) {
						int gi = i + x1 + gw * (j + y1);
						func[gi] = (float) (Math.sin(mode1 * pi * (i + 1)
								/ (nx + 1))
								* Math.sin(pi * (j + 1) / (ny + 1)) * .5 + Math
								.sin(mode2 * pi * (i + 1) / (nx + 1))
								* Math.sin(pi * (j + 1) / (ny + 1)) * .5);
						funci[gi] = 0;
					}
				y += ny + 1;
			}
			dampingBar.setValue(1);
		}

		Setup createNext() {
			return new NByNModeCombosSetup();
		}
	}

	class NByNModeCombosSetup extends Setup {
		String getName() {
			return "NxN Mode Combos";
		}

		void select() {
			sourceChooser.select(SRC_NONE);
			int i, j;
			int y = 1;
			int maxmode = 3;
			if (resBar.getValue() >= 70)
				maxmode++;
			if (resBar.getValue() >= 100)
				maxmode++;
			int ny = windowHeight / maxmode - 2;
			int nx = windowWidth / maxmode - 2;
			int gx, gy;
			for (gx = 1; gx <= maxmode; gx++)
				for (gy = 1; gy <= maxmode; gy++) {
					int mode1x = getrand(4) + 1;
					int mode1y = getrand(4) + 1;
					int mode2x, mode2y;
					do {
						mode2x = getrand(4) + 1;
						mode2y = getrand(4) + 1;
					} while (mode1x == mode2x && mode1y == mode2y);
					int x1 = windowOffsetX + 1 + (ny + 1) * (gx - 1);
					int y1 = windowOffsetY + 1 + (nx + 1) * (gy - 1);
					for (i = 0; i != nx + 2; i++) {
						setWall(x1 + i - 1, y1 - 1);
						setWall(x1 + i - 1, y1 + ny);
					}
					for (j = 0; j != ny + 2; j++) {
						setWall(x1 - 1, y1 + j - 1);
						setWall(x1 + nx, y1 + j - 1);
					}
					for (i = 0; i != nx; i++)
						for (j = 0; j != ny; j++) {
							int gi = i + x1 + gw * (j + y1);
							func[gi] = (float) (Math.sin(mode1x * pi * (i + 1)
									/ (nx + 1))
									* Math.sin(mode1y * pi * (j + 1) / (ny + 1))
									* .5 + Math.sin(mode2x * pi * (i + 1)
									/ (nx + 1))
									* Math.sin(mode2y * pi * (j + 1) / (ny + 1))
									* .5);
							funci[gi] = 0;
						}
				}
			dampingBar.setValue(1);
		}

		Setup createNext() {
			return new ZeroByOneModesSetup();
		}
	}

	class ZeroByOneModesSetup extends Setup {
		String getName() {
			return "0x1 Acoustic Modes";
		}

		void select() {
			fixedEndsCheck.setState(false);
			sourceChooser.select(SRC_NONE);
			int i, j;
			int y = 1;
			int ny = 25;
			while (y + ny < windowHeight) {
				int nx = ((y + ny) * (windowWidth - 8) / windowHeight) + 6;
				int y1 = y + windowOffsetY;
				int x1 = windowOffsetX + 1;
				for (i = 0; i != nx + 2; i++) {
					setWall(x1 + i - 1, y1 - 1);
					setWall(x1 + i - 1, y1 + ny);
				}
				for (j = 0; j != ny + 2; j++) {
					setWall(x1 - 1, y1 + j - 1);
					setWall(x1 + nx, y1 + j - 1);
				}
				setupAcousticMode(x1, y1, nx, ny, 1, 0);
				y += ny + 1;
			}
			dampingBar.setValue(1);
		}

		Setup createNext() {
			return new ZeroByNModesSetup();
		}
	}

	class ZeroByNModesSetup extends Setup {
		String getName() {
			return "0xN Acoustic Modes";
		}

		void select() {
			fixedEndsCheck.setState(false);
			sourceChooser.select(SRC_NONE);
			int i, j;
			int y = 1;
			int ny = 25;
			int nx = windowWidth - 2;
			int mode = 1;
			while (y + ny < windowHeight) {
				int y1 = y + windowOffsetY;
				int x1 = windowOffsetX + 1;
				for (i = 0; i != nx + 2; i++) {
					setWall(x1 + i - 1, y1 - 1);
					setWall(x1 + i - 1, y1 + ny);
				}
				for (j = 0; j != ny + 2; j++) {
					setWall(x1 - 1, y1 + j - 1);
					setWall(x1 + nx, y1 + j - 1);
				}
				setupAcousticMode(x1, y1, nx, ny, mode, 0);
				y += ny + 1;
				mode++;
			}
			dampingBar.setValue(1);
		}

		Setup createNext() {
			return new NByNAcoModesSetup();
		}
	}

	class NByNAcoModesSetup extends Setup {
		String getName() {
			return "NxN Acoustic Modes";
		}

		void select() {
			fixedEndsCheck.setState(false);
			sourceChooser.select(SRC_NONE);
			int i, j;
			int y = 1;
			int modex, modey;
			int maxmode = 2;
			if (resBar.getValue() >= 70)
				maxmode++;
			// weird things start to happen if maxmode goes higher than 4
			int ny = windowHeight / (maxmode + 1) - 2;
			int nx = windowWidth / (maxmode + 1) - 2;
			for (modex = 0; modex <= maxmode; modex++)
				for (modey = 0; modey <= maxmode; modey++) {
					int x1 = windowOffsetX + 1 + (ny + 1) * (modey);
					int y1 = windowOffsetY + 1 + (nx + 1) * (modex);
					for (i = 0; i != nx + 2; i++) {
						setWall(x1 + i - 1, y1 - 1);
						setWall(x1 + i - 1, y1 + ny);
					}
					for (j = 0; j != ny + 2; j++) {
						setWall(x1 - 1, y1 + j - 1);
						setWall(x1 + nx, y1 + j - 1);
					}
					setupAcousticMode(x1, y1, nx, ny, modex, modey);
				}
			dampingBar.setValue(1);
		}

		Setup createNext() {
			return new CoupledCavitiesSetup();
		}
	}

	class CoupledCavitiesSetup extends Setup {
		String getName() {
			return "Coupled Cavities";
		}

		void select() {
			fixedEndsCheck.setState(false);
			sourceChooser.select(SRC_NONE);
			int i, j;
			int y = 1;
			int ny = 5;
			while (y + ny < windowHeight) {
				int nx = 35;
				int y1 = y + windowOffsetY;
				int x1 = windowOffsetX + 1;
				for (i = 0; i != nx + 2; i++) {
					setWall(x1 + i - 1, y1 - 1);
					setWall(x1 + i - 1, y1 + ny);
				}
				for (j = 0; j != ny + 2; j++) {
					setWall(x1 - 1, y1 + j - 1);
					setWall(x1 + nx, y1 + j - 1);
				}
				for (j = 0; j != 2; j++) {
					setWall(x1 + nx / 2, y1 + j);
					setWall(x1 + nx / 2, y1 + 4 - j);
				}
				setupAcousticMode(x1, y1, nx / 2, ny, 1, 0);
				y += ny + 3;
			}
			dampingBar.setValue(1);
		}

		Setup createNext() {
			return new BeatsSetup();
		}
	}

	class BeatsSetup extends Setup {
		String getName() {
			return "Beats";
		}

		void doSetupSources() {
			sourceChooser.select(SRC_2S2F);
			setSources();
			auxBar.setValue(24);
			sources[0].y = sources[1].y = gridSizeY / 2;
			sources[0].x = gridSizeX / 2 - 2;
			sources[1].x = gridSizeX / 2 + 2;
		}

		void select() {
			setBrightness(25);
			setFreqBar(18);
		}

		Setup createNext() {
			return new SlowMediumSetup();
		}
	}

	class SlowMediumSetup extends Setup {
		String getName() {
			return "Slow Medium";
		}

		void select() {
			addMedium();
			setFreqBar(10);
			setBrightness(33);
		}

		Setup createNext() {
			return new RefractionSetup();
		}
	}

	class RefractionSetup extends Setup {
		String getName() {
			return "Refraction";
		}

		void doSetupSources() {
			sourceChooser.select(SRC_1S1F_PLANE_PULSE);
			setSources();
			sources[0].x = windowOffsetX;
			sources[0].y = windowOffsetY + windowHeight / 4;
			sources[1].x = windowOffsetX + windowWidth / 3;
			sources[1].y = windowOffsetY;
			addMedium();
			setFreqBar(1);
			setBrightness(33);
		}

		void select() {
		}

		Setup createNext() {
			return new InternalReflectionSetup();
		}
	}

	class InternalReflectionSetup extends Setup {
		String getName() {
			return "Internal Reflection";
		}

		void doSetupSources() {
			sourceChooser.select(SRC_1S1F_PLANE_PULSE);
			setSources();
			sources[0].x = windowOffsetX;
			sources[0].y = windowOffsetY + windowHeight * 2 / 3;
			sources[1].x = windowOffsetX + windowWidth / 3;
			sources[1].y = windowOffsetY + windowHeight - 1;
			addMedium();
			setFreqBar(1);
			setBrightness(33);
		}

		void select() {
		}

		Setup createNext() {
			return new CoatingSetup();
		}
	}

	class CoatingSetup extends Setup {
		String getName() {
			return "Anti-Reflective Coating";
		}

		void select() {
			sourceChooser.select(SRC_1S1F);
			addMedium();
			int i, j;
			// v2/c2 = 1-(mediumMaxIndex/mediumMax)*medium);
			// n = sqrt(v2/c2)
			double nmax = Math.sqrt(1 - mediumMaxIndex);
			double nroot = Math.sqrt(nmax);
			int mm = (int) ((1 - nmax) * mediumMax / mediumMaxIndex);
			for (i = 0; i != gridSizeX; i++)
				for (j = gridSizeY / 2 - 4; j != gridSizeY / 2; j++)
					medium[i + j * gw] = mm;
			setFreqBar(6);
			setBrightness(28);
		}

		Setup createNext() {
			return new ZonePlateEvenSetup();
		}
	}

	class ZonePlateEvenSetup extends Setup {
		int zoneq;

		ZonePlateEvenSetup() {
			zoneq = 1;
		}

		String getName() {
			return "Zone Plate (Even)";
		}

		void doSetupSources() {
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			setSources();
			if (resBar.getValue() < 42)
				setResolution(42);
			int i;
			// wavelength by default = 25, we want it to be 6
			int freq = 30;
			setFreqBar(freq);
			double halfwave = 25. / (freq * 2 / 5);
			int y = sources[0].y + 1;
			int dy = windowOffsetY + windowHeight / 2 - y;
			int dy2 = dy * dy;
			int cx = gridSizeX / 2;
			for (i = 0; i != windowWidth; i++) {
				int x = windowOffsetX + i;
				int dx = cx - x;
				double dist = Math.sqrt(dx * dx + dy * dy);
				dist = (dist - dy);
				int zone = (int) (dist / halfwave);
				setWall(x, y, ((zone & 1) == zoneq));
				setWall(windowOffsetX, y);
				setWall(windowOffsetX + windowWidth - 1, y);
			}
			setBrightness(zoneq == 1 ? 4 : 7);
		}

		Setup createNext() {
			return new ZonePlateOddSetup();
		}
	}

	class ZonePlateOddSetup extends ZonePlateEvenSetup {
		ZonePlateOddSetup() {
			zoneq = 0;
		}

		String getName() {
			return "Zone Plate (Odd)";
		}

		Setup createNext() {
			return new CircleSetup();
		}
	}

	class CircleSetup extends Setup {
		CircleSetup() {
			circle = true;
		}

		boolean circle;

		String getName() {
			return "Circle";
		}

		void doSetupSources() {
		}

		void select() {
			int i;
			int dx = windowWidth / 2 - 2;
			double a2 = dx * dx;
			double b2 = a2 / 2;
			if (circle)
				b2 = a2;
			int cx = windowWidth / 2 + windowOffsetX;
			int cy = windowHeight / 2 + windowOffsetY;
			int ly = -1;
			for (i = 0; i <= dx; i++) {
				double y = Math.sqrt((1 - i * i / a2) * b2);
				int yi = (int) (y + 1.5);
				if (i == dx)
					yi = 0;
				if (ly == -1)
					ly = yi;
				for (; ly >= yi; ly--) {
					setWall(cx + i, cy + ly);
					setWall(cx - i, cy + ly);
					setWall(cx + i, cy - ly);
					setWall(cx - i, cy - ly);
					// setWall(cx-ly, cx+i);
					// setWall(cx+ly, cx+i);
				}
				ly = yi;
			}
			int c = (int) (Math.sqrt(a2 - b2));
			// walls[cx+c][cy] = walls[cx-c][cy] = true;
			// walls[cx][cy+c] = true;
			sourceChooser.select(SRC_1S1F_PULSE);
			setSources();
			sources[0].x = cx - c;
			sources[0].y = cy;
			setFreqBar(1);
			setBrightness(16);
		}

		Setup createNext() {
			return new EllipseSetup();
		}
	}

	class EllipseSetup extends CircleSetup {
		EllipseSetup() {
			circle = false;
		}

		String getName() {
			return "Ellipse";
		}

		Setup createNext() {
			return new ResonantCavitiesSetup();
		}
	}

	class ResonantCavitiesSetup extends Setup {
		String getName() {
			return "Resonant Cavities 1";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			int i, j;
			int x = 1;
			int nx = 5;
			int y1 = windowOffsetY + 11;
			while (x + nx < windowWidth) {
				int ny = ((x + nx) * (windowHeight - 18) / windowWidth) + 6;
				int x1 = x + windowOffsetX;
				for (i = 0; i != ny + 2; i++) {
					setWall(x1 - 1, y1 + i - 1);
					setWall(x1 + nx, y1 + i - 1);
				}
				for (j = 0; j != nx + 2; j++) {
					setWall(x1 + j - 1, y1 - 1);
					setWall(x1 + j - 1, y1 + ny);
				}
				setWall(x1 + nx / 2, y1 - 1, false);
				x += nx + 1;
			}
			for (; x < windowWidth; x++)
				setWall(x + windowOffsetX, y1 - 1);
			setBrightness(30);
			setFreqBar(15);
		}

		double sourceStrength() {
			return .1;
		}

		Setup createNext() {
			return new ResonantCavities2Setup();
		}
	}

	class ResonantCavities2Setup extends Setup {
		String getName() {
			return "Resonant Cavities 2";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			int i, j;
			int x = 1;
			int nx = 5;
			int y1 = windowOffsetY + 11;
			int ny = 5;
			while (x + nx < windowWidth) {
				int x1 = x + windowOffsetX;
				for (i = 0; i != ny + 2; i++) {
					setWall(x1 - 1, y1 + i - 1);
					setWall(x1 + nx, y1 + i - 1);
				}
				for (j = 0; j != nx + 2; j++)
					setWall(x1 + j - 1, y1 + ny);
				x += nx + 1;
				ny++;
			}
			for (; x < windowWidth; x++)
				setWall(x + windowOffsetX, y1 - 1);
			setBrightness(30);
			setFreqBar(16);
		}

		double sourceStrength() {
			return .03;
		}

		Setup createNext() {
			return new RoomResonanceSetup();
		}
	}

	class RoomResonanceSetup extends Setup {
		String getName() {
			return "Room Resonance";
		}

		void select() {
			sourceChooser.select(SRC_4S1F);
			setSources();
			int i, j;
			int modex, modey;
			int ny = 17;
			int nx = 17;
			for (modex = 1; modex <= 2; modex++)
				for (modey = 1; modey <= 2; modey++) {
					int x1 = windowOffsetX + 1 + (ny + 1) * (modey - 1);
					int y1 = windowOffsetY + 1 + (nx + 1) * (modex - 1);
					for (i = 0; i != nx + 2; i++) {
						setWall(x1 + i - 1, y1 - 1);
						setWall(x1 + i - 1, y1 + ny);
					}
					for (j = 0; j != ny + 2; j++) {
						setWall(x1 - 1, y1 + j - 1);
						setWall(x1 + nx, y1 + j - 1);
					}
				}
			sources[0].x = sources[2].x = windowOffsetX + 2;
			sources[0].y = sources[1].y = windowOffsetY + 2;
			sources[1].x = sources[3].x = windowOffsetX + 1 + nx + (nx + 1) / 2;
			sources[2].y = sources[3].y = windowOffsetY + 1 + ny + (ny + 1) / 2;
			fixedEndsCheck.setState(false);
			dampingBar.setValue(10);
			setBrightness(3);
		}

		void doSetupSources() {
		}

		Setup createNext() {
			return new Waveguides1Setup();
		}
	}

	class Waveguides1Setup extends Setup {
		String getName() {
			return "Waveguides 1";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			int i, j;
			int x = 1;
			int nx = 3;
			int y1 = windowOffsetY + 3;
			int ny = windowHeight - 2;
			while (x + nx < windowWidth) {
				int x1 = x + windowOffsetX;
				for (i = 0; i != ny; i++) {
					setWall(x1 - 1, y1 + i - 1);
					setWall(x1 + nx, y1 + i - 1);
				}
				nx++;
				x += nx;
			}
			for (; x < windowWidth; x++)
				setWall(x + windowOffsetX, y1 - 1);
			setBrightness(6);
			setFreqBar(14);
		}

		Setup createNext() {
			return new Waveguides2Setup();
		}
	}

	class Waveguides2Setup extends Waveguides1Setup {
		String getName() {
			return "Waveguides 2";
		}

		void select() {
			super.select();
			setFreqBar(8);
		}

		Setup createNext() {
			return new Waveguides3Setup();
		}
	}

	class Waveguides3Setup extends Setup {
		String getName() {
			return "Waveguides 3";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			int i, j;
			int x = 1;
			int nx = 8;
			int y1 = windowOffsetY + 3;
			int ny = windowHeight - 2;
			for (x = 1; x < windowWidth; x++)
				setWall(x + windowOffsetX, y1 - 1);
			x = 1;
			j = 0;
			while (x + nx < windowWidth && j < nx) {
				int x1 = x + windowOffsetX;
				for (i = 0; i != ny; i++) {
					setWall(x1 - 1, y1 + i - 1);
					setWall(x1 + nx, y1 + i - 1);
				}
				setWall(x1 + j++, y1 - 1, false);
				x += nx + 1;
			}
			setBrightness(89);
			setFreqBar(16);
		}

		Setup createNext() {
			return new Waveguides4Setup();
		}
	}

	class Waveguides4Setup extends Waveguides3Setup {
		String getName() {
			return "Waveguides 4";
		}

		void select() {
			super.select();
			setBrightness(29);
			setFreqBar(20);
			fixedEndsCheck.setState(false);
		}

		Setup createNext() {
			return new Waveguides5Setup();
		}
	}

	class Waveguides5Setup extends Waveguides3Setup {
		String getName() {
			return "Waveguides 5";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			int i;
			int x = 1;
			int nx = 8;
			int y1 = windowOffsetY + 2;
			int ny = windowHeight - 1;
			x = 1;
			while (x + nx < windowWidth) {
				int x1 = x + windowOffsetX;
				for (i = 0; i != ny; i++) {
					setWall(x1 - 1, y1 + i - 1);
					setWall(x1 + nx, y1 + i - 1);
				}
				x += nx + 1;
			}
			setBrightness(9);
			setFreqBar(22);
		}

		void eachFrame() {
			int y = windowOffsetY + 1;
			int nx = 8;
			int x = 1;
			int g = 1;
			while (x + nx < windowWidth) {
				int x1 = x + windowOffsetX;
				int j;
				int n1 = 1;
				int n2 = 1;
				switch (g) {
				case 1:
					n1 = n2 = 1;
					break;
				case 2:
					n1 = n2 = 2;
					break;
				case 3:
					n1 = 1;
					n2 = 2;
					break;
				case 4:
					n1 = n2 = 3;
					break;
				case 5:
					n1 = 1;
					n2 = 3;
					break;
				case 6:
					n1 = 2;
					n2 = 3;
					break;
				default:
					n1 = n2 = 0;
					break;
				}
				for (j = 0; j != nx; j++)
					func[x1 + j + gw * y] *= .5 * (Math.sin(pi * n1 * (j + 1)
							/ (nx + 1)) + Math
							.sin(pi * n2 * (j + 1) / (nx + 1)));
				x += nx + 1;
				g++;
			}
		}

		Setup createNext() {
			return new ParabolicMirror1Setup();
		}
	}

	/*
	 * class HornSetup extends Setup { String getName() { return "Horn"; } void
	 * select() { if (resBar.getValue() < 76) setResolution(76);
	 * fixedEndsCheck.setState(false); setFreqBar(3); int i; int cx =
	 * windowOffsetX+windowWidth/2; int yy = windowHeight/2; int oj = 0; double
	 * lmult = Math.log(windowWidth/2-2)*1.2; System.out.println(yy + " " +
	 * lmult); for (i = 0; i < yy; i++) { int j = (int) (Math.exp(i*lmult/yy));
	 * System.out.println(i + " " +j); //int j = i*((windowWidth-5)/2)/yy; while
	 * (oj <= j) { walls[cx+oj][windowOffsetY+i] = walls[cx-oj][windowOffsetY+i]
	 * = true; oj++; } oj = j; } setBrightness(12); } Setup createNext() {
	 * return new ParabolicMirror1Setup(); } }
	 */
	class ParabolicMirror1Setup extends Setup {
		String getName() {
			return "Parabolic Mirror 1";
		}

		void select() {
			if (resBar.getValue() < 50)
				setResolution(50);
			int i;
			int cx = windowWidth / 2 + windowOffsetX;
			int lx = 0;
			int dy = windowHeight / 2;
			int cy = windowHeight + windowOffsetY - 2;
			int dx = windowWidth / 2 - 2;
			double c = dx * dx * .5 / dy;
			if (c > 20)
				c = 20;
			for (i = 0; i <= dy; i++) {
				double x = Math.sqrt(2 * c * i);
				int xi = (int) (x + 1.5);
				for (; lx <= xi; lx++) {
					setWall(cx - lx, cy - i);
					setWall(cx + lx, cy - i);
				}
				lx = xi;
			}
			setSources();
			sources[0].x = cx;
			sources[0].y = (int) (cy - 1 - c / 2);
			setBrightness(18);
		}

		void doSetupSources() {
		}

		Setup createNext() {
			return new ParabolicMirror2Setup();
		}
	}

	class ParabolicMirror2Setup extends ParabolicMirror1Setup {
		String getName() {
			return "Parabolic Mirror 2";
		}

		void doSetupSources() {
			sourceChooser.select(SRC_1S1F_PLANE);
			brightnessBar.setValue(370);
			setFreqBar(15);
			setSources();
		}

		Setup createNext() {
			return new SoundDuctSetup();
		}
	}

	class SoundDuctSetup extends Setup {
		String getName() {
			return "Sound Duct";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PULSE);
			fixedEndsCheck.setState(false);
			int i;
			int cx = windowOffsetX + windowWidth / 2;
			for (i = 0; i != windowHeight - 12; i++) {
				setWall(cx - 3, i + windowOffsetY + 6);
				setWall(cx + 3, i + windowOffsetY + 6);
			}
			setFreqBar(1);
			setBrightness(60);
		}

		Setup createNext() {
			return new BaffledPistonSetup();
		}
	}

	class BaffledPistonSetup extends Setup {
		String getName() {
			return "Baffled Piston";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			fixedEndsCheck.setState(false);
			int i;
			for (i = 0; i != gridSizeY; i++)
				setWall(windowOffsetX + 2, i);
			for (i = 0; i <= 11; i++) {
				setWall(windowOffsetX, i + gridSizeY / 2 - 5);
				if (i != 0 && i != 11)
					setWall(windowOffsetX + 2, i + gridSizeY / 2 - 5, false);
			}
			setWall(windowOffsetX + 1, gridSizeY / 2 - 5);
			setWall(windowOffsetX + 1, gridSizeY / 2 + 6);
			setFreqBar(24);
			setSources();
			sources[0].x = sources[1].x = windowOffsetX + 1;
			sources[0].y = gridSizeY / 2 - 4;
			sources[1].y = gridSizeY / 2 + 5;
			setBrightness(18);
		}

		void doSetupSources() {
		}

		Setup createNext() {
			return new LowPassFilter1Setup();
		}
	}

	class LowPassFilter1Setup extends Setup {
		String getName() {
			return "Low-Pass Filter 1";
		}

		void select() {
			if (resBar.getValue() < 43)
				setResolution(43);
			fixedEndsCheck.setState(false);
			int i, j;
			for (i = 0; i != windowWidth; i++)
				setWall(i + windowOffsetX, windowOffsetY + 9);
			int cx = gridSizeX / 2;
			for (i = 1; i <= 4; i++)
				for (j = -7; j <= 7; j++)
					setWall(cx + j, windowOffsetY + 9 * i);
			for (i = 0; i <= 4; i++)
				for (j = -4; j <= 4; j++)
					setWall(cx + j, windowOffsetY + 9 * i, false);
			for (i = 0; i != 27; i++) {
				setWall(cx + 7, windowOffsetY + 9 + i);
				setWall(cx - 7, windowOffsetY + 9 + i);
			}
			setBrightness(38);
		}

		Setup createNext() {
			return new LowPassFilter2Setup();
		}
	}

	class LowPassFilter2Setup extends LowPassFilter1Setup {
		String getName() {
			return "Low-Pass Filter 2";
		}

		void select() {
			super.select();
			setFreqBar(17);
		}

		Setup createNext() {
			return new HighPassFilter1Setup();
		}
	}

	class HighPassFilter1Setup extends Setup {
		String getName() {
			return "High-Pass Filter 1";
		}

		void select() {
			if (resBar.getValue() < 43)
				setResolution(43);
			fixedEndsCheck.setState(false);
			int i, j;
			for (i = 0; i != windowWidth; i++)
				for (j = 0; j <= 25; j += 5)
					setWall(i + windowOffsetX, windowOffsetY + 9 + j);
			int cx = gridSizeX / 2;
			for (i = 0; i <= 25; i += 5)
				for (j = -4; j <= 4; j++)
					setWall(cx + j, windowOffsetY + 9 + i, false);
			setBrightness(62);
			// by default we show a freq high enough to be passed
			setFreqBar(17);
		}

		Setup createNext() {
			return new HighPassFilter2Setup();
		}
	}

	class HighPassFilter2Setup extends HighPassFilter1Setup {
		String getName() {
			return "High-Pass Filter 2";
		}

		void select() {
			super.select();
			setFreqBar(7);
		}

		Setup createNext() {
			return new BandStopFilter1Setup();
		}
	}

	class BandStopFilter1Setup extends Setup {
		String getName() {
			return "Band-Stop Filter 1";
		}

		void select() {
			if (resBar.getValue() < 43)
				setResolution(43);
			fixedEndsCheck.setState(false);
			int i, j, k;
			for (i = 0; i != windowWidth; i++)
				setWall(i + windowOffsetX, windowOffsetY + 9);
			int cx = gridSizeX / 2;
			for (i = 1; i <= 2; i++)
				for (j = -11; j <= 11; j++) {
					if (j > -5 && j < 5)
						continue;
					setWall(cx + j, windowOffsetY + 9 + 9 * i);
				}
			for (i = 0; i <= 1; i++)
				for (j = -4; j <= 4; j++)
					setWall(cx + j, windowOffsetY + 9 + i * 26, false);
			for (i = 0; i <= 18; i++) {
				setWall(cx + 11, windowOffsetY + 9 + i);
				setWall(cx - 11, windowOffsetY + 9 + i);
			}
			for (i = 0; i != 3; i++)
				for (j = 0; j != 3; j++)
					for (k = 9; k <= 18; k += 9) {
						setWall(cx + 5 + i, windowOffsetY + k + j);
						setWall(cx + 5 + i, windowOffsetY + 9 + k - j);
						setWall(cx - 5 - i, windowOffsetY + k + j);
						setWall(cx - 5 - i, windowOffsetY + 9 + k - j);
					}
			setBrightness(38);
			setFreqBar(2);
		}

		Setup createNext() {
			return new BandStopFilter2Setup();
		}
	}

	class BandStopFilter2Setup extends BandStopFilter1Setup {
		String getName() {
			return "Band-Stop Filter 2";
		}

		void select() {
			super.select();
			setFreqBar(10);
		}

		Setup createNext() {
			return new BandStopFilter3Setup();
		}
	}

	class BandStopFilter3Setup extends BandStopFilter1Setup {
		String getName() {
			return "Band-Stop Filter 3";
		}

		void select() {
			super.select();
			// at this frequency it doesn't pass
			setFreqBar(4);
		}

		Setup createNext() {
			return new PlanarConvexLensSetup();
		}
	}

	class PlanarConvexLensSetup extends Setup {
		String getName() {
			return "Planar Convex Lens";
		}

		void select() {
			if (resBar.getValue() < 42)
				setResolution(42);
			sourceChooser.select(SRC_1S1F_PLANE);
			// need small wavelengths here to remove diffraction effects
			int i, j;
			int cx = gridSizeX / 2;
			int cy = windowHeight / 8 + windowOffsetY;
			int x0 = windowWidth / 3 - 2;
			int y0 = 5;
			double r = (.75 * windowHeight) * .5;
			double h = r - y0;
			double r2 = r * r;
			if (x0 > r)
				x0 = (int) r;
			for (i = 0; i <= x0; i++) {
				int y = 2 + (int) (Math.sqrt(r2 - i * i) - h + .5);
				for (; y >= 0; y--) {
					setMedium(cx + i, cy + y, mediumMax / 2);
					setMedium(cx - i, cy + y, mediumMax / 2);
				}
			}
			setFreqBar(19);
			setBrightness(6);
		}

		Setup createNext() {
			return new BiconvexLensSetup();
		}
	}

	class BiconvexLensSetup extends Setup {
		String getName() {
			return "Biconvex Lens";
		}

		void select() {
			if (resBar.getValue() < 50)
				setResolution(50);
			setSources();
			int i, j;
			int cx = gridSizeX / 2;
			int cy = gridSizeY / 2;
			int x0 = windowWidth / 3 - 2;
			int y0 = 10;
			double r = (.75 * .5 * windowHeight) * .5;
			double h = r - y0;
			double r2 = r * r;
			if (x0 > r)
				x0 = (int) r;
			for (i = 0; i <= x0; i++) {
				int y = 1 + (int) (Math.sqrt(r2 - i * i) - h + .5);
				for (; y >= 0; y--) {
					setMedium(cx + i, cy + y, mediumMax / 2);
					setMedium(cx - i, cy + y, mediumMax / 2);
					setMedium(cx + i, cy - y, mediumMax / 2);
					setMedium(cx - i, cy - y, mediumMax / 2);
				}
			}
			setFreqBar(19);
			setBrightness(66);
			sources[0].y = cy - (2 + 2 * (int) r);
		}

		void doSetupSources() {
		}

		Setup createNext() {
			return new PlanarConcaveSetup();
		}
	}

	class PlanarConcaveSetup extends Setup {
		String getName() {
			return "Planar Concave Lens";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			int i, j;
			int cx = gridSizeX / 2;
			int cy = windowHeight / 8 + windowOffsetY;
			int x0 = windowWidth / 5;
			int y0 = 5;
			double r = (.25 * windowHeight) * .5;
			double h = r - y0;
			double r2 = r * r;
			if (x0 > r)
				x0 = (int) r;
			for (i = 0; i <= x0; i++) {
				int y = y0 + 2 - (int) (Math.sqrt(r2 - i * i) - h + .5);
				for (; y >= 0; y--) {
					setMedium(cx + i, cy + y, mediumMax / 2);
					setMedium(cx - i, cy + y, mediumMax / 2);
				}
			}
			for (i = 0; i != windowWidth; i++)
				if (medium[windowOffsetX + i + gw * cy] == 0)
					setWall(windowOffsetX + i, cy);
			setFreqBar(19);
		}

		Setup createNext() {
			return new CircularPrismSetup();
		}
	}

	class CircularPrismSetup extends Setup {
		String getName() {
			return "Circular Prism";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE);
			int i, j;
			int cx = gridSizeX / 2;
			int cy = gridSizeY / 2;
			int x0 = windowWidth / 3 - 2;
			int y0 = x0;
			double r = (x0 * x0 + y0 * y0) / (2. * y0);
			double h = r - y0;
			double r2 = r * r;
			for (i = 0; i < x0; i++) {
				int y = (int) (Math.sqrt(r2 - i * i) - h + .5);
				for (; y >= 0; y--) {
					setMedium(cx + i, cy + y, mediumMax);
					setMedium(cx - i, cy + y, mediumMax);
					setMedium(cx + i, cy - y, mediumMax);
					setMedium(cx - i, cy - y, mediumMax);
				}
			}
			for (i = 0; i != windowWidth; i++)
				if (medium[windowOffsetX + i + gw * cy] == 0)
					setWall(windowOffsetX + i, cy);
			setFreqBar(9);
		}

		Setup createNext() {
			return new RightAnglePrismSetup();
		}
	}

	class RightAnglePrismSetup extends Setup {
		String getName() {
			return "Right-Angle Prism";
		}

		void select() {
			if (resBar.getValue() < 42)
				setResolution(42);
			sourceChooser.select(SRC_1S1F_PLANE);
			int i, j;
			int cx = gridSizeX / 2;
			int cy = gridSizeY / 2;
			int x0 = windowWidth / 4;
			int y0 = x0;
			for (i = -x0; i < x0; i++)
				for (j = -y0; j <= i; j++)
					setMedium(cx + i, cy + j, mediumMax);
			for (i = 0; i != windowWidth; i++)
				if (medium[windowOffsetX + i + gw * (cy - y0)] == 0)
					setWall(windowOffsetX + i, cy - y0);
			setFreqBar(11);
		}

		Setup createNext() {
			return new PorroPrismSetup();
		}
	}

	class PorroPrismSetup extends Setup {
		String getName() {
			return "Porro Prism";
		}

		void select() {
			if (resBar.getValue() < 42)
				setResolution(42);
			sourceChooser.select(SRC_1S1F_PLANE);
			setSources();
			int i, j;
			int cx = gridSizeX / 2;
			sources[1].x = cx - 1;
			int x0 = windowWidth / 2;
			int y0 = x0;
			int cy = gridSizeY / 2 - y0 / 2;
			for (i = -x0; i < x0; i++) {
				int j2 = y0 + 1 - ((i < 0) ? -i : i);
				for (j = 0; j <= j2; j++)
					setMedium(cx + i, cy + j, mediumMax);
			}
			for (i = 0; i != cy; i++)
				if (medium[cx + gw * (i + windowOffsetY)] == 0)
					setWall(cx, i + windowOffsetY);
			setFreqBar(11);
		}

		void doSetupSources() {
		}

		Setup createNext() {
			return new ScatteringSetup();
		}
	}

	class ScatteringSetup extends Setup {
		String getName() {
			return "Scattering";
		}

		void select() {
			sourceChooser.select(SRC_1S1F_PLANE_PULSE);
			int cx = gridSizeX / 2;
			int cy = gridSizeY / 2;
			setWall(cx, cy);
			setFreqBar(1);
			dampingBar.setValue(40);
			setBrightness(52);
		}

		Setup createNext() {
			return new LloydsMirrorSetup();
		}
	}

	class LloydsMirrorSetup extends Setup {
		String getName() {
			return "Lloyd's Mirror";
		}

		void select() {
			setSources();
			sources[0].x = windowOffsetX;
			sources[0].y = windowOffsetY + windowHeight * 3 / 4;
			setBrightness(75);
			setFreqBar(23);
			int i;
			for (i = 0; i != windowWidth; i++)
				setWall(i + windowOffsetX, windowOffsetY + windowHeight - 1);
		}

		void doSetupSources() {
		}

		Setup createNext() {
			return new TempGradient1();
		}
	}

	class TempGradient1 extends Setup {
		String getName() {
			return "Temperature Gradient 1";
		}

		void select() {
			int i, j;
			int j1 = windowOffsetY + windowHeight / 2;
			int j2 = windowOffsetY + windowHeight * 3 / 4;
			int j3 = windowOffsetY + windowHeight * 7 / 8;
			for (j = 0; j != gridSizeY; j++) {
				int m;
				if (j < j1)
					m = 0;
				else if (j > j2)
					m = mediumMax;
				else
					m = mediumMax * (j - j1) / (j2 - j1);
				for (i = 0; i != gridSizeX; i++)
					setMedium(i, j, m);
			}
			for (i = j3; i < windowOffsetY + windowHeight; i++)
				setWall(gridSizeX / 2, i);
			setBrightness(33);
		}

		void doSetupSources() {
			setSources();
			sources[0].x = windowOffsetX + 2;
			sources[0].y = windowOffsetY + windowHeight - 2;
		}

		Setup createNext() {
			return new TempGradient2();
		}
	}

	class TempGradient2 extends Setup {
		String getName() {
			return "Temperature Gradient 2";
		}

		void select() {
			int i, j;
			int j1 = windowOffsetY + windowHeight / 2 - windowHeight / 8;
			int j2 = windowOffsetY + windowHeight / 2 + windowHeight / 8;
			for (j = 0; j != gridSizeY; j++) {
				int m;
				if (j < j1)
					m = mediumMax;
				else if (j > j2)
					m = 0;
				else
					m = mediumMax * (j2 - j) / (j2 - j1);
				for (i = 0; i != gridSizeX; i++)
					setMedium(i, j, m);
			}
			setBrightness(31);
		}

		void doSetupSources() {
			setSources();
			sources[0].x = windowOffsetX + 2;
			sources[0].y = windowOffsetY + windowHeight / 4;
		}

		Setup createNext() {
			return new TempGradient3();
		}
	}

	class TempGradient3 extends Setup {
		String getName() {
			return "Temperature Gradient 3";
		}

		void select() {
			int i, j;
			int j1 = windowOffsetY + windowHeight / 2 - windowHeight / 5;
			int j2 = windowOffsetY + windowHeight / 2 + windowHeight / 5;
			int j3 = gridSizeY / 2;
			for (j = 0; j != gridSizeY; j++) {
				int m;
				if (j < j1 || j > j2)
					m = mediumMax;
				else if (j > j3)
					m = mediumMax * (j - j3) / (j2 - j3);
				else
					m = mediumMax * (j3 - j) / (j3 - j1);
				for (i = 0; i != gridSizeX; i++)
					setMedium(i, j, m);
			}
			setBrightness(31);
		}

		void doSetupSources() {
			setSources();
			sources[0].x = windowOffsetX + 2;
			sources[0].y = windowOffsetY + windowHeight / 4;
		}

		Setup createNext() {
			return new TempGradient4();
		}
	}

	class TempGradient4 extends TempGradient3 {
		String getName() {
			return "Temperature Gradient 4";
		}

		void select() {
			int i, j;
			int j1 = windowOffsetY + windowHeight / 2 - windowHeight / 5;
			int j2 = windowOffsetY + windowHeight / 2 + windowHeight / 5;
			int j3 = gridSizeY / 2;
			for (j = 0; j != gridSizeY; j++) {
				int m;
				if (j < j1 || j > j2)
					m = 0;
				else if (j > j3)
					m = mediumMax * (j2 - j) / (j2 - j3);
				else
					m = mediumMax * (j - j1) / (j3 - j1);
				for (i = 0; i != gridSizeX; i++)
					setMedium(i, j, m);
			}
			setBrightness(31);
		}

		Setup createNext() {
			return new DispersionSetup();
		}
	}

	class DispersionSetup extends Setup {
		String getName() {
			return "Dispersion";
		}

		void select() {
			sourceChooser.select(SRC_2S2F);
			int i, j;
			for (i = 0; i != gridSizeY; i++)
				setWall(gridSizeX / 2, i);
			for (i = 0; i != gridSizeX; i++)
				for (j = 0; j != gridSizeY; j++)
					setMedium(i, j, mediumMax / 3);
			fixedEndsCheck.setState(false);
			setBrightness(16);
		}

		void doSetupSources() {
			setSources();
			sources[0].x = gridSizeX / 2 - 2;
			sources[1].x = gridSizeX / 2 + 2;
			sources[0].y = sources[1].y = windowOffsetY + 1;
			setFreqBar(7);
			auxBar.setValue(30);
		}

		Setup createNext() {
			return null;
		}
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		event.preventDefault();
//		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
//		    return;
		dragging = false;
		dragSet = dragClear = false;
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
	
	void doMouseMove(MouseEvent<?> event) {
		Point pt = getPointFromEvent(event);
		String txt = (selectedObject != null) ? selectedObject.selectText() : null;
		if (txt != null)
			coordsLabel.setText("(" + pt.x + "," + pt.y + ") " + txt);
		else
			coordsLabel.setText("(" + pt.x + "," + pt.y + ")");
		absolutePanel.setWidgetPosition(coordsLabel,
				(pt.x < windowWidth/4 && pt.y > windowHeight*3/4) ? cv.getOffsetWidth()-coordsLabel.getOffsetWidth() : 0,
				cv.getOffsetHeight()-coordsLabel.getOffsetHeight());
		coordsLabel.setVisible(true);
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
		} else if (selectedObject != null) {
			if (dragPoint.x != pt.x || dragPoint.y != pt.y) {
				selectedObject.drag(pt.x-dragPoint.x, pt.y-dragPoint.y);
				dragPoint = pt;
				changedWalls = true;
			}
		} else
			drawPoke(pt.x, pt.y);
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
				console("frequency diff " + src.frequency + " "+ src1.frequency);
				src1 = null;
				break;
			}
		}
		if (src1 == null)
			freqBar.disable();
		else
			freqBar.enable();
	}
	
	@Override
	public void onMouseDown(MouseDownEvent event) {
		event.preventDefault();
		adjustResolution = false;
		doMouseMove(event);
		if (rotationMode) {
			rotationMode = false;
			return;
		}
		dragging = true;
		
		if (view3dCheck.getState())
			return;

		double minf = 22 * windowWidth/winSize.height;
		double bestf = minf;
		Point mp = getPointFromEvent(event);
		draggingHandle = null;
		if (selectedObject != null) {
			int i;
			Point p = selectedObject.inverseTransformPoint(mp);
			console("transform " + p.x + " " + p.y + " " + mp.x + " " + mp.y);
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
		boolean clearingSelection = (selectedObject != null && sel == null);
		setSelectedObject(sel);
		
		if (selectedObject == null && !clearingSelection)
			drawPoke(mp.x, mp.y);
	}
	
	void setSelectedObject(DragObject obj) {
		if (obj == selectedObject)
			return;
		if (selectedObject != null)
			selectedObject.deselect();
		selectedObject = obj;
		if (obj != null)
			selectedObject.select();
	}
	
	@Override
	public void onMouseWheel(MouseWheelEvent event) {
        event.preventDefault();
        if (selectedObject != null && selectedObject.canRotate()) {
        	selectedObject.rotate(event.getDeltaY()* .01);
        }
        if (view3dCheck.getState()) {
        	zoom3d *= Math.exp(-event.getDeltaY() * .01);
        	set3dViewZoom(zoom3d);
        }
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		dragging = false;
		dragSet = dragClear = false;
		coordsLabel.setVisible(false);
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
		} else if (event.getSource() == blankWallsButton) {
			deleteAllObjects();
		} else if (event.getSource() == exportButton) {
			 doImport();
		}
		
		if (event.getSource() == resBar) {
		    setResolution();
//		    reinit();
		}
		if (event.getSource() == dampingBar)
		    setDamping();
		if (event.getSource() == freqBar)
		    setFreq();
	}

	@Override
	public void onChange(ChangeEvent event) {

			if (event.getSource() == stoppedCheck) {
//			    cv.repaint();
			    return;
			}
			if (event.getSource() == sourceChooser) {
			    if (sourceChooser.getSelectedIndex() != sourceIndex)
				setSources();
			}
			if (event.getSource() == setupChooser)
			    doSetup();
			if (event.getSource() == colorChooser){
			    doColor();
			}
			
		
	}

}
