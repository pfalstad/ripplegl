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
import com.google.gwt.core.shared.GWT;
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
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
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
	int sourceFreqCount = -1;
	int sourceWaveform = SWF_SIN;
	int auxFunction;
	long startTime;
	MenuBar mainMenuBar;
	MenuBar elmMenuBar;
    MenuItem elmEditMenuItem;
    MenuItem elmDeleteMenuItem;
	Color wallColor, posColor, negColor, zeroColor, medColor, posMedColor,
			negMedColor, sourceColor;
	Color schemeColors[][];
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
	Rectangle ripArea;
	Canvas cv;
	Context2d cvcontext;
	Canvas backcv;
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
		height = height - MENUBARHEIGHT;
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

	static native void updateRippleGL(double bright) /*-{
		this.updateRipple(bright);
	}-*/;

	static native void simulate() /*-{
		this.simulate();
	}-*/;

	static native void setAcoustic(boolean ac) /*-{
		this.acoustic = ac;
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

	static native void drawPoke(int x, int y) /*-{
		this.drawPoke(x, y);
	}-*/;

	static native void drawLineSource(int x1, int y1, int x2, int y2, double value) /*-{
		this.drawLineSource(x1, y1, x2, y2, value);
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
			int medColor, int sourceColor) /*-{
		this.setColors(wallColor, posColor, negColor, zeroColor, posMedColor, negMedColor,
			medColor, sourceColor);
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
		Setup s = new SingleSourceSetup();
		while (s != null) {
		    setupList.addElement(s);
		    s = s.createNext();
		}

		setupChooser = new Choice();
		int i;
		for (i = 0; i != setupList.size(); i++)
		    setupChooser.add("Setup: " +
				     ((Setup) setupList.elementAt(i)).getName());
		setupChooser.addChangeHandler(this);
//		setupChooser.addItemListener(this);
		
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
		verticalPanel.add(sourceChooser);
		verticalPanel.add(modeChooser);
		verticalPanel.add(colorChooser);
		verticalPanel.add(blankButton = new Button("Clear Waves"));
		blankButton.addClickHandler(this);
		verticalPanel.add(blankWallsButton = new Button("Clear Walls"));
		blankWallsButton.addClickHandler(this);
		verticalPanel.add(borderButton = new Button("Add Wall"));
		borderButton.addClickHandler(this);
		verticalPanel.add(boxButton = new Button("Add Box"));
		boxButton.addClickHandler(this);
		verticalPanel.add(exportButton = new Button("Import/Export"));
		exportButton.addClickHandler(this);

		verticalPanel.add(stoppedCheck = new Checkbox("Stopped"));
		verticalPanel.add(fixedEndsCheck = new Checkbox("Fixed Edges"));
		verticalPanel.add(view3dCheck = new Checkbox("3-D View"));

		int res = 110;
		verticalPanel.add(new Label("Simulation Speed"));
		verticalPanel.add(speedBar = new Scrollbar(Scrollbar.HORIZONTAL, 8, 1, 1, 30));
		verticalPanel.add(new Label("Resolution"));
		verticalPanel.add(resBar = new Scrollbar(Scrollbar.HORIZONTAL, res, 5, 5, 1024));
		resBar.addClickHandler(this);
		setResolution();
		verticalPanel.add(new Label("Damping"));
		verticalPanel.add(dampingBar = new Scrollbar(Scrollbar.HORIZONTAL, 10, 1, 2, 100));
		dampingBar.addClickHandler(this);
		verticalPanel.add(new Label("Source Frequency"));
		verticalPanel.add(freqBar = new Scrollbar(Scrollbar.HORIZONTAL, freqBarValue = 15, 1, 1, 30));
		dampingBar.addClickHandler(this);
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

		layoutPanel.addEast(verticalPanel, verticalPanelWidth);
		layoutPanel.add(cv);
		RootLayoutPanel.get().add(layoutPanel);

		mainMenuBar = new MenuBar(true);
		mainMenuBar.setAutoOpen(true);
		composeMainMenu(mainMenuBar);
		
        elmMenuBar = new MenuBar(true);
        elmMenuBar.addItem(elmEditMenuItem = new MenuItem("Edit",new MyCommand("elm","edit")));
//        elmMenuBar.addItem(elmCutMenuItem = new MenuItem("Cut",new MyCommand("elm","cut")));
//        elmMenuBar.addItem(elmCopyMenuItem = new MenuItem("Copy",new MyCommand("elm","copy")));
        elmMenuBar.addItem(elmDeleteMenuItem = new MenuItem("Delete",new MyCommand("elm","delete")));
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
		setup = (Setup) setupList.elementAt(setupChooser.getSelectedIndex());
		
		cv.addMouseMoveHandler(this);
		cv.addMouseDownHandler(this);
		cv.addMouseOutHandler(this);
		cv.addMouseUpHandler(this);
		cv.addDomHandler(this,  ContextMenuEvent.getType());
		
		reinit();
		
		handleResize();
		// String os = Navigator.getPlatform();
		// isMac = (os.toLowerCase().contains("mac"));
		// ctrlMetaKey = (isMac) ? "Cmd" : "Ctrl";
		timer.scheduleRepeating(FASTTIMER);

	}

    public void composeMainMenu(MenuBar mainMenuBar) {
    	mainMenuBar.addItem(getClassCheckItem("Add Wall", "Wall"));
    	mainMenuBar.addItem(getClassCheckItem("Add Box", "Box"));
    	mainMenuBar.addItem(getClassCheckItem("Add Source", "Source"));
    	mainMenuBar.addItem(getClassCheckItem("Add Line Source", "LineSource"));
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
    }

    MenuItem getClassCheckItem(String s, String t) {
        return new MenuItem(s, new MyCommand("main", t));
    }

    public void wallsChanged() {
    	changedWalls = true;
    }
    
    public void menuPerformed(String menu, String item) {
    	if (item == "delete") {
    		if (selectedObject != null) {
    			dragObjects.remove(selectedObject);
    			selectedObject = null;
    			wallsChanged();
    		}
    	}
    	if (item == "edit")
    		doEdit(selectedObject);
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
    	if (newObject != null) {
    		newObject.setInitialPosition();
    		dragObjects.add(newObject);
    		setSelectedObject(newObject);
    	}
}

	void calcExceptions() {
		int x, y;
		// if walls are in place on border, need to extend that through
		// hidden area to avoid "leaks"
		for (x = 0; x != gridSizeX; x++)
			for (y = 0; y < windowOffsetY; y++) {
				walls[x + gw * y] = walls[x + gw * windowOffsetY];
				walls[x + gw * (gridSizeY - y - 1)] = walls[x + gw
						* (gridSizeY - windowOffsetY - 1)];
			}
		for (y = 0; y < gridSizeY; y++)
			for (x = 0; x < windowOffsetX; x++) {
				walls[x + gw * y] = walls[windowOffsetX + gw * y];
				walls[gridSizeX - x - 1 + gw * y] = walls[gridSizeX
						- windowOffsetX - 1 + gw * y];
			}
		// generate exceptional array, which is useful for doing
		// special handling of elements
		for (x = 1; x < gridSizeX - 1; x++)
			for (y = 1; y < gridSizeY - 1; y++) {
				int gi = x + gw * y;
				exceptional[gi] = walls[gi - 1] || walls[gi + 1]
						|| walls[gi - gw] || walls[gi + gw] || walls[gi]
						|| medium[gi] != medium[gi - 1]
						|| medium[gi] != medium[gi + 1];
				if ((x == 1 || x == gridSizeX - 2)
						&& medium[gi] != medium[gridSizeX - 1 - x + gw
								* (y + 1)]
						|| medium[gi] != medium[gridSizeX - 1 - x + gw
								* (y - 1)])
					exceptional[gi] = true;
			}
		// put some extra exceptions at the corners to ensure tadd2, sinth,
		// etc get calculated
		exceptional[1 + gw] = exceptional[gridSizeX - 2 + gw] = exceptional[1
				+ (gridSizeY - 2) * gw] = exceptional[gridSizeX - 2
				+ (gridSizeY - 2) * gw] = true;
	}

	void createWall(int x1, int y1, int x2, int y2) {
		Wall w = new Wall(x1, y1, x2, y2);
		dragObjects.add(w);
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

	void doBorder() {
		int x, y;
		for (x = 0; x < gridSizeX; x++) {
			setWall(x, windowOffsetY);
			setWall(x, windowBottom);
		}
		for (y = 0; y < gridSizeY; y++) {
			setWall(windowOffsetX, y);
			setWall(windowRight, y);
		}
		calcExceptions();
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

	void handleResize() {
		logger.log(Level.SEVERE, cv.getOffsetWidth() +"," + cv.getOffsetHeight());
//		Dimension d = winSize = new Dimension(cv.getOffsetWidth(),cv.getOffsetHeight());
//		if (winSize.width == 0)
//		    return;
//		pixels = null;
//		if (useBufferedImage) {
//		    try {
//		    	
//		    	ImageData id = cvcontext.createImageData(d.width, d.height);
//		    	pixels = id.getData().
//			/* simulate the following code using reflection:
//			   dbimage = new BufferedImage(d.width, d.height,
//			   BufferedImage.TYPE_INT_RGB);
//			   DataBuffer db = (DataBuffer)(((BufferedImage)dbimage).
//			   getRaster().getDataBuffer());
//			   DataBufferInt dbi = (DataBufferInt) db;
//			   pixels = dbi.getData();
//			*/
//			Class biclass = Class.forName("java.awt.image.BufferedImage");
//			Class dbiclass = Class.forName("java.awt.image.DataBufferInt");
//			Class rasclass = Class.forName("java.awt.image.Raster");
//			Constructor cstr = biclass.getConstructor(
//			    new Class[] { int.class, int.class, int.class });
//			dbimage = (Image) cstr.newInstance(new Object[] {
//			    new Integer(d.width), new Integer(d.height),
//			    new Integer(BufferedImage.TYPE_INT_RGB)});
//			Method m = biclass.getMethod("getRaster", null);
//			Object ras = m.invoke(dbimage, null);
//			Object db = rasclass.getMethod("getDataBuffer", null).
//			    invoke(ras, null);
//			pixels = (int[])
//			    dbiclass.getMethod("getData", null).invoke(db, null);
//		    } catch (Exception ee) {
//			// ee.printStackTrace();
//		    	System.out.println("BufferedImage failed");
//		    }
//		}
		/*
		if (pixels == null) {
//		    pixels = new int[d.width*d.height];
//		    int i;
//		    for (i = 0; i != d.width*d.height; i++)
//		    	pixels[i] = 0xFF000000;
		    int i;
		    System.out.println(d.width + "," + d.height);
		    data = cvcontext.createImageData(d.width, d.height);
		    pixels = data.getData();
		    for (i = 0; i < d.width; i++){
		    	for (int j = 0; j < d.height; j++){
//		    		data.setBlueAt(1, i, j);
//		    		pixels.set(i*j, 200);
		    		pixels.set(i*d.height*4 + j*4 + 0,0);
		    		pixels.set(i*d.height*4 + j*4 + 1,0);
		    		pixels.set(i*d.height*4 + j*4 + 2,0);
		    		pixels.set(i*d.height*4 + j*4 + 3,255);
		    	}
		    }
		    
		    logger.log(Level.SEVERE, "Done");
//		    CanvasElement el = cvcontext.getCanvas().
		   
		}
		*/
	}
	
	void drawWalls() {
		doBlankWalls();
		int i;
		for (i = 0; i != dragObjects.size(); i++)
			dragObjects.get(i).prepare();
	}
	
	public void updateRipple() {
		if (cvcontext == null) {
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
				doSources(.25);
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
			updateRippleGL(brightMult);
			setDrawingSelection(.6+.4*Math.sin(t));
			for (i = 0; i != dragObjects.size(); i++)
				dragObjects.get(i).draw();
			setDrawingSelection(-1);
			return;
		}
		
		Graphics realg = new Graphics(backcontext);
//		realg.setColor(Color.black);
		realg.setFont(new Font("sans-serif", 1, 12));
		
		// winSize = realg.
		// if (winSize == null || winSize.width == 0) {
		// // this works around some weird bug in IE which causes the
		// // applet to not show up properly sometimes.
		// // handleResize();
		// System.out.println("Cagada");
		// return;
		// }
		if (increaseResolution) {
			increaseResolution = false;
			if (resBar.getValue() < 495)
				setResolution(resBar.getValue() + 10);
		}
		long sysTime = (long) Duration.currentTimeMillis();
		double tadd = 0;
		if (!stoppedCheck.getState()) {
			int val = 5; // speedBar.getValue();
			tadd = val * .05;
		}
		int i, j;

		System.out.println(dragging+","+selectedSource);
		boolean stopFunc = dragging && selectedSource == -1
				&& view3dCheck.getState() == false
				&& modeChooser.getSelectedIndex() == MODE_SETFUNC;
		if (stoppedCheck.getState())
			stopFunc = true;
		int iterCount = speedBar.getValue();
		
		System.out.println(String.valueOf(stopFunc)+"Rawr"+String.valueOf(stoppedCheck.getState()));
		if (!stopFunc) {
			/*
			 * long sysTime = System.currentTimeMillis(); if (sysTime-secTime >=
			 * 1000) { framerate = frames; steprate = steps; frames = 0; steps =
			 * 0; secTime = sysTime; } lastTime = sysTime;
			 */
			int iter;
			int mxx = gridSizeX - 1;
			int mxy = gridSizeY - 1;
			for (iter = 0; iter != iterCount; iter++) {
				int jstart, jend, jinc;
//				System.out.println(moveDown);
				if (moveDown) {
					// we process the rows in alternate directions
					// each time to avoid any directional bias.
					jstart = 1;
					jend = mxy;
					jinc = 1;
					moveDown = false;
				} else {
					jstart = mxy - 1;
					jend = 0;
					jinc = -1;
					moveDown = true;
				}
				moveRight = moveDown;
				float sinhalfth = 0;
				float sinth = 0;
				float scaleo = 0;
				int curMedium = -1;
				for (j = jstart; j != jend; j += jinc) {
					int istart, iend, iinc;
					if (moveRight) {
						iinc = 1;
						istart = 1;
						iend = mxx;
						moveRight = false;
					} else {
						iinc = -1;
						istart = mxx - 1;
						iend = 0;
						moveRight = true;
					}
					int gi = j * gw + istart;
					int giEnd = j * gw + iend;
					for (; gi != giEnd; gi += iinc) {
						// calculate equilibrum point of this
						// element's oscillation
						float previ = func[gi - 1];
						float nexti = func[gi + 1];
						float prevj = func[gi - gw];
						float nextj = func[gi + gw];
						float basis = (nexti + previ + nextj + prevj) * .25f;
						if (exceptional[gi]) {
							if (curMedium != medium[gi]) {
								curMedium = medium[gi];
								double tadd2 = tadd
										* (1 - (mediumMaxIndex / mediumMax)
												* curMedium);
								sinhalfth = (float) Math.sin(tadd2 / 2);
								sinth = (float) (Math.sin(tadd2) * dampcoef);
								scaleo = (float) (1 - Math.sqrt(4 * sinhalfth
										* sinhalfth - sinth * sinth));
							}
							if (walls[gi])
								continue;
							int count = 4;
							if (fixedEndsCheck.getState()) {
								if (walls[gi - 1])
									previ = 0;
								if (walls[gi + 1])
									nexti = 0;
								if (walls[gi - gw])
									prevj = 0;
								if (walls[gi + gw])
									nextj = 0;
							} else {
								if (walls[gi - 1])
									previ = walls[gi + 1] ? func[gi]
											: func[gi + 1];
								if (walls[gi + 1])
									nexti = walls[gi - 1] ? func[gi]
											: func[gi - 1];
								if (walls[gi - gw])
									prevj = walls[gi + gw] ? func[gi] : func[gi
											+ gw];
								if (walls[gi + gw])
									nextj = walls[gi - gw] ? func[gi] : func[gi
											- gw];
							}
							basis = (nexti + previ + nextj + prevj) * .25f;
						}
						// what we are doing here (aside from damping)
						// is rotating the point (func[gi], funci[gi])
						// an angle tadd about the point (basis, 0).
						// Rather than call atan2/sin/cos, we use this
						// faster method using some precomputed info.
						Float a = 0f;
						Float b = 0f;
						if (damp[gi] == 1f) {
							a = func[gi] - basis;
							b = funci[gi];
						} else {
							a = (func[gi] - basis) * damp[gi];
							b = funci[gi] * damp[gi];
						}
						func[gi] = basis + a * scaleo - b * sinth;
						funci[gi] = b * scaleo + a * sinth;
					}
				}
				setup.eachFrame();
				steps++;
				filterGrid();
			}
		}

		if (view3dCheck.getState())
			draw3dView();
		else
			draw2dView();

		// if (imageSource != null)
		// imageSource.newPixels();
		System.out.println(dragStartX+"," + view3dCheck.getState());
		cvcontext.putImageData(data, 0, 0);

		if (dragStartX >= 0 && !view3dCheck.getState()) {
			int x = dragStartX * windowWidth / winSize.width;
			int y = windowHeight - 1
					- (dragStartY * windowHeight / winSize.height);
			String s = "(" + x + "," + y + ")";
			realg.setColor(Color.white);
			Font fm = realg.getFont();
			int h = 5 + fm.size;
			realg.fillRect(0, winSize.height - h, fm.size + 10, h);
			realg.setColor(Color.black);
			realg.drawString(s, 5, winSize.height - 5);
		}

		/*
		 * frames++; realg.setColor(Color.white); realg.drawString("Framerate: "
		 * + framerate, 10, 10); realg.drawString("Steprate: " + steprate, 10,
		 * 30); lastFrameTime = lastTime;
		 */

		if (!stoppedCheck.getState()) {
			long diff = (long) Duration.currentTimeMillis() - sysTime;
			// we want the time it takes for a wave to travel across the screen
			// to be more-or-less constant, but don't do anything after 5
			// seconds
			if (adjustResolution && diff > 0 && sysTime < startTime + 1000
					&& windowOffsetX * diff / iterCount < 55) {
				increaseResolution = true;
				startTime = sysTime;
			}
			if (dragging && selectedSource == -1
					&& modeChooser.getSelectedIndex() == MODE_FUNCHOLD)
				editFuncPoint(dragX, dragY);

			// cv.repaint(0);
		}
//		cvcontext.drawImage(backcontext.getCanvas(), 0.0, 0.0);
	}

	void doSources(double tadd) {
		t += tadd;
		if (sourceCount > 0) {
			double w = freqBar.getValue() * (t - freqTimeZero)
					* freqMult;
			double w2 = w;
			boolean skip = false;
			switch (auxFunction) {
			case AUX_FREQ:
				w2 = auxBar.getValue() * t * freqMult;
				break;
			case AUX_PHASE:
				w2 = w + (auxBar.getValue() - 1) * (pi / 29);
				break;
			}
			double v = 0;
			double v2 = 0;
			switch (sourceWaveform) {
			case SWF_SIN:
				v = Math.cos(w);
				if (sourceCount >= (sourcePlane ? 4 : 2))
					v2 = Math.cos(w2);
				else if (sourceFreqCount == 2)
					v = (v + Math.cos(w2)) * .5;
				break;
			case SWF_SQUARE:
				w %= pi * 2;
				v = (w < pi) ? 1 : -1;
				break;
			case SWF_PULSE: {
				w %= pi * 2;
				double pulselen = pi / 4;
				double pulselen2 = freqBar.getValue() * .2;
				if (pulselen2 < pulselen)
					pulselen = pulselen2;
				v = (w > pulselen) ? 0 : Math.sin(w * pi / pulselen);
				if (w > pulselen * 2)
					skip = true;
			}
				break;
			}
			int j;
			for (j = 0; j != sourceCount; j++) {
				if ((j % 2) == 0)
					sources[j].v = (float) (v * setup.sourceStrength());
				else
					sources[j].v = (float) (v2 * setup.sourceStrength());
			}
			if (sourcePlane) {
				if (!skip) {
					for (j = 0; j != sourceCount / 2; j++) {
						OscSource src1 = sources[j * 2];
						OscSource src2 = sources[j * 2 + 1];
						OscSource src3 = sources[j];
						drawLineSource(src1.x, src1.y, src2.x, src2.y,
								src3.v); // , w);
					}
				}
			} else {
				if (sourceMoving) {
					int sy;
					movingSourcePos += tadd * .02 * auxBar.getValue();
					double wm = movingSourcePos;
					int h = windowHeight - 3;
					wm %= h * 2;
					sy = (int) wm;
					if (sy > h)
						sy = 2 * h - sy;
					sy += windowOffsetY + 1;
					sources[0].y = sy;
				}
				int i;
				for (i = 0; i != sourceCount; i++) {
					OscSource src = sources[i];
					drawSource(src.x, src.y, src.v);
				}
			}
		}
	}
	
	// filter out high-frequency noise
	int filterCount;

	void filterGrid() {
		int x, y;
		if (fixedEndsCheck.getState())
			return;
		if (sourceCount > 0 && freqBarValue > 23)
			return;
		if (sourceFreqCount >= 2 && auxBar.getValue() > 23)
			return;
		if (++filterCount < 10)
			return;
		filterCount = 0;
		for (y = windowOffsetY; y < windowBottom; y++)
			for (x = windowOffsetX; x < windowRight; x++) {
				int gi = x + y * gw;
				if (walls[gi])
					continue;
				if (func[gi - 1] < 0 && func[gi] > 0 && func[gi + 1] < 0
						&& !walls[gi + 1] && !walls[gi - 1])
					func[gi] = (func[gi - 1] + func[gi + 1]) / 2;
				if (func[gi - gw] < 0 && func[gi] > 0 && func[gi + gw] < 0
						&& !walls[gi - gw] && !walls[gi + gw])
					func[gi] = (func[gi - gw] + func[gi + gw]) / 2;
				if (func[gi - 1] > 0 && func[gi] < 0 && func[gi + 1] > 0
						&& !walls[gi + 1] && !walls[gi - 1])
					func[gi] = (func[gi - 1] + func[gi + 1]) / 2;
				if (func[gi - gw] > 0 && func[gi] < 0 && func[gi + gw] > 0
						&& !walls[gi - gw] && !walls[gi + gw])
					func[gi] = (func[gi - gw] + func[gi + gw]) / 2;
			}
	}

	void plotPixel(int x, int y, int pix) {
		if (x < 0 || x >= winSize.width){
			System.out.println("return");
			return;
		}
//		logger.log(Level.SEVERE,"no ret" + x + "," + y);
		try {
//			pixels[x + y * winSize.width] = pix;
//			data.setBlueAt(1, x,y);
//			pixels.set(x+y * winSize.width, 200);
			pixels.set(x*4 + y*winSize.width*4 + 0,Color.hex2Rgb(String.valueOf(pix)).getRed());
    		pixels.set(x*4 + y*winSize.width*4 + 1,Color.hex2Rgb(String.valueOf(pix)).getGreen());
    		pixels.set(x*4 + y*winSize.width*4 + 2,Color.hex2Rgb(String.valueOf(pix)).getBlue());
    		pixels.set(x*4 + y*winSize.width*4 + 3,255);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	// draw a circle the slow and dirty way
	void plotSource(int n, int xx, int yy) {
		int rad = sourceRadius;
		int j;
		int col = (sourceColor.getRed() << 16) | (sourceColor.getGreen() << 8)
				| (sourceColor.getBlue()) | 1;
		
		
		
		if (n == selectedSource)
			col ^= 255;
		
//		logger.log(Level.SEVERE, col+"");
		for (j = 0; j <= rad; j++) {
			int k = (int) (Math.sqrt(rad * rad - j * j) + .5);
			plotPixel(xx + j, yy + k, col);
			plotPixel(xx + k, yy + j, col);
			plotPixel(xx + j, yy - k, col);
			plotPixel(xx - k, yy + j, col);
			plotPixel(xx - j, yy + k, col);
			plotPixel(xx + k, yy - j, col);
			plotPixel(xx - j, yy - k, col);
			plotPixel(xx - k, yy - j, col);
			plotPixel(xx, yy + j, col);
			plotPixel(xx, yy - j, col);
			plotPixel(xx + j, yy, col);
			plotPixel(xx - j, yy, col);
		}
	}

	void draw2dView() {
		int ix = 0;
		int i, j, k, l;
		for (j = 0; j != windowHeight; j++) {
			ix = winSize.width * (j * winSize.height / windowHeight);
			int j2 = j + windowOffsetY;
			int gi = j2 * gw + windowOffsetX;
			int y = j * winSize.height / windowHeight;
			int y2 = (j + 1)  * winSize.height / windowHeight;
			for (i = 0; i != windowWidth; i++, gi++) {
				int x = i * winSize.width / windowWidth;
				int x2 = (i + 1) * winSize.width / windowWidth;
				int i2 = i + windowOffsetX;
				double dy = func[gi] * brightMult;
				if (dy < -1)
					dy = -1;
				if (dy > 1)
					dy = 1;
				int col = 0;
				int colR = 0, colG = 0, colB = 0;
				if (walls[gi]) {
					colR = wallColor.getRed();
					colG = wallColor.getGreen();
					colB = wallColor.getBlue();
				} else if (dy < 0) {
					double d1 = -dy;
					double d2 = 1 - d1;
					double d3 = medium[gi] * (1 / 255.01);
					double d4 = 1 - d3;
					double a1 = d1 * d4;
					double a2 = d2 * d4;
					double a3 = d1 * d3;
					double a4 = d2 * d3;
					colR = (int) (negColor.getRed() * a1 + zeroColor.getRed()
							* a2 + negMedColor.getRed() * a3 + medColor
							.getRed() * a4);
					colG = (int) (negColor.getGreen() * a1
							+ zeroColor.getGreen() * a2
							+ negMedColor.getGreen() * a3 + medColor.getGreen()
							* a4);
					colB = (int) (negColor.getBlue() * a1 + zeroColor.getBlue()
							* a2 + negMedColor.getBlue() * a3 + medColor
							.getBlue() * a4);
				} else {
					double d1 = dy;
					double d2 = 1 - dy;
					double d3 = medium[gi] * (1 / 255.01);
					double d4 = 1 - d3;
					double a1 = d1 * d4;
					double a2 = d2 * d4;
					double a3 = d1 * d3;
					double a4 = d2 * d3;
					colR = (int) (posColor.getRed() * a1 + zeroColor.getRed()
							* a2 + posMedColor.getRed() * a3 + medColor
							.getRed() * a4);
					colG = (int) (posColor.getGreen() * a1
							+ zeroColor.getGreen() * a2
							+ posMedColor.getGreen() * a3 + medColor.getGreen()
							* a4);
					colB = (int) (posColor.getBlue() * a1 + zeroColor.getBlue()
							* a2 + posMedColor.getBlue() * a3 + medColor
							.getBlue() * a4);
				}
				col = (255 << 24) | (colR << 16) | (colG << 8) | (colB);
//				logger.log(Level.SEVERE, colR+","+colG+","+colB);
				for (k = 0; k != x2 -x; k++, ix++){
					for (l = 0; l != y2-y; l++){
						pixels.set(l*winSize.width*4 + ix*4 + 0,colR);
			    		pixels.set(l*winSize.width*4 + ix*4+ 1,colG);
			    		pixels.set(l*winSize.width*4 + ix*4 + 2,colB);
			    		pixels.set(l*winSize.width*4 + ix*4+ 3,255);
//						pixels.set(ix + l * winSize.width,col);
//						pixels[ix + l * winSize.width] = col;
					}
				}
			}
		}
		int intf = (gridSizeY / 2 - windowOffsetY) * winSize.height
				/ windowHeight;
		for (i = 0; i != sourceCount; i++) {
			OscSource src = sources[i];
			int xx = src.getScreenX();
			int yy = src.getScreenY();
			plotSource(i, xx, yy);
		}
	}

	double realxmx, realxmy, realymz, realzmy, realzmx, realymadd, realzmadd;
	double viewAngle = pi, viewAngleDragStart;
	double viewZoom = .775, viewZoomDragStart;
	double viewAngleCos = -1, viewAngleSin = 0;
	double viewHeight = -38, viewHeightDragStart;
	double scalex, scaley;
	int centerX3d, centerY3d;
	int xpoints[] = new int[4], ypoints[] = new int[4];
	final double viewDistance = 66;

	void map3d(double x, double y, double z, int xpoints[], int ypoints[],
			int pt) {
		/*
		 * x *= aspectRatio; z *= -4; x *= 16./sampleCount; y *=
		 * 16./sampleCount; double realx = x*viewAngleCos + y*viewAngleSin; //
		 * range: [-10,10] double realy = z-viewHeight; double realz =
		 * y*viewAngleCos - x*viewAngleSin + viewDistance;
		 */
		double realx = realxmx * x + realxmy * y;
		double realy = realymz * z + realymadd;
		double realz = realzmx * x + realzmy * y + realzmadd;
		xpoints[pt] = centerX3d + (int) (realx / realz);
		ypoints[pt] = centerY3d - (int) (realy / realz);
	}

	double scaleMult;

	void scaleworld() {
		scalex = viewZoom * (winSize.width / 4) * viewDistance / 8;
		scaley = -scalex;
		int y = (int) (scaley * viewHeight / viewDistance);
		/*
		 * centerX3d = winSize.x + winSize.width/2; centerY3d = winSize.y +
		 * winSize.height/2 - y;
		 */
		centerX3d = winSize.width / 2;
		centerY3d = winSize.height / 2 - y;
		scaleMult = 16. / (windowWidth / 2);
		realxmx = -viewAngleCos * scaleMult * scalex;
		realxmy = viewAngleSin * scaleMult * scalex;
		realymz = -brightMult * scaley;
		realzmy = viewAngleCos * scaleMult;
		realzmx = viewAngleSin * scaleMult;
		realymadd = -viewHeight * scaley;
		realzmadd = viewDistance;
	}

	void draw3dView() {
		int half = gridSizeX / 2;
		scaleworld();
		int x, y;
		int xdir, xstart, xend;
		int ydir, ystart, yend;
		int sc = windowRight - 1;

		// figure out what order to render the grid elements so that
		// the ones in front are rendered first.
		if (viewAngleCos > 0) {
			ystart = sc;
			yend = windowOffsetY - 1;
			ydir = -1;
		} else {
			ystart = windowOffsetY;
			yend = sc + 1;
			ydir = 1;
		}
		if (viewAngleSin < 0) {
			xstart = windowOffsetX;
			xend = sc + 1;
			xdir = 1;
		} else {
			xstart = sc;
			xend = windowOffsetX - 1;
			xdir = -1;
		}
		boolean xFirst = (viewAngleSin * xdir < viewAngleCos * ydir);

		for (x = 0; x != winSize.width; x++){
			for(int j = 0; j != winSize.height; j++){
				pixels.set(x* winSize.height*4+ j*4+0, 0);
				pixels.set(x* winSize.height*4+ j*4+1, 0);
				pixels.set(x* winSize.height*4+ j*4+2, 0);
				pixels.set(x* winSize.height*4+ j*4+3, 255);
			}
		}
//			pixels[x] = 0xFF000000;
		/*
		 * double zval = 2.0/sampleCount; System.out.println(zval); if
		 * (sampleCount == 128) zval = .1;
		 */
		double zval = .1;
		double zval2 = zval * zval;

		for (x = xstart; x != xend; x += xdir) {
			for (y = ystart; y != yend; y += ydir) {
				if (!xFirst)
					x = xstart;
				for (; x != xend; x += xdir) {
					int gi = x + gw * y;
					map3d(x - half, y - half, func[gi], xpoints, ypoints, 0);
					map3d(x + 1 - half, y - half, func[gi + 1], xpoints,
							ypoints, 1);
					map3d(x - half, y + 1 - half, func[gi + gw], xpoints,
							ypoints, 2);
					map3d(x + 1 - half, y + 1 - half, func[gi + gw + 1],
							xpoints, ypoints, 3);
					double qx = func[gi + 1] - func[gi];
					double qy = func[gi + gw] - func[gi];
					// calculate lighting
					double normdot = (qx + qy + zval) * (1 / 1.73)
							/ Math.sqrt(qx * qx + qy * qy + zval2);
					int[] col = computeColor(gi, normdot);
					fillTriangle(xpoints[0], ypoints[0], xpoints[1],
							ypoints[1], xpoints[3], ypoints[3], col);
					fillTriangle(xpoints[0], ypoints[0], xpoints[2],
							ypoints[2], xpoints[3], ypoints[3], col);
					if (xFirst)
						break;
				}
			}
			if (!xFirst)
				break;
		}
	}

	int[] computeColor(int gix, double c) {
		double h = func[gix] * brightMult;
		if (c < 0)
			c = 0;
		if (c > 1)
			c = 1;
		c = .5 + c * .5;
		double redness = (h < 0) ? -h : 0;
		double grnness = (h > 0) ? h : 0;
		if (redness > 1)
			redness = 1;
		if (grnness > 1)
			grnness = 1;
		if (grnness < 0)
			grnness = 0;
		if (redness < 0)
			redness = 0;
		double grayness = (1 - (redness + grnness)) * c;
		double grayness2 = grayness;
		if (medium[gix] > 0) {
			double mm = 1 - (medium[gix] * (1 / 255.01));
			grayness2 *= mm;
		}
		double gray = .6;
		int ri = (int) ((c * redness + gray * grayness2) * 255);
		int gi = (int) ((c * grnness + gray * grayness2) * 255);
		int bi = (int) ((gray * grayness) * 255);
		int[] ret = new int[3];
		ret[0] = ri;
		ret[1] = gi;
		ret[2] = bi;
		return ret;
//		return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
	}

	void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int[] col) {
		if (x1 > x2) {
			if (x2 > x3) {
				// x1 > x2 > x3
				int ay = interp(x1, y1, x3, y3, x2);
				fillTriangle1(x3, y3, x2, y2, ay, col);
				fillTriangle1(x1, y1, x2, y2, ay, col);
			} else if (x1 > x3) {
				// x1 > x3 > x2
				int ay = interp(x1, y1, x2, y2, x3);
				fillTriangle1(x2, y2, x3, y3, ay, col);
				fillTriangle1(x1, y1, x3, y3, ay, col);
			} else {
				// x3 > x1 > x2
				int ay = interp(x3, y3, x2, y2, x1);
				fillTriangle1(x2, y2, x1, y1, ay, col);
				fillTriangle1(x3, y3, x1, y1, ay, col);
			}
		} else {
			if (x1 > x3) {
				// x2 > x1 > x3
				int ay = interp(x2, y2, x3, y3, x1);
				fillTriangle1(x3, y3, x1, y1, ay, col);
				fillTriangle1(x2, y2, x1, y1, ay, col);
			} else if (x2 > x3) {
				// x2 > x3 > x1
				int ay = interp(x2, y2, x1, y1, x3);
				fillTriangle1(x1, y1, x3, y3, ay, col);
				fillTriangle1(x2, y2, x3, y3, ay, col);
			} else {
				// x3 > x2 > x1
				int ay = interp(x3, y3, x1, y1, x2);
				fillTriangle1(x1, y1, x2, y2, ay, col);
				fillTriangle1(x3, y3, x2, y2, ay, col);
			}
		}
	}

	int interp(int x1, int y1, int x2, int y2, int x) {
		if (x1 == x2)
			return y1;
		if (x < x1 && x < x2 || x > x1 && x > x2)
			System.out.print("interp out of bounds\n");
		return (int) (y1 + ((double) x - x1) * (y2 - y1) / (x2 - x1));
	}

	void fillTriangle1(int x1, int y1, int x2, int y2, int y3, int col[]) {
		// x2 == x3
		int dir = (x1 > x2) ? -1 : 1;
		int x = x1;
		if (x < 0) {
			x = 0;
			if (x2 < 0)
				return;
		}
		if (x >= winSize.width) {
			x = winSize.width - 1;
			if (x2 >= winSize.width)
				return;
		}
		if (y2 > y3) {
			int q = y2;
			y2 = y3;
			y3 = q;
		}
		// y2 < y3
		while (x != x2 + dir) {
			// XXX this could be speeded up
			int ya = interp(x1, y1, x2, y2, x);
			int yb = interp(x1, y1, x2, y3, x);
			if (ya < 0)
				ya = 0;
			if (yb >= winSize.height)
				yb = winSize.height - 1;

			for (; ya <= yb; ya++)
				pixels.set(x*4 + ya * winSize.width*4 + 0, col[0]);
				pixels.set(x*4 + ya * winSize.width*4 + 1, col[1]);
				pixels.set(x*4 + ya * winSize.width*4 + 2, col[2]);
				pixels.set(x*4 + ya * winSize.width*4 + 3, 200);
//				pixels[x + ya * winSize.width] = col;
			x += dir;
			if (x < 0 || x >= winSize.width)
				return;
		}
	}

	int abs(int x) {
		return x < 0 ? -x : x;
	}

	void drawPlaneSource(int x1, int y1, int x2, int y2, float v, double w) {
		if (y1 == y2) {
			if (x1 == windowOffsetX)
				x1 = 0;
			if (x2 == windowOffsetX)
				x2 = 0;
			if (x1 == windowOffsetX + windowWidth - 1)
				x1 = gridSizeX - 1;
			if (x2 == windowOffsetX + windowWidth - 1)
				x2 = gridSizeX - 1;
		}
		if (x1 == x2) {
			if (y1 == windowOffsetY)
				y1 = 0;
			if (y2 == windowOffsetY)
				y2 = 0;
			if (y1 == windowOffsetY + windowHeight - 1)
				y1 = gridSizeY - 1;
			if (y2 == windowOffsetY + windowHeight - 1)
				y2 = gridSizeY - 1;
		}

		/*
		 * double phase = 0; if (sourceChooser.getSelectedIndex() ==
		 * SRC_1S1F_PLANE_PHASE) phase =
		 * (auxBar.getValue()-15)*3.8*freqBar.getValue()*freqMult;
		 */

		// need to draw a line from x1,y1 to x2,y2
		if (x1 == x2 && y1 == y2) {
			func[x1 + gw * y1] = v;
			funci[x1 + gw * y1] = 0;
		} else if (abs(y2 - y1) > abs(x2 - x1)) {
			// y difference is greater, so we step along y's
			// from min to max y and calculate x for each step
			double sgn = sign(y2 - y1);
			int x, y;
			for (y = y1; y != y2 + sgn; y += sgn) {
				x = x1 + (x2 - x1) * (y - y1) / (y2 - y1);
				double ph = sgn * (y - y1) / (y2 - y1);
				int gi = x + gw * y;
				func[gi] = setup.calcSourcePhase(ph, v, w);
				// (phase == 0) ? v :
				// (float) (Math.sin(w+ph));
				funci[gi] = 0;
			}
		} else {
			// x difference is greater, so we step along x's
			// from min to max x and calculate y for each step
			double sgn = sign(x2 - x1);
			int x, y;
			for (x = x1; x != x2 + sgn; x += sgn) {
				y = y1 + (y2 - y1) * (x - x1) / (x2 - x1);
				double ph = sgn * (x - x1) / (x2 - x1);
				int gi = x + gw * y;
				func[gi] = setup.calcSourcePhase(ph, v, w);
				// (phase == 0) ? v :
				// (float) (Math.sin(w+ph));
				funci[gi] = 0;
			}
		}
	}

	int sign(int x) {
		return (x < 0) ? -1 : (x == 0) ? 0 : 1;
	}

	void edit(MouseEvent e) {
		if (view3dCheck.getState())
			return;
		int x = e.getX();
		int y = e.getY();
		if (selectedSource != -1) {
			x = x * windowWidth / winSize.width;
			y = y * windowHeight / winSize.height;
			if (x >= 0 && y >= 0 && x < windowWidth && y < windowHeight) {
				sources[selectedSource].x = x + windowOffsetX;
				sources[selectedSource].y = y + windowOffsetY;
			}
			return;
		}
		/*
		if (dragX == x && dragY == y)
			editFuncPoint(x, y);
		else {
			// need to draw a line from old x,y to new x,y and
			// call editFuncPoint for each point on that line. yuck.
			if (abs(y - dragY) > abs(x - dragX)) {
				// y difference is greater, so we step along y's
				// from min to max y and calculate x for each step
				int x1 = (y < dragY) ? x : dragX;
				int y1 = (y < dragY) ? y : dragY;
				int x2 = (y > dragY) ? x : dragX;
				int y2 = (y > dragY) ? y : dragY;
				dragX = x;
				dragY = y;
				for (y = y1; y <= y2; y++) {
					x = x1 + (x2 - x1) * (y - y1) / (y2 - y1);
					editFuncPoint(x, y);
				}
			} else {
				// x difference is greater, so we step along x's
				// from min to max x and calculate y for each step
				int x1 = (x < dragX) ? x : dragX;
				int y1 = (x < dragX) ? y : dragY;
				int x2 = (x > dragX) ? x : dragX;
				int y2 = (x > dragX) ? y : dragY;
				dragX = x;
				dragY = y;
				for (x = x1; x <= x2; x++) {
					y = y1 + (y2 - y1) * (x - x1) / (x2 - x1);
					editFuncPoint(x, y);
				}
			}
		}
		*/
	}

	void editFuncPoint(int x, int y) {
		int xp = x * windowWidth / winSize.width + windowOffsetX;
		int yp = y * windowHeight / winSize.height + windowOffsetY;
		int gi = xp + yp * gw;
		if (modeChooser.getSelectedIndex() == MODE_WALLS) {
			if (!dragSet && !dragClear) {
				dragClear = walls[gi];
				dragSet = !dragClear;
			}
			walls[gi] = dragSet;
			calcExceptions();
			func[gi] = funci[gi] = 0;
		} else if (modeChooser.getSelectedIndex() == MODE_MEDIUM) {
			if (!dragSet && !dragClear) {
				dragClear = medium[gi] > 0;
				dragSet = !dragClear;
			}
			medium[gi] = (dragSet) ? mediumMax : 0;
			calcExceptions();
		} else {
			if (!dragSet && !dragClear) {
				dragClear = func[gi] > .1;
				dragSet = !dragClear;
			}
			func[gi] = (dragSet) ? 1 : -1;
			funci[gi] = 0;
		}
		// cv.repaint(0);
	}

	void selectSource(MouseEvent me) {
		int x = me.getX();
		int y = me.getY();
		int i;
		for (i = 0; i != sourceCount; i++) {
			OscSource src = sources[i];
			int sx = src.getScreenX();
			int sy = src.getScreenY();
			int r2 = (sx - x) * (sx - x) + (sy - y) * (sy - y);
			if (sourceRadius * sourceRadius > r2) {
				selectedSource = i;
				return;
			}
		}
		selectedSource = -1;
	}

	void setDamping() {
		/*
		 * int i; double damper = dampingBar.getValue() * .00002;// was 5
		 * dampcoef = Math.exp(-damper);
		 */
		dampcoef = 1;
	}

	// public void componentHidden(ComponentEvent e) {
	// }
	//
	// public void componentMoved(ComponentEvent e) {
	// }
	//
	// public void componentShown(ComponentEvent e) {
	// cv.repaint();
	// }
	//
	// public void componentResized(ComponentEvent e) {
	// handleResize();
	// cv.repaint(100);
	// }
	//
	//
	//
	// public void adjustmentValueChanged(AdjustmentEvent e) {
	// System.out.print(((Scrollbar) e.getSource()).getValue() + "\n");
	// if (e.getSource() == resBar) {
	// setResolution();
	// reinit();
	// }
	// if (e.getSource() == dampingBar)
	// setDamping();
	// if (e.getSource() == brightnessBar)
	// cv.repaint(0);
	// if (e.getSource() == freqBar)
	// setFreq();
	// }

	void setFreqBar(int x) {
		freqBar.setValue(x);
		freqBarValue = x;
		freqTimeZero = 0;
	}

	void setFreq() {
		// adjust time zero to maintain continuity in the freq func
		// even though the frequency has changed.
		double oldfreq = freqBarValue * freqMult;
		freqBarValue = freqBar.getValue();
		double newfreq = freqBarValue * freqMult;
		double adj = newfreq - oldfreq;
		freqTimeZero = t - oldfreq * (t - freqTimeZero) / newfreq;
	}

	void setResolution() {
		windowWidth = windowHeight = resBar.getValue();
		int border = windowWidth / 9;
		if (border < 20)
			border = 20;
		windowOffsetX = windowOffsetY = border;
		System.out.println(windowWidth + "," + windowHeight);
		gridSizeX = windowWidth + windowOffsetX * 2;
		gridSizeY = windowHeight + windowOffsetY * 2;
		windowBottom = windowOffsetY + windowHeight - 1;
		windowRight = windowOffsetX + windowWidth - 1;
		setResolutionGL(gridSizeX, gridSizeY, windowOffsetX, windowOffsetY);
		console("res " + gridSizeX + " " + speedBar.getValue());
	}

	void setResolution(int x) {
		resBar.setValue(x);
		setResolution();
		// reinit();
	}

	/*
	public void mouseDragged(MouseEvent e) {
		if (view3dCheck.getState()) {
			view3dDrag(e);
		}
		if (!dragging)
			selectSource(e);
		dragging = true;
		edit(e);
		adjustResolution = false;
		// cv.repaint(0);
	}
*/
	
	void view3dDrag(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		viewAngle = (dragStartX - x) / 40. + viewAngleDragStart;
		while (viewAngle < 0)
			viewAngle += 2 * pi;
		while (viewAngle >= 2 * pi)
			viewAngle -= 2 * pi;
		viewAngleCos = Math.cos(viewAngle);
		viewAngleSin = Math.sin(viewAngle);
		viewHeight = (dragStartY - y) / 10. + viewHeightDragStart;

		/*
		 * viewZoom = (y-dragStartY)/40. + viewZoomDragStart; if (viewZoom < .1)
		 * viewZoom = .1; System.out.println(viewZoom);
		 */
		// cv.repaint();
	}

	// public void mouseClicked(MouseEvent e) {
	// }
	//
	// public void mouseEntered(MouseEvent e) {
	// }
	//
	// public void mouseExited(MouseEvent e) {
	// dragStartX = -1;
	// }
	//
	// public void mousePressed(MouseEvent e) {
	// adjustResolution = false;
	// mouseMoved(e);
	// if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
	// return;
	// dragging = true;
	// edit(e);
	// }
	//
	// public void mouseReleased(MouseEvent e) {
	// if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
	// return;
	// dragging = false;
	// dragSet = dragClear = false;
	// cv.repaint();
	// }
	//
	// public void itemStateChanged(ItemEvent e) {
	// if (e.getItemSelectable() == stoppedCheck) {
	// cv.repaint();
	// return;
	// }
	// if (e.getItemSelectable() == sourceChooser) {
	// if (sourceChooser.getSelectedIndex() != sourceIndex)
	// setSources();
	// }
	// if (e.getItemSelectable() == setupChooser)
	// doSetup();
	// if (e.getItemSelectable() == colorChooser)
	// doColor();
	// }

	void deleteAllObjects() {
		dragObjects.removeAllElements();
		selectedObject = null;
		doBlankWalls();
	}
	
	void doSetup() {
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
		calcExceptions();
		setDamping();
		// System.out.println("setup " + setupChooser.getSelectedIndex());
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
		setColors(wallColor.toInteger(), posColor.toInteger(), negColor.toInteger(),
				zeroColor.toInteger(), posMedColor.toInteger(), negMedColor.toInteger(),
				  medColor.toInteger(), sourceColor.toInteger());
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
		int i, j;
		for (i = 0; i != gridSizeX; i++)
			for (j = gridSizeY / 2; j != gridSizeY; j++)
				medium[i + j * gw] = mediumMax;
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

	void doImport() {
		if (impDialog != null) {
//			requestFocus();
			impDialog.setVisible(false);
			impDialog = null;
		}
		
		String dump = "";

		int i;
		dump = "$ 0 " + resBar.getValue() + " "
				+ sourceChooser.getSelectedIndex() + " "
				+ colorChooser.getSelectedIndex() + " "
				+ fixedEndsCheck.getState() + " " + view3dCheck.getState()
				+ " " + speedBar.getValue() + " " + freqBar.getValue() + " "
				+ brightnessBar.getValue() + " " + auxBar.getValue() + "\n";
		for (i = 0; i != sourceCount; i++) {
			OscSource src = sources[i];
			dump += "s " + src.x + " " + src.y + "\n";
		}
		for (i = 0; i != gridSizeXY;) {
			if (i >= gridSizeX) {
				int istart = i;
				for (; i < gridSizeXY && walls[i] == walls[i - gridSizeX]
						&& medium[i] == medium[i - gridSizeX]; i++)
					;
				if (i > istart) {
					dump += "l " + (i - istart) + "\n";
					continue;
				}
			}
			boolean x = walls[i];
			int m = medium[i];
			int ct = 0;
			for (; i < gridSizeXY && walls[i] == x && medium[i] == m; ct++, i++)
				;
			dump += (x ? "w " : "c ") + ct + " " + m + "\n";
		}
		DialogBox dial = new DialogBox();
		
		impDialog = new ImportDialog(dial, dump, this);
		dial.setWidget(impDialog);
		dial.center();
		dial.show();
	}

	void readImport(String s) {
		doBlank();
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

						resBar.setValue(new Integer(st.nextToken()).intValue());
						setResolution();
						 reinit(false);

						sourceChooser.select(new Integer(st.nextToken())
								.intValue());
						setSources();

						colorChooser.select(new Integer(st.nextToken())
								.intValue());
						doColor();

						fixedEndsCheck.setState(st.nextToken()
								.compareTo("true") == 0);
						view3dCheck
								.setState(st.nextToken().compareTo("true") == 0);
						speedBar.setValue(new Integer(st.nextToken())
								.intValue());
						freqBar.setValue(new Integer(st.nextToken()).intValue());
						brightnessBar.setValue(new Integer(st.nextToken())
								.intValue());
						auxBar.setValue(new Integer(st.nextToken()).intValue());
						break;
					}
					if (tint == 'w' || tint == 'c') {
						boolean w = (tint == 'w');
						int ct = new Integer(st.nextToken()).intValue();
						int md = new Integer(st.nextToken()).intValue();
						for (; ct > 0; ct--, x++) {
							walls[x] = w;
							medium[x] = md;
						}
						break;
					}
					if (tint == 'l') {
						int ct = new Integer(st.nextToken()).intValue();
						for (; ct > 0; ct--, x++) {
							walls[x] = walls[x - gridSizeX];
							medium[x] = medium[x - gridSizeX];
						}
						break;
					}
					if (tint == 's') {
						int sx = new Integer(st.nextToken()).intValue();
						int sy = new Integer(st.nextToken()).intValue();
						sources[srci].x = sx;
						sources[srci].y = sy;
						srci++;
						break;
					}
					System.out.println("unknown type!");
				} catch (Exception ee) {
					ee.printStackTrace();
					break;
				}
				break;
			}
			p += l;

		}
		calcExceptions();
		setDamping();
	}

	// class ImportDialogLayout implements LayoutManager {
	// public ImportDialogLayout() {
	// }
	//
	// public void addLayoutComponent(String name, Component c) {
	// }
	//
	// public void removeLayoutComponent(Component c) {
	// }
	//
	// public Dimension preferredLayoutSize(Container target) {
	// return new Dimension(500, 500);
	// }
	//
	// public Dimension minimumLayoutSize(Container target) {
	// return new Dimension(100, 100);
	// }
	//
	// public void layoutContainer(Container target) {
	// Insets insets = target.insets();
	// int targetw = target.size().width - insets.left - insets.right;
	// int targeth = target.size().height - (insets.top + insets.bottom);
	// int i;
	// int pw = 300;
	// if (target.getComponentCount() == 0)
	// return;
	// Component cl = target.getComponent(target.getComponentCount() - 1);
	// Dimension dl = cl.getPreferredSize();
	// target.getComponent(0).move(insets.left, insets.top);
	// int cw = target.size().width - insets.left - insets.right;
	// int ch = target.size().height - insets.top - insets.bottom
	// - dl.height;
	// target.getComponent(0).resize(cw, ch);
	// int h = ch + insets.top;
	// int x = 0;
	// for (i = 1; i < target.getComponentCount(); i++) {
	// Component m = target.getComponent(i);
	// if (m.isVisible()) {
	// Dimension d = m.getPreferredSize();
	// m.move(insets.left + x, h);
	// m.resize(d.width, d.height);
	// x += d.width;
	// }
	// }
	// }
	// };

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

		abstract void select();

		void doSetupSources() {
			setSources();
		}

		void deselect() {
		}

		double sourceStrength() {
			return 1;
		}

		abstract Setup createNext();

		void eachFrame() {
		}

		float calcSourcePhase(double ph, float v, double w) {
			return v;
		}
	};

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
			for (i = -15; i <= 15; i++)
				// was 5
				setWall(x + i, y);
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
			for (i = windowWidth / 2; i < windowWidth; i++)
				setWall(windowOffsetX + i, windowOffsetY + 3);
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
				calcExceptions();
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
		int i, j;
		for (i = 0; i != sx; i++)
			for (j = 0; j != sy; j++) {
				int gi = i + x + gw * (j + y);
				func[gi] = (float) (Math.sin(pi * nx * (i + 1) / (sx + 1)) * Math
						.sin(pi * ny * (j + 1) / (sy + 1)));
				funci[gi] = 0;
			}
	}

	void setupAcousticMode(int x, int y, int sx, int sy, int nx, int ny) {
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
			int ny = 5;
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
			int ny = 5;
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
			int ny = 5;
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
			int ny = 5;
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
//		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
//		    return;
		dragging = false;
		dragSet = dragClear = false;
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if (dragging) {
			dragMouse(event);
			return;
		}
		int x = event.getX();
		int y = event.getY();
		dragStartX = dragX = x;
		dragStartY = dragY = y;
		viewAngleDragStart = viewAngle;
		viewHeightDragStart = viewHeight;
		selectSource(event);
	}

	void dragMouse(MouseMoveEvent event) {
		if (view3dCheck.getState()) {
			view3dDrag(event);
		}
		if (!dragging)
			selectSource(event);
		dragging = true;
		edit(event);
		adjustResolution = false;

		int xp = event.getX()*windowWidth/winSize.width + windowOffsetX;
		int yp = event.getY()*windowHeight/winSize.height + windowOffsetY;
		if (draggingHandle != null) {
			draggingHandle.dragTo(xp, yp);
			changedWalls = true;
		} else if (selectedObject != null) {
			int dxp = dragX*windowWidth/winSize.width + windowOffsetX;
			int dyp = dragY*windowHeight/winSize.height + windowOffsetY;
			console("drag " + xp + " " + yp + " " + dxp + " " + dyp);
			if (dxp != xp || dyp != yp) {
				selectedObject.drag(xp-dxp, yp-dyp);
				dragX = event.getX();
				dragY = event.getY();
				changedWalls = true;
			}
		} else
			drawPoke(xp, yp);
	}
	
	public void mouseMoved(MouseEvent e) {
		if (dragging)
			return;
		int x = e.getX();
		int y = e.getY();
		dragStartX = dragX = x;
		dragStartY = dragY = y;
		viewAngleDragStart = viewAngle;
		viewHeightDragStart = viewHeight;
		selectSource(e);
		// if (stoppedCheck.getState())
		// cv.repaint(0);
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		adjustResolution = false;
		mouseMoved(event);
//		if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
//		    return;
		dragging = true;
		edit(event);
		
		double minf = 9 * windowWidth/winSize.height;
		double bestf = minf;
		int xp = event.getX()*windowWidth/winSize.width + windowOffsetX;
		int yp = event.getY()*windowHeight/winSize.height + windowOffsetY;
		draggingHandle = null;
		if (selectedObject != null) {
			int i;
			for (i = 0; i != selectedObject.handles.size(); i++) {
				DragHandle dh = selectedObject.handles.get(i);
				double r = DragObject.hypotf(xp-dh.x, yp-dh.y);
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
			double ht = obj.hitTest(xp, yp);
			
	        // if there are no better options, select a RectDragObject if we're tapping
	        // inside it.
			if (ht > minf && !obj.hitTestInside(xp, yp))
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
			drawPoke(xp, yp);
		
		/*
		handler = cv.addMouseMoveHandler(new MouseMoveHandler(){
            public void onMouseMove(MouseMoveEvent e) {
            	if (view3dCheck.getState()) {
            	    view3dDrag(e);
            	}
            	if (!dragging)
            	    selectSource(e);
            	dragging = true;
            	edit(e);
            	adjustResolution = false;
            }
      });
      */
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
		// TODO Auto-generated method stub

	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		dragging = false;
		dragSet = dragClear = false;
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

    void doPopupMenu() {
    	if (selectedObject != null) {
                elmEditMenuItem .setEnabled(selectedObject.getEditInfo(0) != null);
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
		// TODO Auto-generated method stub

	}

	void doCreateWall() {
		Wall w = new Wall();
		w.setInitialPosition();
		dragObjects.add(w);
	}
	
	Rectangle findSpace(DragObject obj, int sx, int sy) {
		return new Rectangle(gridSizeX/2, gridSizeY/2, sx, sy);
	}
	
	@Override
	public void onClick(ClickEvent event) {
		if (event.getSource() == blankButton) {
			doBlank();
		} else if (event.getSource() == blankWallsButton) {
			deleteAllObjects();
		} else if (event.getSource() == borderButton) {
			doCreateWall();
//			doBorder();
		} else if (event.getSource() == boxButton) {
			Box b = new Box();
			b.setInitialPosition();
			dragObjects.add(b);
		} else if (event.getSource() == exportButton) {
			 doImport();
		}
		
		if (event.getSource() == resBar) {
		    setResolution();
		    reinit();
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
