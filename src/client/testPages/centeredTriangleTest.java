package client.testPages;

import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import polygon.PolygonRenderer;
import polygon.Polygon;
import java.util.Random;

public class centeredTriangleTest {
	
	private final Drawable panel;
	private final PolygonRenderer polygonRenderer;
	// the polygon array storing all 6 triangles
	private Polygon triangles[] = new Polygon[6];
	private final int NUM_TRIANGLES = 6;
	private Vertex3D center;
	private final int radius = 275;
	private Vertex3D circular_points[] = new Vertex3D[400];
	private double random_color[] = new double[6];
	private Color triangle_color;
	
	public centeredTriangleTest(Drawable panel, PolygonRenderer polygonRenderer) {
		
		this.panel = panel;
		this.polygonRenderer = polygonRenderer;
		
		// get the center of the panel
		getCenter();
		
		initializeColor();
		
		renderTriangles(panel, polygonRenderer);
		
		//colorTriangles();
	}
	
	private void initializeColor() {
		random_color[0] = 0.25;
		random_color[1] = 0.4;
		random_color[2] = 1.0;
		random_color[3] = 0.85;
		random_color[4] = 0.55;
		random_color[5] = 0.7;
	}
	
	private void renderTriangles(Drawable panel, PolygonRenderer polygonRenderer) {
		
		int index = 0;
		double angle = 0;
		for(; angle < 360; angle++) {
			circular_points[index] = radialPoint(radius, angle);
			index ++;
		}
		// randomly pick one vertex between 0 and 359
		index = 0;
		
		for(; index < NUM_TRIANGLES; index++) {
			int random_index = (int)(Math.random() * 359 + 0);
			
			triangle_color = new Color(random_color[5 - index], random_color[5 - index], 
					random_color[5 - index]);
			Vertex3D p1 = new Vertex3D(circular_points[random_index].getX(), circular_points[random_index].getY(), 
					0, triangle_color);
			
			System.out.println("X is: " + p1.getX());
			// get the next point: rotate 120 degree about center
			
			double new_x = (p1.getX() - center.getX()) * Math.cos(Math.toRadians(120)) - 
					(p1.getY() - center.getY()) * Math.sin(Math.toRadians(120)) + center.getX();
			double new_y = (p1.getX() - center.getX()) * Math.sin(Math.toRadians(120)) + 
					(p1.getY() - center.getY()) * Math.cos(Math.toRadians(120)) + center.getY();
			Vertex3D p2 = new Vertex3D(new_x, new_y, 0, triangle_color);
			
			// get the last point
			new_x = (p2.getX() - center.getX()) * Math.cos(Math.toRadians(120)) - 
					(p2.getY() - center.getY()) * Math.sin(Math.toRadians(120)) + center.getX();
			new_y = (p2.getX() - center.getX()) * Math.sin(Math.toRadians(120)) + 
					(p2.getY() - center.getY()) * Math.cos(Math.toRadians(120)) + center.getY();
			Vertex3D p3 = new Vertex3D(new_x, new_y, 0, triangle_color);
			// make the triangle
			triangles[index] = Polygon.make(p1, p2, p3);
		}
		
		// now we have all 6 triangles. Draw them!
		index = 0;
		for(; index < NUM_TRIANGLES; index++) {
			
			polygonRenderer.drawPolygon(triangles[index], panel);
		}
		
	}
	
	private void getCenter() {
		int center_width = panel.getWidth() / 2;
		int center_height = panel.getHeight() / 2;
		center = new Vertex3D(center_width, center_height, 0, Color.BLACK);
		System.out.println("The center is: " + center_width + " , " + center_height);
	}
	
	private Vertex3D radialPoint(double radius, double angle) {
		double x = center.getX() + radius * Math.cos(angle);	// the end-point on center
		double y = center.getY() + radius * Math.sin(angle);	// the end-point on circular edge
		return new Vertex3D(x, y, 0, Color.WHITE);		// we have two end-points to draw a line using line renderer
	}
}
