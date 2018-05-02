package client.interpreter;

import java.util.ArrayList;
import java.util.Stack;
import client.interpreter.LineBasedReader;
import geometry.Point3DH;
import geometry.Rectangle;
import geometry.Vertex3D;
import line.LineRenderer;
import client.*;		//
import client.DepthCueingDrawable;		//
import client.RendererTrio;			//
import geometry.Transformation;			//
import polygon.Polygon;
import polygon.PolygonRenderer;
import polygon.Shader;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import windowing.graphics.Dimensions;
import client.interpreter.*;
import shading.*;
import java.util.List;

import java.io.File;

public class SimpInterpreter {
	
	private Vertex3D light_location;
	
	private static final int NUM_TOKENS_FOR_POINT = 3;
	private static final int NUM_TOKENS_FOR_COMMAND = 1;
	private static final int NUM_TOKENS_FOR_COLORED_VERTEX = 6;
	private static final int NUM_TOKENS_FOR_UNCOLORED_VERTEX = 3;
	private static final char COMMENT_CHAR = '#';
	private RenderStyle renderStyle;
	private double z_near = -Double.MAX_VALUE;
	private double z_far = -Double.MAX_VALUE;
	private double attenuation_a;
	private double attenuation_b;
	
	private static double CTM[][] = new double[4][4];                                    
	public static Transformation worldToScreen;
	public static double CTM_inverse[][] = worldToScreen.identity().getMatrix();
	public static double toWindowSpace[][] = worldToScreen.identity().getMatrix();
	public static double toPerspective[][] = worldToScreen.identity().getMatrix();
	public static double toView[][] = worldToScreen.identity().getMatrix();
	public static double toBlackEdge[][] = worldToScreen.identity().getMatrix();
	
	private static int WORLD_LOW_X = -100;
	private static int WORLD_HIGH_X = 100;
	private static int WORLD_LOW_Y = -100;
	private static int WORLD_HIGH_Y = 100;
	private double x_low = 0;
	private double y_low = 0;
	private double x_high = 0;
	private double y_high = 0;
	private double hither = 0;
	private double yon = 0;
	public static double camera_height = 0;
	public static double camera_width = 0;
	private double specular_coefficient = 0.3;
	private double specular_exp = 8;
	
	private LineBasedReader reader;
	private Stack<LineBasedReader> readerStack;
	private Stack<double[][]> CTMStack;
	
	private Color defaultColor = Color.WHITE;
	private Color ambientLight = Color.BLACK;
	private Color ambient_color;
	private Color surfaceColor = Color.WHITE;
	private Color depthColor = Color.BLACK;
	private Color objectColor = Color.WHITE;
	private Color light_intensity;
	
	private Drawable drawable;
	private Drawable depthCueingDrawable;
	private LineRenderer lineRenderer;
	public static PolygonRenderer filledRenderer;
	public static PolygonRenderer wireframeRenderer;
	public static PolygonRenderer phongRenderer;
	private Transformation cameraToScreen;
	private Clipper clipper;
	private FlatShading flat;
	private GouraudShading gouraud;
	public static GouraudShading phong;
	private Lighting lightBulb;
	// array list used for storing multiple light sources
	private ArrayList<Lighting> lightSources;
	
	public static boolean render_style = false;
	public static boolean black_edge = false;
	// shading style
	private boolean flat_shading = false;
	private boolean phong_shading = false;
	private boolean gouraud_shading = false;
	
	private String shading_style = "default";
	
	// z buffer
	public static double z_buffer[][];

	public enum RenderStyle {
		FILLED,
		WIREFRAME;
	}
	public SimpInterpreter(String filename, 
			Drawable drawable,
			RendererTrio renderers) {
		this.drawable = drawable;
		this.depthCueingDrawable = drawable;
		this.lineRenderer = renderers.getLineRenderer();
		this.filledRenderer = renderers.getFilledRenderer();
		this.wireframeRenderer = renderers.getWireframeRenderer();
		this.phongRenderer = renderers.getPhongRenderer();
		this.defaultColor = Color.fromARGB(depthCueingDrawable.getPixel(0,0));
		makeWorldToScreenTransform(drawable.getDimensions());
		
		reader = new LineBasedReader(filename);
		readerStack = new Stack<>();
		CTMStack = new Stack<>();
		renderStyle = RenderStyle.FILLED;
		CTM = Transformation.identity().getMatrix();
		render_style = true;
		// initialize z_buffer;
		z_buffer = new double[651][651];
		z_buffer = initializeZbuffer(z_buffer);
		camera_height = drawable.getDimensions().getHeight();
		camera_width = drawable.getDimensions().getWidth();
		black_edge = false;
		lightSources = new ArrayList<Lighting>();
		
		// -------------------------------CHECKED!
//		System.out.println("CTM is: ");
//		for(int i = 0; i < 4; i++) {
//			for(int j = 0; j < 4; j++) {
//				System.out.print(CTM[i][j] + " ");
//			}
//			System.out.println();
//		}
		// -----------------------------------
		// initialize perspective matrix and view matrix
		initializePerspective();
	}
	
	// initialize z_buffer with all entries of -200;
	private double[][] initializeZbuffer(double z_buffer[][]) {
		for(int i = 0; i < 650; i++) {
			for(int j = 0; j < 650; j++) {
				z_buffer[i][j] = -200;
			}
		}
		return z_buffer;
	}
	
