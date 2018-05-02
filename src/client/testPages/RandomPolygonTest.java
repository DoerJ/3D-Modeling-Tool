package client.testPages;

import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import polygon.PolygonRenderer;
import polygon.Polygon;
import java.util.Random;

public class RandomPolygonTest {
	
	private final static int NUM_TRIANGLES = 20;
	Drawable panel;
	PolygonRenderer polygonRenderer;
	private static Polygon[] copy_triangles = new Polygon[20];	// 20 triangles
	private static int visit = 0;
	
	public RandomPolygonTest(Drawable panel, PolygonRenderer polygonRenderer) {
		
		this.panel = panel;
		this.polygonRenderer = polygonRenderer;
		
		if(visit == 0) {				// if this is the first visit
			randomTriPoints(panel, polygonRenderer);
		}
		else {
			copyTriPoints(panel, polygonRenderer);
		}
	}
	
	private void randomTriPoints(Drawable panel, PolygonRenderer polygonRenderer) {
		
		int count = 0;
		Random rand = new Random();
		for(; count < NUM_TRIANGLES; count++) {
		// we need three points to get a triangle
		Vertex3D[] points = new Vertex3D[3];
		points[0] = new Vertex3D(rand.nextInt(299) + 0, rand.nextInt(299) + 0, 0, Color.WHITE);
		points[1] = new Vertex3D(rand.nextInt(299) + 0, rand.nextInt(299) + 0, 0, Color.WHITE);
		points[2] = new Vertex3D(rand.nextInt(299) + 0, rand.nextInt(299) + 0, 0, Color.WHITE);
		
		// store random triangles
		Polygon polygon = Polygon.makeEnsuringClockwise(points[0], points[1], points[2]);
		copy_triangles[count] = polygon;
		
		polygonRenderer.drawPolygon(polygon, panel);
		}
		visit ++;
		if(visit == 2) {
			visit = 0;
		}
		System.out.println("num of triangles is: " + count);
	}
	
	private void copyTriPoints(Drawable panel, PolygonRenderer polygonrenderer) {
		
		int count = 0;
		for(; count < NUM_TRIANGLES; count++) {
			polygonRenderer.drawPolygon(copy_triangles[count], panel);
		}
		
		visit ++;
		if(visit == 2) {
			visit = 0;
		}
	}
}