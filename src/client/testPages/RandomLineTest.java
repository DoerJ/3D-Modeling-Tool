package client.testPages;

// two function: 1) render a set of random lines  2) apply random set of colors to lines
import geometry.Vertex3D;
import line.LineRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import java.util.Random;

public class RandomLineTest {
	
	// a double array for storing end-points of lines
	static double[] x_start = new double[30];
	static double[] y_start = new double[30];
	static double[] x_end = new double[30];
	static double[] y_end = new double[30];
	static double[] color_R = new double[30];
	static double[] color_G = new double[30];
	static double[] color_B = new double[30];
	final static double[] copy_x_start = new double[30];
	final static double[] copy_y_start = new double[30];
	final static double[] copy_x_end = new double[30];
	final static double[] copy_y_end = new double[30];
	final static double[] copy_R = new double[30];
	final static double[] copy_G = new double[30];
	final static double[] copy_B = new double[30];
	
	private static final int NUM_LINES = 30;	// number of random lines
	private static final double min = 0.0;
	private static final double max = 299.0;
	private final Drawable panel;
	private final LineRenderer renderer;
	public static int visit = 0;
	
	// constructor
	public RandomLineTest(Drawable panel, LineRenderer renderer) {
		
		this.panel = panel;
		this.renderer = renderer;
		if (visit % 4 == 0) {
		// get random end-points of lines
		randomEndpoints();
		
		// get random color of lines
		randomColor();
		
		// now we have all 30 pairs of end-points stored in four double arrays
		// ready for render
		render();
		}
		else {
			renderCopy();
		}
		
		visit ++;
	}
	
	// store end-points to double arrays
	private void randomEndpoints() {
		
		double random_point;
		int point_counter = 0;
		for(; point_counter < NUM_LINES; point_counter++) {
			
			random_point = min + new Random().nextDouble() * (max - min);
			x_start[point_counter] = random_point;
			random_point = min + new Random().nextDouble() * (max - min);
			y_start[point_counter] = random_point;
			random_point = min + new Random().nextDouble() * (max - min);
			x_end[point_counter] = random_point;
			random_point = min + new Random().nextDouble() * (max - min);
			y_end[point_counter] = random_point;
			copyPoints(x_start[point_counter], y_start[point_counter], x_end[point_counter], 
					y_end[point_counter], point_counter);
		}
	}
	
	private void copyPoints(double x_start, double y_start, double x_end, double y_end, int counter) {
		
		// copy and store all points into final static double arrays
		copy_x_start[counter] = x_start;
		copy_y_start[counter] = y_start;
		copy_x_end[counter] = x_end;
		copy_y_end[counter] = y_end;
	}
	
	// store random color into arrays
	private void randomColor() {
		
		int color_counter = 0;
		for(; color_counter < NUM_LINES; color_counter++) {
			
			color_R[color_counter] = Math.random();
			color_G[color_counter] = Math.random();
			color_B[color_counter] = Math.random();
			copyColor(color_R[color_counter], color_G[color_counter], color_B[color_counter], color_counter);
		}
	}
	
	private void copyColor(double r, double g, double b, int counter) {
		
		// copy and store random color into final static double arrays
		copy_R[counter] = r;
		copy_G[counter] = g;
		copy_B[counter] = b;
	}
	// render lines on panels
	private void render() {
		
		Color COLOR_OF_LINES;
		int point_counter = 0;
		for(; point_counter < NUM_LINES; point_counter++) {
			
			COLOR_OF_LINES = new Color(color_R[point_counter], color_G[point_counter], color_B[point_counter]);
			// add random colors
			Vertex3D first_endpoint = new Vertex3D(x_start[point_counter], y_start[point_counter], 0, COLOR_OF_LINES);
			Vertex3D second_endpoint = new Vertex3D(x_end[point_counter], y_end[point_counter], 0, COLOR_OF_LINES);
			renderer.drawLine(first_endpoint, second_endpoint, panel);
		}
	}
	
	private void renderCopy() {
		
		Color COLOR_OF_LINES;
		int point_counter = 0;
		for(; point_counter < NUM_LINES; point_counter++) {
			
			COLOR_OF_LINES = new Color(copy_R[point_counter], copy_G[point_counter], copy_B[point_counter]);
			// add random colors
			Vertex3D first_endpoint = new Vertex3D(copy_x_start[point_counter], copy_y_start[point_counter], 0, COLOR_OF_LINES);
			Vertex3D second_endpoint = new Vertex3D(copy_x_end[point_counter], copy_y_end[point_counter], 0, COLOR_OF_LINES);
			renderer.drawLine(first_endpoint, second_endpoint, panel);
		}
	}
}