	private void initializeView(double xHIGH, double xLOW) {

		double deltaY = y_high - y_low;
		double deltaX = x_high - x_low;
		// if deltaX >= deltaY, deno = deltaX
		// else deno = deltaY
		double deno = deltaX >= deltaY ? deltaX : deltaY;
		double ratio = deltaX / deltaY;
		double height = drawable.getDimensions().getHeight() / deno;
		double width = drawable.getDimensions().getWidth() / deno;
		
		if(ratio >= 1) {
			camera_height = ((int)Math.round(drawable.getDimensions().getHeight() / ratio));
		} 
		else {
			camera_width = ((int)Math.round(drawable.getDimensions().getWidth() / ratio));
		}
		
		// define toBlackEdge matrix (the translation pixels need for fitting in black edges)
		double bottom;
		double right;
		if(camera_height >= camera_width) {
			right = (650 - camera_width) / 2;
			toBlackEdge[0][3] = (int)right;
		}
		else {
			bottom = (650 - camera_height) / 2;
			toBlackEdge[1][3] = (int)bottom;
		}
		
		// define view matrix
		toView[0][0] = 650 / Math.abs(xHIGH - xLOW);
		toView[0][3] = 650 / Math.abs(xHIGH - xLOW);
		toView[1][3] = 650 / Math.abs(xHIGH - xLOW);
		toView[1][1] = 650 / Math.abs(xHIGH - xLOW);
		
//		System.out.println("The View matrix is: ");
//		for(int i = 0; i < 4; i++) {
//			for(int j = 0; j < 4; j++) {
//				System.out.print(toView[i][j] + " , ");
//			}
//			System.out.println();
//		}
	}
	
	public void initializePerspective() {
		// define perspective matrix	
		toPerspective[3][3] = 0;
		toPerspective[3][2] = -1;
	}
	
