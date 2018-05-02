package client.testPages;

import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import polygon.PolygonRenderer;
import polygon.Polygon;
import java.util.Random;

public class MeshPolygonTest {
	
	public static final int NO_PERTURBATION = 1;
	public static final int USE_PERTURBATION = 0;
	private final Drawable panel;
	private final PolygonRenderer polygonRenderer;
	private static Vertex3D[][] random_points = new Vertex3D[10][10];		// 100 random points in total
	private static final int NUM_ROW_COLUMN = 10;
	private static int mode_counter = 0;				// used for testing if random_points should be generated
	
	public MeshPolygonTest(Drawable panel, PolygonRenderer polygonRenderer, int mode) {
		
		this.panel = panel;
		this.polygonRenderer = polygonRenderer;
		// check which mode should be applied
		if(mode == 1) {					
			
			polygonDrawing(panel, polygonRenderer);				
		}
		else {
			if(mode_counter == 0) {
				randomVertex();			
			}
			randomPolygonDrawing(panel, polygonRenderer);
		}
	}
	
	// margin: 18px 
	//distance between points: 66px
	// renderer.drawPloygon()
	private void polygonDrawing(Drawable panel, PolygonRenderer polygonrenderer) {
		
		int row_counter = 0;
		int column_counter;
		for(; row_counter < NUM_ROW_COLUMN - 1; row_counter++) {
			column_counter = 0;
			for(; column_counter < NUM_ROW_COLUMN - 1; column_counter++) {
				
				// draw upward triangles
				Vertex3D p1 = new Vertex3D(15 + column_counter * 30, 15 + row_counter * 30, 0, Color.WHITE);
				Vertex3D p2 = new Vertex3D(15 + column_counter * 30 + 30, 15 + row_counter * 30, 0, Color.WHITE);
				Vertex3D p3 = new Vertex3D(15 + column_counter * 30 + 30, 15 + row_counter * 30 + 30, 0, Color.WHITE);
				Polygon polygon = Polygon.make(p1, p2, p3);
				polygonRenderer.drawPolygon(polygon, panel);
				
				// draw downward triangles
				Vertex3D p4 = new Vertex3D(15 + column_counter * 30, 15 + row_counter * 30, 0, Color.WHITE);
				Vertex3D p5 = new Vertex3D(15 + column_counter * 30, 15 + row_counter * 30 + 30, 0, Color.WHITE);
				Vertex3D p6 = new Vertex3D(15 + column_counter * 30 + 30, 15 + row_counter * 30 + 30, 0, Color.WHITE);
				Polygon polygon1 = Polygon.make(p4, p5, p6);
				polygonRenderer.drawPolygon(polygon1, panel);
			}
		}
	}
	
	// generate 100 random points
	private void randomVertex() {
	
		int row_counter = 0;
		int column_counter;
		Random rand = new Random();
		for(; row_counter < NUM_ROW_COLUMN; row_counter++) {
			column_counter = 0;
			for(; column_counter < NUM_ROW_COLUMN; column_counter++) {
				
				// get the random color of vertex
				double color_r = Math.random();
				double color_g = Math.random();
				double color_b = Math.random();
				Color random_color = new Color(color_r, color_g, color_b);
				// ----------------------------------
				
				Vertex3D p1 = new Vertex3D(28 + column_counter * 66 + rand.nextInt(25) - 12, 
						28 + row_counter * 66 + rand.nextInt(25) - 12, 0, random_color);
				random_points[row_counter][column_counter] = p1;
			}
		}
	}
	
	// draw and fill random triangles
	private void randomPolygonDrawing(Drawable panel, PolygonRenderer polygonrenderer) {
		
		int row_counter = 0;
		int column_counter;
		for(; row_counter < NUM_ROW_COLUMN - 1; row_counter++) {
			column_counter = 0;
			for(; column_counter < NUM_ROW_COLUMN - 1; column_counter++) {
				
				// draw all upward triangles
				Vertex3D p1 = random_points[row_counter][column_counter];
				Vertex3D p2 = random_points[row_counter][column_counter + 1];
				Vertex3D p3 = random_points[row_counter + 1][column_counter + 1];
				Polygon polygon = Polygon.make(p1, p2, p3);
				polygonrenderer.drawPolygon(polygon, panel);
				
				// draw all downward triangles
				Vertex3D p4 = random_points[row_counter][column_counter];
				Vertex3D p5 = random_points[row_counter + 1][column_counter];
				Vertex3D p6 = random_points[row_counter + 1][column_counter + 1];
				Polygon polygon1 = Polygon.makeEnsuringClockwise(p4, p5, p6);
				polygonrenderer.drawPolygon(polygon1, panel);
				System.out.println("points are: " + p4.toString());
			}
		}
		mode_counter ++;
		if(mode_counter == 2) {
			mode_counter = 0;
		}
	}
}

