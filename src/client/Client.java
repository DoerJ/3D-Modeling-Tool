package client;

import client.testPages.StarburstLineTest;
import geometry.Point2D;
import windowing.graphics.Color;
import line.AlternatingLineRenderer;
import line.ExpensiveLineRenderer;
import line.LineRenderer;
import client.ColoredDrawable;		//
import client.testPages.MeshPolygonTest;		//
import client.testPages.ParallelogramTest;		//
import client.testPages.RandomLineTest;			//
import client.testPages.RandomPolygonTest;			//
import client.testPages.StarburstPolygonTest;		//
import client.testPages.centeredTriangleTest;		//
import line.AntialiasingLineRenderer;		//
import line.BresenhamLineRenderer;		//
import line.DDALineRenderer;		//
import polygon.wireframeRenderer;
import polygon.BlerpPolygonRenderer;		//
import polygon.FilledPolygonRenderer;
import polygon.WireframePolygonRenderer;		//
import client.interpreter.SimpInterpreter;		//
import polygon.*;
import windowing.PageTurner;
import windowing.drawable.Drawable;
import windowing.drawable.GhostWritingDrawable;
import windowing.drawable.InvertedYDrawable;
import windowing.drawable.TranslatingDrawable;
import windowing.graphics.Dimensions;

public class Client implements PageTurner {
	private static final int ARGB_WHITE = 0xff_ff_ff_ff;
	private static final int ARGB_GREEN = 0xff_00_ff_40;
	
	private static final int NUM_PAGES = 16;
	protected static final double GHOST_COVERAGE = 0.14;
	
	// update1
	private static final int NUM_PANELS = 1;		// number of panels
	private static final Dimensions PANEL_SIZE = new Dimensions(650, 650);
	
	// update2
	// Point2D stores the fixed corners of four panels
	private static final Point2D lowCornersOfPanels = new Point2D(50, 50);

	private final Drawable drawable;
	private int pageNumber = 0;
	
	private Drawable image;
	// update3
	private Drawable panels;						// each of four panels is a drawable object
	private Drawable[] ghostPanels;					// use transparency and write only white
	private Drawable largePanel;
	
	private PolygonRenderer polygonRenderer;
	private PolygonRenderer polygonRenderer1;
	private PolygonRenderer polygonRenderer2;
	private PolygonRenderer polygonRenderer3;
	
	// new SimpInterpreter
	SimpInterpreter interpreter;
	DepthCueingDrawable depthCueingDrawable;
	RendererTrio renderers;
	
	public Client(Drawable drawable) {
		this.drawable = drawable;	
		createDrawables();
		createRenderers();
	}

	public void createDrawables() {
		image = new InvertedYDrawable(drawable);
		image = new TranslatingDrawable(image, point(0, 0), dimensions(750, 750));
		image = new ColoredDrawable(image, ARGB_WHITE);   // set the background white and panels black
		
		largePanel = new TranslatingDrawable(image, point(  50, 50),  dimensions(650, 650));
		
		createPanels();
		createGhostPanels();
	}

	public void createPanels() {
		
		// 4 panels are created here!
		for(int index = 0; index < NUM_PANELS; index++) {
			panels = new TranslatingDrawable(image, lowCornersOfPanels, PANEL_SIZE);
		}
	}

	private void createGhostPanels() {
		ghostPanels = new Drawable[NUM_PANELS];
		
		for(int index = 0; index < NUM_PANELS; index++) {
			ghostPanels[index] = new GhostWritingDrawable(drawable, GHOST_COVERAGE);
		}
	}
	private Point2D point(int x, int y) {
		return new Point2D(x, y);
	}	
	private Dimensions dimensions(int x, int y) {
		return new Dimensions(x, y);
	}
	// update 4
	private void createRenderers() {
		polygonRenderer = wireframeRenderer.make();
		polygonRenderer1 = WireframePolygonRenderer.make();
		polygonRenderer2 = FilledPolygonRenderer.make();
		polygonRenderer3 = TriangleFilledPolygonRenderer.make();
		renderers = new RendererTrio();
	}
	
	@Override
	public void nextPage() {
		System.out.println("PageNumber " + (pageNumber + 1));
		pageNumber = (pageNumber + 1) % NUM_PAGES;
		
		image.clear();
		largePanel.clear();
	
		switch(pageNumber) {

		case 1:	 depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
				 interpreter = new SimpInterpreter("page-a1.simp", depthCueingDrawable, renderers);
				 interpreter.interpret();
				 break;
		case 2:  depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-a2.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;
		case 3:  depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-a3.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;
		case 4:  depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-b1.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;
		case 5:  depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-b2.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;
		case 6:  depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-b3.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;
		case 7:  depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-c1.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;
		case 8:  depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-c2.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;
		case 9:  depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-c3.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break; 
		case 10: depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-d.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break; 
		case 11: depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-e.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;
		case 12: depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-f1.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;
		case 13: depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-f2.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;	 
		case 14: depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-g.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;	
		case 15: depthCueingDrawable = new DepthCueingDrawable(panels, 0, -200, Color.WHITE); 
		 		 interpreter = new SimpInterpreter("page-h.simp", depthCueingDrawable, renderers);
		 		 interpreter.interpret();
		 		 break;	 
		default: defaultPage();
				 break;
		}
	}

	public void polygonDrawerPage(Drawable[] panelArray) {
		image.clear();
		panels.clear();
		new MeshPolygonTest(panels, polygonRenderer, MeshPolygonTest.USE_PERTURBATION);
	}

	private void defaultPage() {
		image.clear();
		panels.fill(ARGB_GREEN, Double.MAX_VALUE);
	}
}