	private void makeWorldToScreenTransform(Dimensions dimensions) {
		// TODO: fill this in
		double height = dimensions.getHeight();
		double width = dimensions.getWidth();
		
		// scale the object space
		double scale_height = height / (WORLD_HIGH_Y - WORLD_LOW_Y);
		double scale_width = width / (WORLD_HIGH_X - WORLD_LOW_X);
		
		// translate the object space
		double translate_height = 100;
		double translate_width = 100;
		
		// translate to toWindowSpace 
		toWindowSpace[0][3] = translate_width;
		toWindowSpace[1][3] = translate_height;
		
		//System.out.println("The width of dimension: " + width);
		//System.out.println("The height of dimension: " + height);
		
		// a temp matrix storing scale
		double temp[][] = new double[4][4];
		temp = worldToScreen.identity().getMatrix();
		temp[0][0] = scale_width;
		temp[1][1] = scale_height;
		
		// multiply temp matrix with toWindowSpace
		double new_temp[][] = worldToScreen.allZero().getMatrix();
		double value;
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += temp[i][k] * toWindowSpace[k][j] ;
				}
				new_temp[i][j] = value;
			}
		}
		toWindowSpace = new_temp;
		
		System.out.println("The toWindowSpace matrix is: ");
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				System.out.print(toWindowSpace[i][j] + " , ");
			}
			System.out.println();
		}
	}
	// interpret object file
	private void objFile(String filename) {
		ObjReader objReader = new ObjReader(filename, defaultColor, lightSources, shading_style, 
				specular_coefficient, specular_exp, objectColor);
		// store object vertex, texture vertex, normal vertex, and faces into lists
		objReader.read();
		// start rendering
		objReader.render(drawable);
	}
	
	public void interpret() {
		while(reader.hasNext()) {
			String line = reader.next().trim();
			interpretLine(line);
			while(!reader.hasNext()) {
				if(readerStack.isEmpty()) {
					return;
				}
				else {
					reader = readerStack.pop();
				}
			}
		}
	}
	public void interpretLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretCommand(tokens);
			}
		}
	}
	private void interpretCommand(String[] tokens) {
		switch(tokens[0]) {
		case "{" :      push();   break;
		case "}" :      pop();    break;
		case "wire" :   wire();   break;
		case "filled" : filled(); break;
		
		case "file" :		interpretFile(tokens);		break;
		case "scale" :		interpretScale(tokens);		break;
		case "translate" :	interpretTranslate(tokens);	break;
		case "rotate" :		interpretRotate(tokens);	break;
		case "line" :		interpretLine(tokens);		break;
		case "polygon" :	interpretPolygon(tokens);	break;
		case "camera" :		interpretCamera(tokens);	break;
		case "surface" :	interpretSurface(tokens);	break;
		case "ambient" :	interpretAmbient(tokens);	break;
		case "depth" :		interpretDepth(tokens);		break;
		case "obj" :		interpretObj(tokens);		break;
		case "flat" :		interpretFlat();			break;
		case "gouraud" :	interpretGouraud();			break;
		case "phong" :		interpretPhong();			break;
		case "light" :		interpretLight(tokens);		break;
		
		default :
			System.err.println("bad input line: " + tokens);
			break;
		}
	}

	private void push() {
		CTMStack.push(CTM);
		// TODO: finish this method
	}
	private void pop() {
		CTM = CTMStack.pop();
		// TODO: finish this method
	}
	private void wire() {
		renderStyle = RenderStyle.WIREFRAME;
		render_style = false;
		// TODO: finish this method
	}
	private void filled() {
		renderStyle = RenderStyle.FILLED;
		render_style = true;
		// TODO: finish this method
	}
	
	// interpret flat
	private void interpretFlat() {
		flat_shading = true;
		shading_style = "flat";
	}
	// interpret phong
	private void interpretPhong() {
		phong_shading = true;
		shading_style = "phong";
	}
	// interpret gouraud
	private void interpretGouraud() {
		gouraud_shading = true;
		shading_style = "gouraud";
	}
	// interpret light source
	private void interpretLight(String[] tokens) {
		double light_r = cleanNumber(tokens[1]);
		double light_g = cleanNumber(tokens[2]);
		double light_b = cleanNumber(tokens[3]);
		// form light color (light intensity)
		light_intensity = new Color(light_r, light_g, light_b);
		// get attenuation constants
		attenuation_a = cleanNumber(tokens[4]);
		attenuation_b = cleanNumber(tokens[5]);
		// get the location of light source
		light_location = new Vertex3D(0, 0, 0, light_intensity);
		Point3DH light_CTM = getLightInCTM(light_location);		
		Point3DH light_inverseCTM = transformToCameraspace(light_CTM);
		// the location of light source
		light_location = new Vertex3D(light_inverseCTM.getX(), light_inverseCTM.getY(), 
				light_inverseCTM.getZ(), light_location.getColor());
		lightBulb = new Lighting(light_intensity, attenuation_a, attenuation_b, light_location);
		// add lightBulb into lighting arrayList
		lightSources.add(lightBulb);
	}
	// get the light location after CTM
	private Point3DH getLightInCTM(Vertex3D location) {
		
		double current_point[] = new double[4];
		double temp[] = new double[4];
		current_point[0] = location.getX();
		current_point[1] = location.getY();
		current_point[2] = location.getZ();
		current_point[3] = 1.0;		// w in homogeneous point

		for(int i = 0; i < 4; i++) {
			double value = 0;
			for(int j = 0; j < 4; j++) {
				value += CTM[i][j] * current_point[j];
			}
			temp[i] = value;
		}
		current_point = temp;
		
		Point3DH CTM_point = new Point3DH(current_point[0], current_point[1], current_point[2]);
		return CTM_point;
	}
	// interpret depth
	private void interpretDepth(String[] tokens) {
		z_near = cleanNumber(tokens[1]);
		z_far = cleanNumber(tokens[2]);
		double depth_r = cleanNumber(tokens[3]);
		double depth_g = cleanNumber(tokens[4]);
		double depth_b = cleanNumber(tokens[5]);
		// depth color is defined here
		depthColor = new Color(depth_r, depth_g, depth_b);
	}
	// interpret object file
	private void interpretObj(String[] tokens) {

		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"'; 
		String filename = quotedFilename.substring(1, length-1);
		filename = filename + ".obj";
		objFile(filename);
	}
	
	// interpret the ambient color
	private void interpretAmbient(String[] tokens) {
		
		double ambient_r = cleanNumber(tokens[1]);
		double ambient_g = cleanNumber(tokens[2]);
		double ambient_b = cleanNumber(tokens[3]);
		// assign the ambient_r, g, b to ambient_color for later use
		ambient_color = new Color(ambient_r, ambient_g, ambient_b);
		defaultColor = ambient_color;
	}
	
	// interpret camera, compute the perspective matrix
	private void interpretCamera(String[] tokens) {
		x_low = cleanNumber(tokens[1]);
		y_low = cleanNumber(tokens[2]);
		x_high = cleanNumber(tokens[3]);
		y_high = cleanNumber(tokens[4]);
		hither = cleanNumber(tokens[5]);
		yon = cleanNumber(tokens[6]);
		// initialize the inverse of CTM
		getCTM_inverse();
		// initialize the view matrix
		initializeView(x_high, x_low);
		// check if black edge should be applied
		if(x_low != y_low || x_high != y_high) {
			black_edge = true;
		}
	}
	// interpret surface color
	private void interpretSurface(String[] tokens) {
		double surface_r = cleanNumber(tokens[1]);
		double surface_g = cleanNumber(tokens[2]);
		double surface_b = cleanNumber(tokens[3]);
		Color surface = new Color(surface_r, surface_g, surface_b);
		surfaceColor = surface;
		// paint object with surface color
		objectColor = surfaceColor;
		specular_coefficient = cleanNumber(tokens[4]);
		specular_exp = cleanNumber(tokens[5]);
	}
	// this one is complete.
	private void interpretFile(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"'; 
		String filename = quotedFilename.substring(1, length-1);
		file(filename + ".simp");
	}
	private void file(String filename) {
		readerStack.push(reader);
		reader = new LineBasedReader(filename);
	}	

	private void interpretScale(String[] tokens) {
		double sx = cleanNumber(tokens[1]);
		double sy = cleanNumber(tokens[2]);
		double sz = cleanNumber(tokens[3]);
		// TODO: finish this method
		
		Transformation trans = new Transformation();
		trans = trans.identity();
		double temp[][] = new double[4][4];
		trans.transformMatrix(0, 0, sx);
		trans.transformMatrix(1, 1, sy);
		trans.transformMatrix(2, 2, sz);
		
		// multiply transformation matrix with CTM
		double value;
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += CTM[i][k] * trans.getMatrix()[k][j] ;
				}
				temp[i][j] = value;
			}
		}
		CTM = temp;
		
		// ---------
		/*
		System.out.println("The current matrix after scaling is: ");
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				System.out.print(CTM[i][j] + " , ");
			}
			System.out.println();
		}
		*/
		// ---------
	}
	private void interpretTranslate(String[] tokens) {
		double tx = cleanNumber(tokens[1]);
		double ty = cleanNumber(tokens[2]);
		double tz = cleanNumber(tokens[3]);
		// TODO: finish this method
		
		// translate matrix
		Transformation trans = new Transformation();
		trans = trans.identity();
		double temp[][] = new double[4][4];
		trans.transformMatrix(0, 3, tx);
		trans.transformMatrix(1, 3, ty);
		trans.transformMatrix(2, 3, tz);
		
		// multiply transformation matrix with CTM
		double value;
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += CTM[i][k] * trans.getMatrix()[k][j] ;
				}
				temp[i][j] = value;
			}
		}
		CTM = temp;
		
		// ---------
		/*
		System.out.println("The current matrix after translation is: ");
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				System.out.print(CTM[i][j] + " , ");
			}
			System.out.println();
		}
		*/
		// ---------	
		
	}
	private void interpretRotate(String[] tokens) {
		String axisString = tokens[1];
		double angleInDegrees = cleanNumber(tokens[2]);

		// TODO: finish this method
		Transformation trans = new Transformation();
		double temp[][] = new double[4][4];
		trans = trans.identity();
		
		if(axisString.equals("X")) {
			trans.transformMatrix(1, 1, Math.cos(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(2, 1, Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(1, 2, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(2, 2, Math.cos(Math.toRadians(angleInDegrees)));
		}
		else if(axisString.equals("Y")) {
			trans.transformMatrix(0, 0, Math.cos(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(0, 2, Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(2, 0, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(2, 2, Math.cos(Math.toRadians(angleInDegrees)));
		}
		else if(axisString.equals("Z")) {
			trans.transformMatrix(0, 0, Math.cos(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(0, 1, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(1, 0, Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(1, 1, Math.cos(Math.toRadians(angleInDegrees)));
		}
		
		// multiply transformation matrix with CTM
		double value;
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += CTM[i][k] * trans.getMatrix()[k][j] ;
				}
				temp[i][j] = value;
			}
		}
		CTM = temp;
		
		// ---------
		/*
		System.out.println("The current matrix after rotation is: ");
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				System.out.print(CTM[i][j] + " , ");
			}
			System.out.println();
		}
		*/
		// ---------
	}
	private static double cleanNumber(String string) {
		return Double.parseDouble(string);
	}
	
	private enum VertexColors {
		COLORED(NUM_TOKENS_FOR_COLORED_VERTEX),
		UNCOLORED(NUM_TOKENS_FOR_UNCOLORED_VERTEX);
		
		private int numTokensPerVertex;
		
		private VertexColors(int numTokensPerVertex) {
			this.numTokensPerVertex = numTokensPerVertex;
		}
		public int numTokensPerVertex() {
			return numTokensPerVertex;
		}
	}
	private void interpretLine(String[] tokens) {			
		Vertex3D[] vertices = interpretVertices(tokens, 2, 1);

		// TODO: finish this method
		lineRenderer.drawLine(vertices[0], vertices[1], drawable);
	}	
	// draw polygons
	private void interpretPolygon(String[] tokens) {
		// a polygon has three vertices
		Vertex3D[] vertices = interpretVertices(tokens, 3, 1);
		// check the shading style and proceed lighting calculation
		// flat shading
		if(flat_shading == true) {
			Polygon plain_polygon = Polygon.make(vertices[0], vertices[1], vertices[2]);
			flat = new FlatShading(plain_polygon, lightSources, specular_coefficient, specular_exp, objectColor);
			Polygon shade_polygon = flat.shade(plain_polygon);
			vertices[0] = shade_polygon.get(0);
			vertices[1] = shade_polygon.get(1);
			vertices[2] = shade_polygon.get(2);
		}
		// gouraud shading
		else {
			Polygon plain_polygon = Polygon.make(vertices[0], vertices[1], vertices[2]);
			gouraud = new GouraudShading(plain_polygon, lightSources, specular_coefficient, specular_exp, objectColor);
			Vertex3D shaded_vertex1 = gouraud.shade(plain_polygon, vertices[1]);
			Vertex3D shaded_vertex2 = gouraud.shade(plain_polygon, vertices[0]);
			Vertex3D shaded_vertex3 = gouraud.shade(plain_polygon, vertices[2]);
			vertices[0] = shaded_vertex1;
			vertices[1] = shaded_vertex2;
			vertices[2] = shaded_vertex3;
		}
		// if phong shading
//		else {
//			Clipper2 new_clipper = new Clipper2(hither, yon, x_low, y_low, x_high, y_high, vertices[0], vertices[1], vertices[2]);
//			ArrayList<Vertex3D> points = new_clipper.getVertex();
//			Polygon plain_polygon = Polygon.make(points.get(0), points.get(1), points.get(2));
//			// pass the polygon in camera space into phongRenderer
//			phong = new GouraudShading(plain_polygon, lightSources, specular_coefficient, specular_exp, objectColor);
//			phongRenderer.drawPolygon(plain_polygon, drawable);
//		}
		// add clipper 
		// perspective matrix and screen matrix is done inside the clipper
		Clipper clipper = new Clipper(hither, yon, x_low, y_low, x_high, y_high, vertices[0], vertices[1], vertices[2]);
		// ---------
		/*
				System.out.println("The CTM is: ");
				for(int i = 0; i < 4; i++) {
					for(int j = 0; j < 4; j++) {
						System.out.print(CTM[i][j] + " , ");
					}
					System.out.println();
				}
				// ---------
				
				// ---------
				System.out.println("The inverse of CTM is: ");
				for(int i = 0; i < 4; i++) {
					for(int j = 0; j < 4; j++) {
						System.out.print(CTM_inverse[i][j] + " , ");
					}
					System.out.println();
				}
				// ---------
				// ---------
				System.out.println("The perspective matrix is: ");
				for(int i = 0; i < 4; i++) {
					for(int j = 0; j < 4; j++) {
						System.out.print(toPerspective[i][j] + " , ");
					}
					System.out.println();
				}
				// ---------
				// ---------
				System.out.println("The view matrix is: ");
				for(int i = 0; i < 4; i++) {
					for(int j = 0; j < 4; j++) {
						System.out.print(toView[i][j] + " , ");
					}
					System.out.println();
				}
				*/
				// ---------
				
		// TODO: finish this method
		int current = 1;
		ArrayList<Vertex3D> points = clipper.getVertex();
		int array_size = points.size() - 2;
		double current_point[] = new double[4];
		double temp[] = new double[4];
		Color color;
		ArrayList<Vertex3D> new_points = new ArrayList<>();
		while(current <= array_size) {
			for (int i = 0; i < points.size(); i ++) {
				Vertex3D point = points.get(i);
				color = point.getColor();
				current_point[0] = point.getX();
				current_point[1] = point.getY();
				current_point[2] = point.getIntZ();
				current_point[3] = 1.0;
				for (int k = 0; k < 4; k++) {
					double lineTotal = 0;
					for (int j = 0; j < 4; j++) {
						lineTotal += toBlackEdge[k][j] * current_point[j];
					}
					temp[k] = lineTotal;
				}
				current_point = temp;
				Point3DH temp_point = new Point3DH(current_point).euclidean();
				Vertex3D p = new Vertex3D(temp_point, color);
				new_points.add(p);
			}
			Polygon triangle;
			if(black_edge == true) {
				triangle = Polygon.make(new_points.get(current), new_points.get(0), new_points.get(current + 1));
			}
			else {
				triangle = Polygon.make(points.get(current), points.get(0), points.get(current + 1));
			}
			if(render_style == true) {
		
					filledRenderer.drawPolygon(triangle, drawable);
			} 
			else {
				wireframeRenderer.drawPolygon(triangle, drawable);
			}
			current++;
		}
	}
	public Vertex3D[] interpretVertices(String[] tokens, int numVertices, int startingIndex) {
		VertexColors vertexColors = verticesAreColored(tokens, numVertices);	
		Vertex3D vertices[] = new Vertex3D[numVertices];
		
		for(int index = 0; index < numVertices; index++) {
			// startingIndex: 1, 4, 7
			vertices[index] = interpretVertex(tokens, startingIndex + index * vertexColors.numTokensPerVertex(), vertexColors);
		}
		return vertices;
	}
	public VertexColors verticesAreColored(String[] tokens, int numVertices) {
		return hasColoredVertices(tokens, numVertices) ? VertexColors.COLORED :
														 VertexColors.UNCOLORED;
	}
	public boolean hasColoredVertices(String[] tokens, int numVertices) {
		return tokens.length == numTokensForCommandWithNVertices(numVertices);
	}
	public int numTokensForCommandWithNVertices(int numVertices) {
		return NUM_TOKENS_FOR_COMMAND + numVertices*(NUM_TOKENS_FOR_COLORED_VERTEX);
	}

	
	private Vertex3D interpretVertex(String[] tokens, int startingIndex, VertexColors colored) {
		// multiply the point by CTM (C)
		Point3DH point = interpretPoint(tokens, startingIndex);
		
		// CTM has been already applied to point --------------
		// default color are green
		Color color = defaultColor;
		//System.out.println("The defalut color is: " + defaultColor.getR() + "," + defaultColor.getG() + defaultColor.getB());
		if(colored == VertexColors.COLORED) {
			// the base color of vertex is GREEN
			// we need to add shade to it, according to z-value
			color = interpretColor(tokens, startingIndex + NUM_TOKENS_FOR_POINT);
		}
		double r = surfaceColor.getR();
		double g = surfaceColor.getG();
		double b = surfaceColor.getB();
		
		// multiply current point by CTM inverse
		Point3DH camera_point = transformToCameraspace(point);
		
		// apply depth color to vertex here
		double z_camera = camera_point.getZ();
		if(z_camera >= z_near) {
			color = ambient_color.multiply(objectColor);
		}
		else if (z_camera <= z_far) {
			color = depthColor;
		}
		else if(z_camera <= z_near && z_camera >= z_far) {
			Color lightingColor = ambient_color.multiply(objectColor);
			double r_slope = (depthColor.getR() - lightingColor.getR()) / Math.abs(z_far - z_near);
			double g_slope = (depthColor.getG() - lightingColor.getG()) / Math.abs(z_far - z_near);
			double b_slope = (depthColor.getB() - lightingColor.getB()) / Math.abs(z_far - z_near);
			double disZ = Math.abs(z_camera) - Math.abs(z_near);
			double vertex_r = lightingColor.getR() + disZ * r_slope;
			double vertex_g = lightingColor.getG() + disZ * g_slope;
			double vertex_b = lightingColor.getB() + disZ * b_slope;
			// define new color for vertex 
			color = new Color(vertex_r, vertex_g, vertex_b);
		}
		/*
		// apply perspective matrix to current point
		Point3DH prj_point = transformToPerspective(camera_point);
		// multiply current point by toViewSpace
		Point3DH view_point = transformtoView(prj_point);
		
		// toWindowSpace has been already applied to point -----------
		double shade = (200 - Math.abs(view_point.getZ())) / 200;
		double new_r = r * shade;
		double new_g = g * shade;
		double new_b = b * shade;
		
		Color new_color = new Color(new_r, new_g, new_b);
		*/
		Vertex3D final_point = new Vertex3D(camera_point.getX(), camera_point.getY(), camera_point.getZ(), color);
		
		return final_point;
	}
	// transform to camera space
	public static Point3DH transformToCameraspace(Point3DH point) {
		double current_point[] = new double[4];
		double temp[] = new double[4];
		current_point[0] = point.getX();
		current_point[1] = point.getY();
		current_point[2] = point.getZ();
		current_point[3] = point.getW();
		//System.out.println("w is: " + current_point[3]);
		
		for(int i = 0; i < 4; i++) {
			double value = 0;
			for(int j = 0; j < 4; j++) {
				value += CTM_inverse[i][j] * current_point[j];
			}
			temp[i] = value;
		}
		current_point = temp;
		Point3DH new_point = new Point3DH(current_point[0], current_point[1], current_point[2]).euclidean();
		
		
		//System.out.println("The points after inverse of CTM: (" + current_point[0] + ", " + 
		//current_point[1] + ", " + current_point[2] + ")");
		return new_point;	
	}
	// transform to view space
	public static Point3DH transformtoView(Point3DH point) {
		double current_point[] = new double[4];
		double temp[] = new double[4];
		current_point[0] = point.getX();
		current_point[1] = point.getY();
		current_point[2] = point.getZ();
		current_point[3] = point.getW();
		
		for(int i = 0; i < 4; i++) {
			double value = 0;
			for(int j = 0; j < 4; j++) {
				value += toView[i][j] * current_point[j];
			}
			temp[i] = value;
		}
		current_point = temp;
		Point3DH new_point = new Point3DH(current_point[0], current_point[1], current_point[2]).euclidean();
		return new_point;	
	}
	
	// apply one_point perspective to current point
	public static Point3DH transformToPerspective(Point3DH point) {
		
		double current_point[] = new double[4];
		double temp[] = new double[4];
		current_point[0] = point.getX();
		current_point[1] = point.getY();
		current_point[2] = point.getZ();
		current_point[3] = point.getW();
		
		//System.out.println("w is: " + current_point[3]);
		
		for(int i = 0; i < 4; i++) {
			double value = 0;
			for(int j = 0; j < 4; j++) {
				value += toPerspective[i][j] * current_point[j];
			}
			temp[i] = value;
		}
		current_point = temp;
		
		Point3DH new_point;
		
		// if w is 0, apply euclidean()
		if(current_point[3] == 0) {
			new_point = new Point3DH(current_point[0], current_point[1], 
					current_point[2]).euclidean();
		}
		else {
		// convert homogeneous point to euclidean point by deviding x, y, z with w
		new_point = new Point3DH(current_point[0] / current_point[3], 
				current_point[1] / current_point[3], current_point[2] / current_point[3]);
		}
		
		//System.out.println("The points after perspective matrix: (" + new_point.getX() + 
		//		", " + new_point.getY() + ", " + new_point.getZ() + ")");
		return new_point;
	}
	
	// transform to camera space 
	public static Point3DH transformToWorldSpace(Point3DH point) {
		
		double current_point[] = new double[4];
		double temp[] = new double[4];
		current_point[0] = point.getX();
		current_point[1] = point.getY();
		current_point[2] = point.getZ();
		current_point[3] = point.getW();
		
		for(int i = 0; i < 4; i++) {
			double value = 0;
			for(int j = 0; j < 4; j++) {
				value += toWindowSpace[i][j] * current_point[j];
			}
			temp[i] = value;
		}
		current_point = temp;
		Point3DH new_point = new Point3DH(current_point[0], current_point[1], current_point[2]);
		//System.out.println("The points after window matrix: (" + new_point.getX() + ", " + 
		//new_point.getY() + ", " + new_point.getZ() + ")");
		return new_point;
	}
	
	public static Point3DH interpretPoint(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);

		// TODO: finish this method
		// multiply the current point with CTM
		double current_point[] = new double[4];
		double temp[] = new double[4];
		current_point[0] = x;
		current_point[1] = y;
		current_point[2] = z;
		current_point[3] = 1.0;		// w in homogeneous point

		for(int i = 0; i < 4; i++) {
			double value = 0;
			for(int j = 0; j < 4; j++) {
				value += CTM[i][j] * current_point[j];
			}
			temp[i] = value;
		}
		current_point = temp;
		
		Point3DH CTM_point = new Point3DH(current_point[0], current_point[1], current_point[2]);
		//System.out.println("The points after CTM: (" + current_point[0] + ", " + current_point[1] + ", " 
		//		+ current_point[2] + ")");
		return CTM_point;
	}
	
	public void getCTM_inverse() {
		double determinant = getDeterminant();
		double coefficient = 1/determinant;
		
		// b11
		CTM_inverse[0][0] = coefficient * (CTM[1][1] * CTM[2][2] * CTM[3][3] +
				CTM[1][2] * CTM[2][3] * CTM[3][1] + 
				CTM[1][3] * CTM[2][1] * CTM[3][2] - 
				CTM[1][1] * CTM[2][3] * CTM[3][2] - 
				CTM[1][2] * CTM[2][1] * CTM[3][3] -
				CTM[1][3] * CTM[2][2] * CTM[3][1]);
		// b12
		CTM_inverse[0][1] = coefficient * (CTM[0][1] * CTM[2][3] * CTM[3][2] +
				CTM[0][2] * CTM[2][1] * CTM[3][3] + 
				CTM[0][3] * CTM[2][2] * CTM[3][1] - 
				CTM[0][1] * CTM[2][2] * CTM[3][3] - 
				CTM[0][2] * CTM[2][3] * CTM[3][1] -
				CTM[0][3] * CTM[2][1] * CTM[3][2]);
		// b13
		CTM_inverse[0][2] = coefficient * (CTM[0][1] * CTM[1][2] * CTM[3][3] +
				CTM[0][2] * CTM[1][3] * CTM[3][1] + 
				CTM[0][3] * CTM[1][1] * CTM[3][2] - 
				CTM[0][1] * CTM[1][3] * CTM[3][2] - 
				CTM[0][2] * CTM[1][1] * CTM[3][3] -
				CTM[0][3] * CTM[1][2] * CTM[3][1]);
		// b14
		CTM_inverse[0][3] = coefficient * (CTM[0][1] * CTM[1][3] * CTM[2][2] +
				CTM[0][2] * CTM[1][1] * CTM[2][3] + 
				CTM[0][3] * CTM[1][2] * CTM[2][1] - 
				CTM[0][1] * CTM[1][2] * CTM[2][3] - 
				CTM[0][2] * CTM[1][3] * CTM[2][1] -
				CTM[0][3] * CTM[1][1] * CTM[2][2]);
		// b21
		CTM_inverse[1][0] = coefficient * (CTM[1][0] * CTM[2][3] * CTM[3][2] +
				CTM[1][2] * CTM[2][0] * CTM[3][3] + 
				CTM[1][3] * CTM[2][2] * CTM[3][0] - 
				CTM[1][0] * CTM[2][2] * CTM[3][3] - 
				CTM[1][2] * CTM[2][3] * CTM[3][0] -
				CTM[1][3] * CTM[2][0] * CTM[3][2]);
		// b22
		CTM_inverse[1][1] = coefficient * (CTM[0][0] * CTM[2][2] * CTM[3][3] +
				CTM[0][2] * CTM[2][3] * CTM[3][0] + 
				CTM[0][3] * CTM[2][0] * CTM[3][2] - 
				CTM[0][0] * CTM[2][3] * CTM[3][2] - 
				CTM[0][2] * CTM[2][0] * CTM[3][3] -
				CTM[0][3] * CTM[2][2] * CTM[3][0]);
		// b23
		CTM_inverse[1][2] = coefficient * (CTM[0][0] * CTM[1][3] * CTM[3][2] +
				CTM[0][2] * CTM[1][0] * CTM[3][3] + 
				CTM[0][3] * CTM[1][2] * CTM[3][0] - 
				CTM[0][0] * CTM[1][2] * CTM[3][3] - 
				CTM[0][2] * CTM[1][3] * CTM[3][0] -
				CTM[0][3] * CTM[1][0] * CTM[3][2]);
		// b24
		CTM_inverse[1][3] = coefficient * (CTM[0][0] * CTM[1][2] * CTM[2][3] +
				CTM[0][2] * CTM[1][3] * CTM[2][0] + 
				CTM[0][3] * CTM[1][0] * CTM[2][2] - 
				CTM[0][0] * CTM[1][3] * CTM[2][2] - 
				CTM[0][2] * CTM[1][0] * CTM[2][3] -
				CTM[0][3] * CTM[1][2] * CTM[2][0]);
		// b31
		CTM_inverse[2][0] = coefficient * (CTM[1][0] * CTM[2][1] * CTM[3][3] +
				CTM[1][1] * CTM[2][3] * CTM[3][0] + 
				CTM[1][3] * CTM[2][0] * CTM[3][1] - 
				CTM[1][0] * CTM[2][3] * CTM[3][1] - 
				CTM[1][1] * CTM[2][0] * CTM[3][3] -
				CTM[1][3] * CTM[2][1] * CTM[3][0]);
		// b32
		CTM_inverse[2][1] = coefficient * (CTM[0][0] * CTM[2][3] * CTM[3][1] +
				CTM[0][1] * CTM[2][0] * CTM[3][3] + 
				CTM[0][3] * CTM[2][1] * CTM[3][0] - 
				CTM[0][0] * CTM[2][1] * CTM[3][3] - 
				CTM[0][1] * CTM[2][3] * CTM[3][0] -
				CTM[0][3] * CTM[2][0] * CTM[3][1]);
		// b33
		CTM_inverse[2][2] = coefficient * (CTM[0][0] * CTM[1][1] * CTM[3][3] +
				CTM[0][1] * CTM[1][3] * CTM[3][0] + 
				CTM[0][3] * CTM[1][0] * CTM[3][1] - 
				CTM[0][0] * CTM[1][3] * CTM[3][1] - 
				CTM[0][1] * CTM[1][0] * CTM[3][3] -
				CTM[0][3] * CTM[1][1] * CTM[3][0]);
		// b34
		CTM_inverse[2][3] = coefficient * (CTM[0][0] * CTM[1][3] * CTM[2][1] +
				CTM[0][1] * CTM[1][0] * CTM[2][3] + 
				CTM[0][3] * CTM[1][1] * CTM[2][0] - 
				CTM[0][0] * CTM[1][1] * CTM[2][3] - 
				CTM[0][1] * CTM[1][3] * CTM[2][0] -
				CTM[0][3] * CTM[1][0] * CTM[2][1]);
		// b41
		CTM_inverse[3][0] = coefficient * (CTM[1][0] * CTM[2][2] * CTM[3][1] +
				CTM[1][1] * CTM[2][0] * CTM[3][2] + 
				CTM[1][2] * CTM[2][1] * CTM[3][0] - 
				CTM[1][0] * CTM[2][1] * CTM[3][2] - 
				CTM[1][1] * CTM[2][2] * CTM[3][0] -
				CTM[1][2] * CTM[2][0] * CTM[3][1]);
		// b42
		CTM_inverse[3][1] = coefficient * (CTM[0][0] * CTM[2][1] * CTM[3][2] +
				CTM[0][1] * CTM[2][2] * CTM[3][0] + 
				CTM[0][2] * CTM[2][0] * CTM[3][1] - 
				CTM[0][0] * CTM[2][2] * CTM[3][1] - 
				CTM[0][1] * CTM[2][0] * CTM[3][2] -
				CTM[0][2] * CTM[2][1] * CTM[3][0]);
		// b43
		CTM_inverse[3][2] = coefficient * (CTM[0][0] * CTM[1][2] * CTM[3][1] +
				CTM[0][1] * CTM[1][0] * CTM[3][2] + 
				CTM[0][2] * CTM[1][1] * CTM[3][0] - 
				CTM[0][0] * CTM[1][1] * CTM[3][2] - 
				CTM[0][1] * CTM[1][2] * CTM[3][0] -
				CTM[0][2] * CTM[1][0] * CTM[3][1]);
		// b44
		CTM_inverse[3][3] = coefficient * (CTM[0][0] * CTM[1][1] * CTM[2][2] +
				CTM[0][1] * CTM[1][2] * CTM[2][0] + 
				CTM[0][2] * CTM[1][0] * CTM[2][1] - 
				CTM[0][0] * CTM[1][2] * CTM[2][1] - 
				CTM[0][1] * CTM[1][0] * CTM[2][2] -
				CTM[0][2] * CTM[1][1] * CTM[2][0]);
		
	}
	
	public double getDeterminant() {
		double det;
		det = CTM[0][0] * CTM[1][1] * CTM[2][2] * CTM[3][3] +
				CTM[0][0] * CTM[1][2] * CTM[2][3] * CTM[3][1] +
				CTM[0][0] * CTM[1][3] * CTM[2][1] * CTM[3][2] +
				CTM[0][1] * CTM[1][0] * CTM[2][3] * CTM[3][2] +
				CTM[0][1] * CTM[1][2] * CTM[2][0] * CTM[3][3] +
				CTM[0][1] * CTM[1][3] * CTM[2][2] * CTM[3][0] + 
				CTM[0][2] * CTM[1][0] * CTM[2][1] * CTM[3][3] + 
				CTM[0][2] * CTM[1][1] * CTM[2][3] * CTM[3][0] +
				CTM[0][2] * CTM[1][3] * CTM[2][0] * CTM[3][1] +
				CTM[0][3] * CTM[1][0] * CTM[2][2] * CTM[3][1] +
				CTM[0][3] * CTM[1][1] * CTM[2][0] * CTM[3][2] + 
				CTM[0][3] * CTM[1][2] * CTM[2][1] * CTM[3][0] -
				CTM[0][0] * CTM[1][1] * CTM[2][3] * CTM[3][2] -
				CTM[0][0] * CTM[1][2] * CTM[2][1] * CTM[3][3] -
				CTM[0][0] * CTM[1][3] * CTM[2][2] * CTM[3][1] -
				CTM[0][1] * CTM[1][0] * CTM[2][2] * CTM[3][3] -
				CTM[0][1] * CTM[1][2] * CTM[2][3] * CTM[3][0] -
				CTM[0][1] * CTM[1][3] * CTM[2][0] * CTM[3][2] -
				CTM[0][2] * CTM[1][0] * CTM[2][3] * CTM[3][1] -
				CTM[0][2] * CTM[1][1] * CTM[2][0] * CTM[3][3] -
				CTM[0][2] * CTM[1][3] * CTM[2][1] * CTM[3][0] -
				CTM[0][3] * CTM[1][0] * CTM[2][1] * CTM[3][2] -
				CTM[0][3] * CTM[1][1] * CTM[2][2] * CTM[3][0] -
				CTM[0][3] * CTM[1][2] * CTM[2][0] * CTM[3][1];

		return det;
	}
	public static Color interpretColor(String[] tokens, int startingIndex) {
		double r = cleanNumber(tokens[startingIndex]);
		double g = cleanNumber(tokens[startingIndex + 1]);
		double b = cleanNumber(tokens[startingIndex + 2]);

		// TODO: finish this method
		Color color =  Color.fromARGB(Color.makeARGB((int)Math.round(r), (int)Math.round(g), (int)Math.round(b)));
		return color;
	}
	
	public static Point3DH interpretPointWithW(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);
		double w = cleanNumber(tokens[startingIndex + 3]);
		Point3DH point = new Point3DH(x, y, z, w);
		return point;
	}
	
	// interpreter object file
//	private void objFile(String filename) {
//		ObjReader objReader = new ObjReader(filename, defaultColor);
//		objReader.read();
//		objReader.render();
//	}
/*
	private void line(Vertex3D p1, Vertex3D p2) {
		Vertex3D screenP1 = transformToCamera(p1);
		Vertex3D screenP2 = transformToCamera(p2);
		// TODO: finish this method
	}
	private void polygon(Vertex3D p1, Vertex3D p2, Vertex3D p3) {
		Vertex3D screenP1 = transformToCamera(p1);
		Vertex3D screenP2 = transformToCamera(p2);
		Vertex3D screenP3 = transformToCamera(p3);
		// TODO: finish this method
	}

	private VerormToCamera(Vertex3D vertex) {
		// TODO: finish this method
	}
*/
}
